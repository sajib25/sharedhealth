package org.sharedhealth.healthId.web.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static org.springframework.util.CollectionUtils.isEmpty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserInfo {
    public static final String ROLE_PREFIX = "ROLE_";
    public static final String FACILITY_GROUP = ROLE_PREFIX + "FACILITY";
    public static final String MCI_USER_GROUP = ROLE_PREFIX + "MCI User";
    public static final String PROVIDER_GROUP = ROLE_PREFIX + "PROVIDER";
    public static final String PATIENT_GROUP = ROLE_PREFIX + "PATIENT";
    public static final String FACILITY_ADMIN_GROUP = ROLE_PREFIX + "Facility Admin";
    public static final String MCI_ADMIN = ROLE_PREFIX + "MCI Admin";
    public static final String MCI_APPROVER = ROLE_PREFIX + "MCI Approver";

    public static final String SHR_SYSTEM_ADMIN_GROUP = ROLE_PREFIX + "SHR System Admin";
    public static final String HRM_SHR_SYSTEM_ADMIN_GROUP = "SHR System Admin";

    @JsonProperty("id")
    private String id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("email")
    private String email;
    @JsonProperty("is_active")
    private int isActive;
    @JsonProperty("activated")
    private boolean activated;
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("groups")
    private List<String> groups;
    @JsonProperty("profiles")
    private List<UserProfile> userProfiles;
    @JsonIgnore
    private List<String> userGroups;

    private UserInfoProperties instance;

    public UserInfo(String id, String name, String email, int isActive, boolean activated, String accessToken, List<String> groups, List<UserProfile> userProfiles) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.isActive = isActive;
        this.activated = activated;
        this.accessToken = accessToken;
        this.groups = groups;
        this.userProfiles = userProfiles;
        this.userGroups = new ArrayList<>();
    }

    public UserInfo() {
        this.userGroups = new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserInfo)) return false;

        UserInfo userInfo = (UserInfo) o;

        if (activated != userInfo.activated) return false;
        if (isActive != userInfo.isActive) return false;
        if (!accessToken.equals(userInfo.accessToken)) return false;
        if (!email.equals(userInfo.email)) return false;
        if (groups != null ? !groups.equals(userInfo.groups) : userInfo.groups != null) return false;
        if (!id.equals(userInfo.id)) return false;
        if (name != null ? !name.equals(userInfo.name) : userInfo.name != null) return false;
        if (userProfiles != null ? !userProfiles.equals(userInfo.userProfiles) : userInfo.userProfiles != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + email.hashCode();
        result = 31 * result + isActive;
        result = 31 * result + (activated ? 1 : 0);
        result = 31 * result + accessToken.hashCode();
        result = 31 * result + (groups != null ? groups.hashCode() : 0);
        result = 31 * result + (userProfiles != null ? userProfiles.hashCode() : 0);
        return result;
    }

    public UserInfoProperties getProperties() {
        if (null == instance) {
            instance = new UserInfoProperties();
        }
        return instance;
    }

    public class UserInfoProperties {
        private boolean isShrSystemAdmin;
        private String facilityId;
        private String providerId;
        private String patientHid;
        private String adminId;

        public UserInfoProperties() {
            loadUserProperties();
        }

        public String getName() {
            return name;
        }

        public List<String> getUserGroups() {
            return userGroups;
        }

        public List<String> getGroups() {
            return groups;
        }

        public String getId() {
            return id;
        }

        public List<UserProfile> getUserProfiles() {
            return userProfiles;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public boolean isActivated() {
            return activated;
        }

        public int getIsActive() {
            return isActive;
        }

        public String getEmail() {
            return email;
        }

        public String getFacilityId() {
            return facilityId;
        }

        public String getProviderId() {
            return providerId;
        }

        public String getPatientHid() {
            return patientHid;
        }

        public String getAdminId() {
            return adminId;
        }

        private void loadUserProperties() {
            if (containsCaseInsensitive(groups, HRM_SHR_SYSTEM_ADMIN_GROUP)){
                userGroups.add(SHR_SYSTEM_ADMIN_GROUP);
            }
            if (containsCaseInsensitive(groups, HRM_SHR_SYSTEM_ADMIN_GROUP)) {
                isShrSystemAdmin = true;
            }
        }

        private boolean containsCaseInsensitive(List<String> groups, String role) {
            for (String groupMember : groups) {
                if (groupMember.equalsIgnoreCase(role)) {
                    return true;
                }
            }
            return false;
        }
    }
}
