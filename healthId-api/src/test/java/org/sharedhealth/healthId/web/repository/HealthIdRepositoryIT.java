package org.sharedhealth.healthId.web.repository;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.healthId.web.Model.MciHealthId;
import org.sharedhealth.healthId.web.Model.OrgHealthId;
import org.sharedhealth.healthId.web.config.EnvironmentMock;
import org.sharedhealth.healthId.web.exception.HealthIdExhaustedException;
import org.sharedhealth.healthId.web.launch.WebMvcConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(initializers = EnvironmentMock.class, classes = WebMvcConfig.class)
public class HealthIdRepositoryIT {

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    @Qualifier("HealthIdCassandraTemplate")
    private CassandraOperations cqlTemplate;

    @Autowired
    private HealthIdRepository healthIdRepository;

    @Before
    public void setUp() throws ExecutionException, InterruptedException {
        cqlTemplate.execute("truncate mci_healthId");
        healthIdRepository.resetLastReservedHealthId();
    }

    @After
    public void tearDown() {
        cqlTemplate.execute("truncate mci_healthId");
    }

    private List<MciHealthId> createHealthIds(long prefix) {
        List<MciHealthId> MciHealthIds = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            MciHealthIds.add(healthIdRepository.saveMciHealthIdSync(new MciHealthId(String.valueOf(prefix + i))));
        }
        return MciHealthIds;
    }

    @Test(expected = HealthIdExhaustedException.class)
    public void shouldGetExceptionIfIdsAreNotGeneratedBeforeFetch() throws ExecutionException, InterruptedException {
        healthIdRepository.getNextBlock();
    }

    @Test(expected = HealthIdExhaustedException.class)
    public void shouldGetExceptionIfIdsAreExhausted() throws ExecutionException, InterruptedException {
        long prefix = 98190001231L;
        createHealthIds(prefix);
        healthIdRepository.getNextBlock();
        healthIdRepository.getNextBlock();
    }

    @Test
    public void shouldGetBlockFirstTime() throws ExecutionException, InterruptedException {
        long prefix = 98190001231L;
        createHealthIds(prefix);
        assertNotNull(healthIdRepository.getNextBlock(2));
    }

    @Test
    public void shouldRemoveUsedHid() throws ExecutionException, InterruptedException {
        long prefix = 98190001231L;
        createHealthIds(prefix);
        List<MciHealthId> nextBlock = healthIdRepository.getNextBlock(2);
        MciHealthId MciHealthId = nextBlock.remove(0);
        healthIdRepository.removeUsedHidSync(MciHealthId);
        MciHealthId id = healthIdRepository.getHealthId(MciHealthId.getHid());
        assertNull(id);
    }

    @Test
    public void shouldGetANewBlockEveryTime() throws ExecutionException, InterruptedException {
        long prefix = 98190001231L;
        createHealthIds(prefix);
        List<MciHealthId> MciHealthIds = healthIdRepository.getNextBlock(2);
        String lastReservedHealthId = healthIdRepository.getLastTakenHidMarker();
        assertFalse(lastReservedHealthId == null);
        List<MciHealthId> nextBlock = healthIdRepository.getNextBlock(2);
        assertFalse(lastReservedHealthId == healthIdRepository.getLastTakenHidMarker());
        for (MciHealthId MciHealthId : nextBlock) {
            assertFalse(MciHealthIds.contains(MciHealthId));
        }
    }

    @Test
    public void shouldSaveAHIDForGivenOrganization() throws Exception {
        OrgHealthId orgHealthId = new OrgHealthId("9110", "OTHER-ORG", null);

        healthIdRepository.saveOrgHealthIdSync(orgHealthId);

        String select = select().all().from(RepositoryConstants.CF_ORG_HEALTH_ID).toString();
        List<OrgHealthId> insertedHIDs = cqlTemplate.select(select, OrgHealthId.class);
        assertEquals(1, insertedHIDs.size());
        assertEquals(orgHealthId, insertedHIDs.get(0));
    }

    @Test
    public void shouldFindOrgHIDByGivenHID() throws Exception {
        OrgHealthId hid = new OrgHealthId("1234", "XYZ", null);
        cqlTemplate.insert(asList(hid, new OrgHealthId("1134", "ABC", null)));

        OrgHealthId orgHealthId = healthIdRepository.findOrgHealthId("1234");
        assertEquals(hid, orgHealthId);
    }
}

