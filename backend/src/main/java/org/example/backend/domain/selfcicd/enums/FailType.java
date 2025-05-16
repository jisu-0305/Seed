package org.example.backend.domain.selfcicd.enums;

public enum FailType {
    BUILD,
    RUNTIME;

    public static FailType from(String value) {
        for (FailType type : values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown fail type: " + value);
    }
}
