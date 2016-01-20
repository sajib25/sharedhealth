package org.sharedhealth.healthId.web.repository;


import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import org.sharedhealth.healthId.web.Model.GeneratedHIDBlock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static org.sharedhealth.healthId.web.repository.RepositoryConstants.CF_GENERATED_HID_BLOCKS;
import static org.sharedhealth.healthId.web.repository.RepositoryConstants.SERIES_NO;

@Component
public class GeneratedHidBlockRepository extends BaseRepository {

    @Autowired
    public GeneratedHidBlockRepository(@Qualifier("HealthIdCassandraTemplate") CassandraOperations cassandraOps) {
        super(cassandraOps);
    }

    public List<GeneratedHIDBlock> getPreGeneratedHidBlocks(long seriesNo) {
        Select selectHIDBlocks = QueryBuilder.select().from(CF_GENERATED_HID_BLOCKS);
        selectHIDBlocks.where(eq(SERIES_NO, seriesNo));
        return cassandraOps.select(selectHIDBlocks, GeneratedHIDBlock.class);
    }

    public GeneratedHIDBlock saveGeneratedHidBlock(GeneratedHIDBlock generatedHIDBlock) {
        return cassandraOps.insert(generatedHIDBlock);
    }
}
