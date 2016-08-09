package org.sharedhealth.healthId.web.repository;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.querybuilder.*;
import org.sharedhealth.healthId.web.Model.MciHealthId;
import org.sharedhealth.healthId.web.Model.OrgHealthId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Component;
import rx.Observable;

import java.util.List;

import static org.sharedhealth.healthId.web.repository.RepositoryConstants.*;
import static org.springframework.data.cassandra.core.CassandraTemplate.createDeleteQuery;
import static org.springframework.data.cassandra.core.CassandraTemplate.createInsertQuery;

@Component
public class HealthIdRepository extends BaseRepository {
    private static final Logger logger = LoggerFactory.getLogger(HealthIdRepository.class);
    public static final int BLOCK_SIZE = 10000;
    private String lastTakenHidMarker;

    @Autowired
    public HealthIdRepository(@Qualifier("HealthIdCassandraTemplate") CassandraOperations cassandraOps) {
        super(cassandraOps);
    }

    public Observable<ResultSet> saveMciHealthId(MciHealthId mciHealthId) {
        logger.debug(String.format("Inserting new hid for MCI :%s", mciHealthId.getHid()));
        Insert insertQuery = getInsertQuery(mciHealthId);
        return Observable.from(cassandraOps.executeAsynchronously(insertQuery.ifNotExists()));
    }

    public void saveOrgHealthId(OrgHealthId orgHealthId) {
        logger.debug(String.format("Assigining %s Health Id for Organization %s", orgHealthId.getHealthId(), orgHealthId.getAllocatedFor()));
        Insert insertQuery = getInsertQuery(orgHealthId);
        Observable.from(cassandraOps.executeAsynchronously(insertQuery)).toBlocking().first();
    }

    public List<MciHealthId> getNextBlock(int blockSize) {
        logger.debug(String.format("Getting next block of size : %d", blockSize));
        Select.Where from = QueryBuilder.select().from(CF_MCI_HEALTH_ID).where();
        Select nextBlockQuery = from.limit(blockSize);

        return cassandraOps.select(nextBlockQuery, MciHealthId.class);
    }

    public void resetLastReservedHealthId() {
        lastTakenHidMarker = null;
    }

    @Deprecated
    public void removedUsedHid(MciHealthId nextMciHealthId) {
//        cassandraOps.deleteAsynchronously(nextMciHealthId);
    }

//    public void deleteHealthBlock(List<MciHealthId> allocatedHID) {
//
//        cassandraOps.delete(allocatedHID);
//    }

    private Insert getInsertQuery(MciHealthId mciHealthId) {
        return createInsertQuery(CF_MCI_HEALTH_ID, mciHealthId, null, cassandraOps.getConverter());
    }

    private Insert getInsertQuery(OrgHealthId orgHealthId) {
        return createInsertQuery(CF_ORG_HEALTH_ID, orgHealthId, null, cassandraOps.getConverter());
    }

    private Delete getDeleteQuery(MciHealthId mciHealthId) {
        return createDeleteQuery(CF_MCI_HEALTH_ID, mciHealthId, null, cassandraOps.getConverter());
    }

    public OrgHealthId findOrgHealthId(String healthId) {
        Select selectHealthId = QueryBuilder.select().from(CF_ORG_HEALTH_ID).where(QueryBuilder.eq(HEALTH_ID, healthId)).limit(1);
        List<OrgHealthId> orgHealthIds = cassandraOps.select(selectHealthId, OrgHealthId.class);
        return orgHealthIds.isEmpty() ? null : orgHealthIds.get(0);
    }

    public void saveOrgHidAndDeleteMciHid(final List<MciHealthId> mciHealthId, final List<OrgHealthId> orgHealthId) {
        Batch batch = QueryBuilder.batch();
        for (OrgHealthId orgHid : orgHealthId) {
            batch.add(getInsertQuery(orgHid));
        }
        for (MciHealthId healthId : mciHealthId) {
            batch.add(getDeleteQuery(healthId));
        }
        cassandraOps.execute(batch);
    }
}
