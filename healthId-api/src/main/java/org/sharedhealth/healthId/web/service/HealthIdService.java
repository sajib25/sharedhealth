package org.sharedhealth.healthId.web.service;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.healthId.web.Model.GeneratedHIDBlock;
import org.sharedhealth.healthId.web.Model.MciHealthId;
import org.sharedhealth.healthId.web.Model.OrgHealthId;
import org.sharedhealth.healthId.web.Model.RequesterDetails;
import org.sharedhealth.healthId.web.config.HealthIdProperties;
import org.sharedhealth.healthId.web.exception.HealthIdExhaustedException;
import org.sharedhealth.healthId.web.exception.HealthIdNotFoundException;
import org.sharedhealth.healthId.web.exception.HidGenerationException;
import org.sharedhealth.healthId.web.repository.HealthIdRepository;
import org.sharedhealth.healthId.web.security.UserInfo;
import org.sharedhealth.healthId.web.utils.FileUtil;
import org.sharedhealth.healthId.web.utils.LuhnChecksumGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rx.Observable;
import rx.functions.Func1;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import static com.datastax.driver.core.utils.UUIDs.timeBased;
import static org.sharedhealth.healthId.web.utils.DateUtil.SIMPLE_DATE_WITH_SECS_FORMAT;
import static org.sharedhealth.healthId.web.utils.DateUtil.toDateString;
import static org.sharedhealth.healthId.web.utils.JsonMapper.writeValueAsString;

@Component
public class HealthIdService {
    private static Logger logger = LoggerFactory.getLogger(HealthIdService.class);

    private static final int DIGITS_FOR_BLOCK_SEPARATION = 2;
    private static final String DEFAULT_HID_STORAGE_PATH = "/opt/mci/hid";

    private final Pattern mciInvalidHidPattern;
    private final Pattern orgInvalidHidPattern;
    private final HealthIdProperties healthIdProperties;
    private HealthIdRepository healthIdRepository;
    private LuhnChecksumGenerator checksumGenerator;
    private GeneratedHidBlockService generatedHidBlockService;

    @Autowired
    public HealthIdService(HealthIdProperties healthIdProperties, HealthIdRepository healthIdRepository,
                           LuhnChecksumGenerator checksumGenerator, GeneratedHidBlockService generatedHidBlockService) {
        this.healthIdProperties = healthIdProperties;
        this.healthIdRepository = healthIdRepository;
        this.checksumGenerator = checksumGenerator;
        this.generatedHidBlockService = generatedHidBlockService;
        this.mciInvalidHidPattern = Pattern.compile(healthIdProperties.getMciInvalidHidPattern());
        this.orgInvalidHidPattern = Pattern.compile(healthIdProperties.getOtherOrgInvalidHidPattern());
    }

    public GeneratedHIDBlock generateAll(UserInfo userInfo) {
        Long start = healthIdProperties.getMciStartHid();
        Long end = healthIdProperties.getMciEndHid();
        long numberOfValidHIDs = 0L;
        for (long i = start; i <= end; i++) {
            numberOfValidHIDs = saveIfValidMciHID(numberOfValidHIDs, i);
        }
        return saveGeneratedBlock(start, end, numberOfValidHIDs, healthIdProperties.getMciOrgCode(), userInfo, timeBased());
    }

    public GeneratedHIDBlock generateBlock(long start, long totalHIDs, UserInfo userInfo) {
        long numberOfValidHIDs = 0L;
        long seriesNo = identifySeriesNo(start);
        long startForBlock = identifyStartInSeries(seriesNo);
        int i;
        for (i = 0; numberOfValidHIDs < totalHIDs; i++) {
            long possibleHID = startForBlock + i;
            if (!isPartOfSeries(seriesNo, possibleHID)) {
                break;
            }
            numberOfValidHIDs = saveIfValidMciHID(numberOfValidHIDs, possibleHID);
        }
        long end = startForBlock + i - 1;
        return saveGeneratedBlock(startForBlock, end, numberOfValidHIDs, healthIdProperties.getMciOrgCode(), userInfo, timeBased());
    }

