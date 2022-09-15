package org.sharedhealth.healthId.web.Model;

public enum IdentityType {

    NID(1), PASSPORT(2), DRIVINGLICENSE(3),BIRTHCERTIFICATE(4),OTHERS(5);

    private int numVal;

    IdentityType(int numVal) {
        this.numVal = numVal;
    }

    public int getNumVal() {
        return numVal;
    }
}
