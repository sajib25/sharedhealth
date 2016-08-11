package org.sharedhealth.healthId.web.service;

import org.apache.commons.collections.CollectionUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.healthId.web.Model.MciHealthId;
import org.sharedhealth.healthId.web.config.EnvironmentMock;
import org.sharedhealth.healthId.web.launch.WebMvcConfig;
import org.sharedhealth.healthId.web.repository.HealthIdRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

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

    @Before
    public void setUp() throws ExecutionException, InterruptedException {
        cqlTemplate.execute("truncate mci_healthId");
        healthIdRepository.resetLastReservedHealthId();
    }

    @After
    public void tearDown() throws Exception {
        cqlTemplate.execute("truncate mci_healthId");
    }

    @Test
    public void shouldGenerateUniqueBlock() throws Exception {
        createHealthIds(9800000000L);
        List<MciHealthId> mciHealthIds = healthIdService.getNextBlock("MCI1", 10);
        List<MciHealthId> mciHealthIds2 = healthIdService.getNextBlock("MCI2", 20);
        Collection intersection = CollectionUtils.intersection(mciHealthIds, mciHealthIds2);
        assertTrue(CollectionUtils.isEmpty(intersection));
    }

    private void createHealthIds(long prefix) {
        for (int i = 0; i < 200; i++) {
            MciHealthId mciHealthId = new MciHealthId(String.valueOf(prefix + i));
            healthIdRepository.saveMciHealthId(mciHealthId).toBlocking().last();
        }
    }
}