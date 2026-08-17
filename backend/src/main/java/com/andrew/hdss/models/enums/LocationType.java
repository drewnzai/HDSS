package com.andrew.hdss.models.enums;

public enum LocationType {
    COUNTRY(0),
    COUNTY(1),
    SUB_COUNTY(2),
    DIVISION(3),
    LOCATION(4),
    SUB_LOCATION(5);

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