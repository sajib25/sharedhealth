package org.sharedhealth.healthId.web.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.healthId.web.Model.MciHealthId;
import org.sharedhealth.healthId.web.config.EnvironmentMock;
import org.sharedhealth.healthId.web.launch.WebMvcConfig;
import org.sharedhealth.healthId.web.repository.HealthIdRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(initializers = EnvironmentMock.class, classes = WebMvcConfig.class)
public class HealthIdServiceIT {
    private static final Logger logger = LoggerFactory.getLogger(HealthIdServiceIT.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")
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

    @Ignore
    @Test
    public void shouldGenerateUniqueBlock() throws Exception {
        createHealthIds(9800000000L);
        ExecutorService executor = Executors.newFixedThreadPool(5);
        final Set<Future<List<MciHealthId>>> eventualHealthIds = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            Callable<List<MciHealthId>> nextBlock = new Callable<List<MciHealthId>>() {
                @Override
                public List<MciHealthId> call() throws Exception {
                    return healthIdService.getNextBlock(2);
                }
            };
            Future<List<MciHealthId>> eventualHealthId = executor.submit(nextBlock);
            eventualHealthIds.add(eventualHealthId);
        }
        Set<MciHealthId> uniqueMciHealthIds = new HashSet<>();

        for (Future<List<MciHealthId>> eventualHealthId : eventualHealthIds) {
            uniqueMciHealthIds.addAll(eventualHealthId.get());
        }
        assertEquals(200, uniqueMciHealthIds.size());
    }

    private void createHealthIds(long prefix) {
        logger.debug("generating health Id for test");
        for (int i = 0; i < 200; i++) {
            healthIdRepository.saveMciHealthIdSync(new MciHealthId(String.valueOf(prefix + i)));
        }
    }
}