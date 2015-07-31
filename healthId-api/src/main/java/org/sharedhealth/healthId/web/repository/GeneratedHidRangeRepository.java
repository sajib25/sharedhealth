package org.sharedhealth.healthId.web.repository;


import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import org.sharedhealth.healthId.web.Model.GeneratedHidRange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.sharedhealth.healthId.web.repository.RepositoryConstants.CF_GENERATED_HID_RANGE;

@Component
public class GeneratedHidRangeRepository extends BaseRepository {

    @Autowired
    public GeneratedHidRangeRepository(@Qualifier("MCICassandraTemplate") CassandraOperations cassandraOps) {
        super(cassandraOps);
    }

    public List<GeneratedHidRange> getPreGeneratedHidRanges() {
        Select from = QueryBuilder.select().from(CF_GENERATED_HID_RANGE);
        return cassandraOps.select(from, GeneratedHidRange.class);
    }

    public GeneratedHidRange saveGeneratedHidRange(GeneratedHidRange generatedHidRange) {
        return cassandraOps.insert(generatedHidRange);
    }
}
