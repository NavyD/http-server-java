package xyz.navyd.http.enums;

import java.util.Optional;

public enum VersionEnum {
    HTTP1_0("HTTP/1.0"),
    HTTP1_1("HTTP/1.1"),
    
    ;
    
    private String value;

    private VersionEnum(String val) {
        this.value = val;
    }

    public String getValue() {
        return value;
    }

    public static Optional<VersionEnum> parse(String value) {
        for (VersionEnum v : values()) {
            if (v.value.equalsIgnoreCase(value.toLowerCase())) {
                return Optional.of(v);
            }
        }
        return Optional.empty();
    }
}