    public GeneratedHIDBlock generateBlockForOrg(long start, long totalHIDs, String orgCode, UserInfo userInfo) throws HidGenerationException {
        UUID generatedAt = timeBased();
        long numberOfValidHIDs = 0L;
        long seriesNo = identifySeriesNo(start);
        long startForBlock = identifyStartInSeries(seriesNo);
        File hidFile = createFileForOrg(orgCode);
        if (hidStoragePathExists(hidFile)) {
            logger.info(String.format("Saving HIDs to file %s ", hidFile.getAbsolutePath()));
            long i;
            for (i = 0; numberOfValidHIDs < totalHIDs; i++) {
                long possibleHID = startForBlock + i;
                if (!isPartOfSeries(seriesNo, possibleHID)) {
                    break;
                }
                numberOfValidHIDs = saveIfValidOrgHID(orgCode, numberOfValidHIDs, hidFile, possibleHID, generatedAt);
            }
            return saveGeneratedBlock(startForBlock, startForBlock + i - 1, numberOfValidHIDs, orgCode, userInfo, generatedAt);
        } else {
            logger.info(String.format("HID Storage path %s does not exists ", hidFile.getAbsolutePath()));
            throw new HidGenerationException(String.format("HID Storage path %s does not exists ", hidFile.getAbsolutePath()));
        }
    }

    private boolean hidStoragePathExists(File hidFile) {
        File parentFile = hidFile.getParentFile();
        return parentFile.exists();
    }

    public synchronized List<MciHealthId> getNextBlock(final String mciCode, Integer blockSize) {
        List<MciHealthId> mciHealthIds = healthIdRepository.getNextBlock(blockSize);
        if (CollectionUtils.isEmpty(mciHealthIds)) throw new HealthIdExhaustedException();
        List<OrgHealthId> orgHealthIds = new ArrayList<>();
        UUID generatedAt = timeBased();
        for (MciHealthId mciHealthId : mciHealthIds) {
            orgHealthIds.add(new OrgHealthId(mciHealthId.getHid(), mciCode, generatedAt));
        }
        healthIdRepository.saveOrgHidAndDeleteMciHid(mciHealthIds, orgHealthIds);
        return mciHealthIds;
    }

    public Observable<Boolean> markOrgHealthIdUsed(String healthId, final UUID usedAt) {
        Observable<OrgHealthId> orgHealthId = healthIdRepository.findOrgHealthId(healthId);
        return orgHealthId.concatMap(new Func1<OrgHealthId, Observable<Boolean>>() {
            @Override
            public Observable<Boolean> call(OrgHealthId orgHealthId) {
                if (null == orgHealthId) {
                    return Observable.error(new HealthIdNotFoundException("Health Id not allocated to any Organization."));
                }
                orgHealthId.markUsed(usedAt);
                logger.debug(String.format("Marking %s used for Organization %s", orgHealthId.getHealthId(), orgHealthId.getAllocatedFor()));
                return healthIdRepository.saveOrUpdateOrgHealthId(orgHealthId);
            }
        });
    }

    private long saveIfValidMciHID(long numberOfValidHids, long currentNumber) {
        String possibleHid = String.valueOf(currentNumber);
        if (!mciInvalidHidPattern.matcher(possibleHid).find()) {
            numberOfValidHids += 1;
            String newHealthId = possibleHid + checksumGenerator.generate(possibleHid.substring(1));
            healthIdRepository.saveMciHealthId(new MciHealthId(newHealthId));
        }
        return numberOfValidHids;
    }

