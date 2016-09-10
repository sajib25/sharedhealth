package org.sharedhealth.healthId.web.utils;

import org.springframework.data.cassandra.core.CassandraOperations;

import java.util.List;

import static java.util.Arrays.asList;
import static org.sharedhealth.healthId.web.repository.RepositoryConstants.*;

public class TestUtil {

    public static void truncateAllColumnFamilies(CassandraOperations cassandraOps) {
        List<String> cfs = getAllColumnFamilies();
        for (String cf : cfs) {
            cassandraOps.execute("truncate " + cf);
        }
    }

    private static List<String> getAllColumnFamilies() {
        return asList(
                CF_MCI_HEALTH_ID,
                CF_GENERATED_HID_BLOCKS,
                CF_ORG_HEALTH_ID
        );
    }


}
