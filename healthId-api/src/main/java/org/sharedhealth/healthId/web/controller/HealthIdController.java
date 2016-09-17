package org.sharedhealth.healthId.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.sharedhealth.healthId.web.Model.GeneratedHIDBlock;
import org.sharedhealth.healthId.web.Model.MciHealthId;
import org.sharedhealth.healthId.web.Model.OrgHealthId;
import org.sharedhealth.healthId.web.config.HealthIdProperties;
import org.sharedhealth.healthId.web.exception.InvalidRequestException;
import org.sharedhealth.healthId.web.security.UserInfo;
import org.sharedhealth.healthId.web.service.FacilityService;
import org.sharedhealth.healthId.web.service.HealthIdService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import rx.functions.Action1;

import java.util.*;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping("/healthIds")
public class HealthIdController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(HealthIdController.class);

    public static final String GENERATE_ALL_URI = "/generate";
    public static final String GENERATE_BLOCK_URI = "/generateBlock";
    public static final String GENERATE_BLOCK_FOR_ORG_URI = "/generateBlockForOrg";
    private static final long HID_GENERATION_LIMIT = 2000000;

    private HealthIdService healthIdService;
    private FacilityService facilityService;
    private HealthIdProperties healthIdProperties;

    @Autowired
    public HealthIdController(HealthIdService healthIdService, FacilityService facilityService, HealthIdProperties healthIdProperties) {
        this.healthIdService = healthIdService;
        this.facilityService = facilityService;
        this.healthIdProperties = healthIdProperties;
    }

    @PreAuthorize("hasAnyRole('ROLE_SHR System Admin')")
    @RequestMapping(method = POST, value = GENERATE_ALL_URI)
    public DeferredResult<String> generate() {
        UserInfo userInfo = getUserInfo();
        logAccessDetails(userInfo, "Generating new hids");
        GeneratedHIDBlock generatedHIDBlock = healthIdService.generateAll(userInfo);
        final DeferredResult<String> deferredResult = new DeferredResult<>();
        String message = String.format("Generated %s HIDs.", generatedHIDBlock.getTotalHIDs());
        deferredResult.setResult(message);
        logger.info(message);
        return deferredResult;
    }

    @PreAuthorize("hasAnyRole('ROLE_SHR System Admin')")
    @RequestMapping(method = POST, value = GENERATE_BLOCK_URI)
    public DeferredResult<String> generateBlock(@RequestParam(value = "start") long start,
                                                @RequestParam(value = "totalHIDs") long totalHIDs) {
        if (isStartInvalidForMCI(start)) {
            throw new InvalidRequestException(String.format("%s not for MCI", start));
        }
        UserInfo userInfo = getUserInfo();
        logAccessDetails(userInfo, "Generating new hids");
        GeneratedHIDBlock generatedHIDBlock = healthIdService.generateBlock(start, totalHIDs, userInfo);
        return getResult(generatedHIDBlock, totalHIDs);
    }

    @PreAuthorize("hasAnyRole('ROLE_SHR System Admin')")
    @RequestMapping(method = POST, value = GENERATE_BLOCK_FOR_ORG_URI)
    public DeferredResult<String> generateBlockForOrg(@RequestParam(value = "org") String orgCode,
                                                      @RequestParam(value = "start") long start,
                                                      @RequestParam(value = "totalHIDs") long totalHIDs) {
        validateRequest(orgCode, start, totalHIDs);
        UserInfo userInfo = getUserInfo();
        logAccessDetails(userInfo, "Generating new hids");
        GeneratedHIDBlock generatedHIDBlock = healthIdService.generateBlockForOrg(start, totalHIDs, orgCode, userInfo);
        return getResult(generatedHIDBlock, totalHIDs);
    }

    @PreAuthorize("hasAnyRole('ROLE_SHR System Admin')")
    @RequestMapping(method = GET, value = "/nextBlock/mci/{mciCode}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map nextBlock(@PathVariable(value = "mciCode") String mciCode,
                         @RequestParam(value = "blockSize", required = false) Integer blockSize) throws JsonProcessingException {
        int defaultBlockSize = healthIdProperties.getHealthIdBlockSize();
        if (blockSize == null || blockSize <= 0 || blockSize > defaultBlockSize)
            blockSize = defaultBlockSize;
        logAccessDetails(getUserInfo(), "Assigning next block to MCI");
        List<MciHealthId> nextBlock = healthIdService.getNextBlock(mciCode, blockSize);
        HashMap<String, Object> responseMap = new HashMap<>();
        int totalHids = nextBlock.size();
        responseMap.put("total", totalHids);
        Collection hids = CollectionUtils.collect(nextBlock, new Transformer() {
            @Override
            public String transform(Object input) {
                return ((MciHealthId) input).getHid();
            }
        });
        responseMap.put("hids", hids);
        logger.info("Assigned {} MCI healthIds for {}.", totalHids, mciCode);
        return responseMap;
    }

    @PreAuthorize("hasAnyRole('ROLE_SHR System Admin')")
    @RequestMapping(method = PUT, value = "/markUsed/{healthId}", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public DeferredResult<String> markUsed(@PathVariable(value = "healthId") String healthId,
                                           @RequestBody Map responseBody) throws JsonProcessingException {
        logger.debug("Marking {} as used.", healthId);
        logAccessDetails(getUserInfo(), "Marking Health Id as used");
        final DeferredResult<String> deferredResult = new DeferredResult<>();
        String usedAt = (String) responseBody.get("used_at");
        rx.Observable<Boolean> observable = healthIdService.markOrgHealthIdUsed(healthId, UUID.fromString(usedAt));
        observable.subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean aBoolean) {
                if (aBoolean)
                    deferredResult.setResult("Accepted");
                else
                    deferredResult.setErrorResult(new InvalidRequestException("Rejected"));
            }
        }, errorCallback(deferredResult));
        return deferredResult;
    }

    @PreAuthorize("hasAnyRole('ROLE_SHR System Admin')")
    @RequestMapping(method = GET, value = "/checkAvailability/{healthId}")
    public DeferredResult<Map> checkAvailability(@PathVariable(value = "healthId") String healthId,
                                                 @RequestParam(value = "orgCode", required = true) final String orgCode) {
        logger.debug("Checking availability of {} for org {}.", healthId, orgCode);
        logAccessDetails(getUserInfo(), "Checking availability of Health Id");
        final DeferredResult<Map> deferredResult = new DeferredResult<>();
        rx.Observable<OrgHealthId> observable = healthIdService.findOrgHealthId(healthId);
        observable.subscribe(new Action1<OrgHealthId>() {
            @Override
            public void call(OrgHealthId orgHealthId) {
                Map<String, Object> map = new HashMap<>();
                map.put("availability", false);
                if (orgHealthId == null) {
                    map.put("reason", "Health Id is not allocated to any organization.");
                } else if (orgHealthId.isUsed()) {
                    map.put("reason", "Health Id is already allocated to another patient.");
                } else if (!orgCode.equals(orgHealthId.getAllocatedFor())) {
                    map.put("reason", "Health Id is allocated to another organization.");
                } else {
                    map.put("availability", true);
                }
                deferredResult.setResult(map);
            }
        }, errorCallback(deferredResult));
        return deferredResult;
    }

    private <T> Action1<Throwable> errorCallback(final DeferredResult<T> deferredResult) {
        return new Action1<Throwable>() {
            @Override
            public void call(Throwable error) {
                logger.error(error.getMessage());
                deferredResult.setErrorResult(error);
            }
        };
    }

    private DeferredResult<String> getResult(GeneratedHIDBlock generatedHIDBlock, long totalHIDs) {
        final DeferredResult<String> deferredResult = new DeferredResult<>();
        String message;
        if (generatedHIDBlock.getTotalHIDs() < totalHIDs) {
            message = String.format("Can generate only %s HIDs, because series exhausted. Use another series.", generatedHIDBlock.getTotalHIDs());
        } else {
            message = String.format("Generated %s HIDs.", generatedHIDBlock.getTotalHIDs());
        }
        deferredResult.setResult(message);
        logger.info(message);
        return deferredResult;
    }

    private boolean isStartInvalidForMCI(long start) {
        return healthIdProperties.getMciStartHid() > start || healthIdProperties.getMciEndHid() < start;
    }

    private void validateRequest(String orgCode, long start, long totalHIDs) {
        if (orgCode.equals(healthIdProperties.getMciOrgCode())) {
            throw new InvalidRequestException(String.format("This endpoint is not for MCI. To generate HIDs for MCI use %s endpoint", GENERATE_BLOCK_URI));
        }
        if (isStartInvalidForOtherOrg(start)) {
            throw new InvalidRequestException(String.format("%s series is not valid.", start));
        }
        if (totalHIDs > HID_GENERATION_LIMIT) {
            throw new InvalidRequestException(String.format("Total HIDs should not be more than %s", HID_GENERATION_LIMIT));
        }
        if (isInvalidOrg(orgCode)) {
            throw new InvalidRequestException(String.format("Invalid Organization:- %s", orgCode));
        }
    }

    private boolean isInvalidOrg(String orgCode) {
        return facilityService.find(orgCode) == null;
    }

    private boolean isStartInvalidForOtherOrg(long start) {
        return healthIdProperties.getOtherOrgStartHid() > start || healthIdProperties.getOtherOrgEndHid() < start;
    }
}
