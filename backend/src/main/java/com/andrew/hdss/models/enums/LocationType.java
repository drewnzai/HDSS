package com.andrew.hdss.models.enums;

public enum LocationType {
    COUNTRY(0),
    COUNTY(1),
    SUB_COUNTY(2),
    LOCATION(3),
    SUB_LOCATION(4);

    private final int level;

    LocationType(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public LocationType parentType() {
        if (level == 0) return null;
        return values()[level - 1];
    }
}