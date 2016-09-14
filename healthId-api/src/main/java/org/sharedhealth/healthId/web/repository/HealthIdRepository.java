package org.sharedhealth.healthId.web.repository;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
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
import rx.functions.Func1;

import java.util.List;
import java.util.UUID;

import static org.sharedhealth.healthId.web.repository.RepositoryConstants.*;
import static org.springframework.data.cassandra.core.CassandraTemplate.createDeleteQuery;
import static org.springframework.data.cassandra.core.CassandraTemplate.createInsertQuery;

@Component
public class HealthIdRepository extends BaseRepository {
    private static final Logger logger = LoggerFactory.getLogger(HealthIdRepository.class);

    @Autowired
    public HealthIdRepository(@Qualifier("HealthIdCassandraTemplate") CassandraOperations cassandraOps) {
        super(cassandraOps);
    }

    public Observable<ResultSet> saveMciHealthId(MciHealthId mciHealthId) {
        logger.debug(String.format("Inserting new hid for MCI :%s", mciHealthId.getHid()));
        Insert insertQuery = getInsertQuery(mciHealthId);
        return Observable.from(cassandraOps.executeAsynchronously(insertQuery.ifNotExists()));
    }

    public Observable<Boolean> saveOrUpdateOrgHealthId(OrgHealthId orgHealthId) {
        logger.debug(String.format("Marking %s used for Organization %s", orgHealthId.getHealthId(), orgHealthId.getAllocatedFor()));
        Insert insertQuery = getInsertQuery(orgHealthId);
        return Observable.from(cassandraOps.executeAsynchronously(insertQuery)).flatMap(
                RxMaps.respondOnNext(true),
                RxMaps.<Boolean>forwardError(), RxMaps.<Boolean>completeResponds());
    }

    public List<MciHealthId> getNextBlock(int blockSize) {
        logger.debug(String.format("Getting next block of size : %d", blockSize));
        Select.Where from = QueryBuilder.select().from(CF_MCI_HEALTH_ID).where();
        Select nextBlockQuery = from.limit(blockSize);

        return cassandraOps.select(nextBlockQuery, MciHealthId.class);
    }

    private Insert getInsertQuery(MciHealthId mciHealthId) {
        return createInsertQuery(CF_MCI_HEALTH_ID, mciHealthId, null, cassandraOps.getConverter());
    }

    private Insert getInsertQuery(OrgHealthId orgHealthId) {
        return createInsertQuery(CF_ORG_HEALTH_ID, orgHealthId, null, cassandraOps.getConverter());
    }

    private Delete getDeleteQuery(MciHealthId mciHealthId) {
        return createDeleteQuery(CF_MCI_HEALTH_ID, mciHealthId, null, cassandraOps.getConverter());
    }

    public Observable<OrgHealthId> findOrgHealthId(String healthId) {
        Select selectHealthId = QueryBuilder.select().from(CF_ORG_HEALTH_ID).where(QueryBuilder.eq(HEALTH_ID, healthId)).limit(1);
        return Observable.from(cassandraOps.executeAsynchronously(selectHealthId)).flatMap(
                new Func1<ResultSet, Observable<OrgHealthId>>() {
                    @Override
                    public Observable<OrgHealthId> call(ResultSet resultSet) {
                        if (resultSet.isExhausted()) {
                            return Observable.just(null);
                        }
                        Row one = resultSet.one();
                        String healthId = one.getString("health_id");
                        String allocatedFor = one.getString("allocated_for");
                        UUID generatedAt = one.getUUID("generated_at");
                        Boolean isUsed = one.getBool("is_used");
                        UUID usedAt = one.getUUID("used_at");

                        return Observable.just(new OrgHealthId(healthId, allocatedFor, generatedAt, isUsed, usedAt));
                    }
                });
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
