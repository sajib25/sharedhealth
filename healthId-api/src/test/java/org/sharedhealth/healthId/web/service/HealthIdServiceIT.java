package org.sharedhealth.healthId.web.service;

import org.apache.commons.collections.CollectionUtils;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.healthId.web.Model.MciHealthId;
import org.sharedhealth.healthId.web.Model.OrgHealthId;
import org.sharedhealth.healthId.web.config.EnvironmentMock;
import org.sharedhealth.healthId.web.launch.WebMvcConfig;
import org.sharedhealth.healthId.web.repository.HealthIdRepository;
import org.sharedhealth.healthId.web.utils.TestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static com.datastax.driver.core.utils.UUIDs.timeBased;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(initializers = EnvironmentMock.class, classes = WebMvcConfig.class)
public class HealthIdServiceIT {

    @Autowired
    @Qualifier("HealthIdCassandraTemplate")
    private CassandraOperations cqlTemplate;

    @Autowired
    private HealthIdRepository healthIdRepository;

    @Autowired
    private HealthIdService healthIdService;

    @After
    public void tearDown() throws Exception {
        TestUtil.truncateAllColumnFamilies(cqlTemplate);
    }

    @Test
    public void shouldGenerateUniqueBlock() throws Exception {
        createHealthIds(9800000000L, 50);
        List<MciHealthId> mciHealthIds = healthIdService.getNextBlock("MCI1", 10);
        List<MciHealthId> mciHealthIds2 = healthIdService.getNextBlock("MCI2", 20);
        Collection intersection = CollectionUtils.intersection(mciHealthIds, mciHealthIds2);
        assertTrue(CollectionUtils.isEmpty(intersection));
    }

    @Test
    public void shouldMarkHealthIdUsed() throws Exception {
        String healthId = "9800000000L";
        String orgCode = "mci1";
        createOrgHealthIds(healthId, orgCode);
        UUID usedAt = timeBased();
        healthIdService.markOrgHealthIdUsed(healthId, usedAt).toBlocking().last();

        OrgHealthId savedOrgHealthId = healthIdRepository.findOrgHealthId(healthId).toBlocking().first();
        assertTrue(savedOrgHealthId.isUsed());
        assertEquals(usedAt, savedOrgHealthId.getUsedAt());
    }

    private void createHealthIds(long prefix, int numberOfHids) {
        for (int i = 0; i < numberOfHids; i++) {
            MciHealthId mciHealthId = new MciHealthId(String.valueOf(prefix + i));
            healthIdRepository.saveMciHealthId(mciHealthId).toBlocking().last();
        }
    }

    private OrgHealthId createOrgHealthIds(String healthId, String orgCode) {
        OrgHealthId org = new OrgHealthId(healthId, orgCode, timeBased());
        healthIdRepository.saveOrUpdateOrgHealthId(org).toBlocking().first();
        return org;
    }
}