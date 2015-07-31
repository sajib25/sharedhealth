package org.sharedhealth.healthId.web.Model;

import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

import static org.sharedhealth.healthId.web.repository.RepositoryConstants.CF_MCI_HEALTH_ID;
import static org.sharedhealth.healthId.web.repository.RepositoryConstants.HID;
import static org.springframework.cassandra.core.PrimaryKeyType.PARTITIONED;

@Table(value = CF_MCI_HEALTH_ID)
public class MciHealthId {
    public static MciHealthId NULL_HID = new MciHealthId("00000000000");

    @PrimaryKeyColumn(name = HID, ordinal = 0, type = PARTITIONED)
    private String hid;

    public MciHealthId(String hid) {
        this.hid = hid;
    }

    public String getHid() {
        return hid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MciHealthId)) return false;

        MciHealthId MciHealthId = (MciHealthId) o;

        if (!hid.equals(MciHealthId.hid)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return hid.hashCode();
    }
}