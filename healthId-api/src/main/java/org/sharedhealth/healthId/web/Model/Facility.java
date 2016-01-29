package org.sharedhealth.healthId.web.Model;


public class Facility {

    private String id;

    private String name;


    public Facility(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Facility)) return false;

        Facility facility = (Facility) o;

        if (id != null ? !id.equals(facility.id) : facility.id != null) return false;
        if (name != null ? !name.equals(facility.name) : facility.name != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}