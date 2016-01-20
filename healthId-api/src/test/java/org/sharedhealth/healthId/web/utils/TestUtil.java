package org.sharedhealth.healthId.web.utils;

import com.datastax.driver.core.utils.UUIDs;
import org.springframework.data.cassandra.core.CassandraOperations;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.Collections.addAll;
import static java.util.Collections.unmodifiableList;
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
                CF_GENERATED_HID_RANGE,
                CF_GENERATED_HID_BLOCKS,
                CF_ORG_HEALTH_ID
        );
    }

    public static Set<String> asSet(String... values) {
        Set<String> set = new HashSet<>();
        addAll(set, values);
        return set;
    }


    public static List<UUID> buildTimeUuids() throws InterruptedException {
        List<UUID> timeUuids = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            timeUuids.add(UUIDs.timeBased());
            Thread.sleep(1);
        }
        return unmodifiableList(timeUuids);
    }
}
