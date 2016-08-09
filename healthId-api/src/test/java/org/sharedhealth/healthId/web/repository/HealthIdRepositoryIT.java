package org.sharedhealth.healthId.web.repository;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.healthId.web.Model.MciHealthId;
import org.sharedhealth.healthId.web.Model.OrgHealthId;
import org.sharedhealth.healthId.web.config.EnvironmentMock;
import org.sharedhealth.healthId.web.exception.HealthIdExhaustedException;
import org.sharedhealth.healthId.web.launch.WebMvcConfig;
import org.sharedhealth.healthId.web.utils.TestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import rx.Observable;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static com.datastax.driver.core.utils.UUIDs.timeBased;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.sharedhealth.healthId.web.repository.RepositoryConstants.CF_MCI_HEALTH_ID;
import static org.sharedhealth.healthId.web.repository.RepositoryConstants.HID;

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
        TestUtil.truncateAllColumnFamilies(cqlTemplate);
    }

    private void createHealthIds(long prefix) {
        for (int i = 0; i < 10; i++) {
            MciHealthId mciHealthId = new MciHealthId(String.valueOf(prefix + i));
            healthIdRepository.saveMciHealthId(mciHealthId).toBlocking().first();
        }
    }

    @Test
    public void shouldGetBlock() throws ExecutionException, InterruptedException {
        long prefix = 98190001231L;
        createHealthIds(prefix);
        List<MciHealthId> nextBlock = healthIdRepository.getNextBlock(2);
        assertNotNull(nextBlock);
    }

    @Test
    public void shouldDeleteAHIDBlockFromMciHIDAndAddItToOrgHID() throws Exception {
        long prefix = 98190001231L;
        createHealthIds(prefix);
        List<MciHealthId> nextBlock = healthIdRepository.getNextBlock(2);
        MciHealthId mciHealthId = nextBlock.get(0);
        String hid = mciHealthId.getHid();
        OrgHealthId orgHealthId = new OrgHealthId(hid, "MCI", null, null);
        healthIdRepository.saveOrgHidAndDeleteMciHid(asList(mciHealthId), asList(orgHealthId));
        assertNull(getHealthId(hid));
        assertNotNull(healthIdRepository.findOrgHealthId(hid));
    }

    @Test
    public void shouldSaveAHIDForGivenOrganization() throws Exception {
        OrgHealthId orgHealthId = new OrgHealthId("9110", "OTHER-ORG", timeBased(), null);

        healthIdRepository.saveOrgHealthId(orgHealthId);

        String select = select().all().from(RepositoryConstants.CF_ORG_HEALTH_ID).toString();
        List<OrgHealthId> insertedHIDs = cqlTemplate.select(select, OrgHealthId.class);
        assertEquals(1, insertedHIDs.size());
        assertEquals(orgHealthId, insertedHIDs.get(0));
    }

    @Test
    public void shouldFindOrgHIDByGivenHID() throws Exception {
        OrgHealthId hid = new OrgHealthId("1234", "XYZ", timeBased(), null);
        cqlTemplate.insert(asList(hid, new OrgHealthId("1134", "ABC", timeBased(), null)));

        OrgHealthId orgHealthId = healthIdRepository.findOrgHealthId("1234");
        assertEquals(hid, orgHealthId);
    }

    public MciHealthId getHealthId(String hid) {
        Select selectHealthId = QueryBuilder.select().from(CF_MCI_HEALTH_ID).where(QueryBuilder.eq(HID, hid)).limit(1);
        List<MciHealthId> mciHealthIds = cqlTemplate.select(selectHealthId, MciHealthId.class);
        return mciHealthIds.isEmpty() ? null : mciHealthIds.get(0);
    }
}

