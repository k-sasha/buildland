package com.sasha.buildland.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum Status {
    READY_FOR_SALE("Ready for sale"),
    REPAIRS_NEEDED("Repairs needed"),
    RENTED("Rented"),
    SOLD("Sold"),
    SOLD_RETURNED_FOR_REPAIR("Sold returned for repair");

    private final String displayName;

    Status(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static List<String> getDisplayNames() {
        return Arrays.stream(Status.values())
                .map(Status::getDisplayName)
                .collect(Collectors.toList());
    }

    public static Status fromDisplayName(String displayName) {
        for (Status status : Status.values()) {
            if (status.getDisplayName().equals(displayName)) {
                return status;
            }
        }
        throw new IllegalArgumentException("No enum constant for display name: " + displayName);
    }
}