    private long saveIfValidOrgHID(String orgCode, long numberOfValidHIDs, File hidFile, long possibleHID, UUID generatedAt) {
        String possibleHid = String.valueOf(possibleHID);
        if (!orgInvalidHidPattern.matcher(possibleHid).find()) {
            String newHealthId = possibleHid + checksumGenerator.generate(possibleHid.substring(1));
            if (shouldSaveHID(newHealthId)) {
                try {
                    numberOfValidHIDs += 1;
                    FileUtil.addHidToFile(hidFile, newHealthId);
                    logger.debug(String.format("Saving healthid %s used for Organization %s", newHealthId, orgCode));
                    healthIdRepository.saveOrUpdateOrgHealthId(new OrgHealthId(newHealthId, orgCode, generatedAt)).toBlocking().first();
                } catch (IOException e) {
                    logger.info(e.getMessage(), e);
                    throw new HidGenerationException(e.getMessage());
                }
            }
        }
        return numberOfValidHIDs;
    }

    private boolean shouldSaveHID(String newHealthId) {
        return healthIdRepository.findOrgHealthId(newHealthId).toBlocking().first() == null;
    }

    private File createFileForOrg(String orgCode) {
        String hidStorageDirPath = healthIdProperties.getHidStoragePath();
        if (StringUtils.isBlank(hidStorageDirPath)) {
            hidStorageDirPath = DEFAULT_HID_STORAGE_PATH;
        }
        String fileName = String.format("%s-%s", orgCode, toDateString(new Date(), SIMPLE_DATE_WITH_SECS_FORMAT));
        return FileUtil.createHIDFile(hidStorageDirPath, fileName);
    }

    private GeneratedHIDBlock saveGeneratedBlock(Long start, Long end, Long numberOfValidHids, String orgCode, UserInfo userInfo, UUID generatedAt) {
        long seriesNo = identifySeriesNo(start);
        RequesterDetails requesterDetails = getRequesterDetails(userInfo);
        GeneratedHIDBlock generatedHIDBlock = new GeneratedHIDBlock(seriesNo, orgCode, start, end, numberOfValidHids, writeValueAsString(requesterDetails), generatedAt);
        if (numberOfValidHids > 0) {
            generatedHidBlockService.saveGeneratedHidBlock(generatedHIDBlock);
        }
        return generatedHIDBlock;
    }

    private RequesterDetails getRequesterDetails(UserInfo userInfo) {
        UserInfo.UserInfoProperties properties = userInfo.getProperties();
        return new RequesterDetails(properties.getId());
    }

    private boolean isPartOfSeries(long seriesNo, long possibleHID) {
        return identifySeriesNo(possibleHID) == seriesNo;
    }

    private long identifyStartInSeries(long seriesNo) {
        long endsAt = 0L;
        List<GeneratedHIDBlock> preGeneratedHIDBlocks = generatedHidBlockService.getPreGeneratedHidBlocks(seriesNo);
        if (CollectionUtils.isEmpty(preGeneratedHIDBlocks)) {
            return seriesNo;
        }
        for (GeneratedHIDBlock preGeneratedHIDBlock : preGeneratedHIDBlocks) {
            if (endsAt < preGeneratedHIDBlock.getEndsAt()) {
                endsAt = preGeneratedHIDBlock.getEndsAt();
            }
        }
        return endsAt + 1;
    }

    private long identifySeriesNo(Long start) {
        String startAsText = String.valueOf(start);
        String startPrefix = startAsText.substring(0, DIGITS_FOR_BLOCK_SEPARATION);
        String startSuffix = startAsText.substring(DIGITS_FOR_BLOCK_SEPARATION, startAsText.length());
        return Long.parseLong(String.valueOf(startPrefix + startSuffix.replaceAll(".", "0")));
    }

    public Observable<OrgHealthId> findOrgHealthId(String healthId) {
        return healthIdRepository.findOrgHealthId(healthId);
    }

    public Observable<Integer> findRemainingHIDs() {
        int threshold = healthIdProperties.getHealthIdWarningThreshold();
        return healthIdRepository.findRemainingHIDs(threshold);
    }
}
