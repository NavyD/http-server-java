package xyz.navyd.http.enums;

import java.util.Optional;

public enum MethodEnum {

    GET("GET"), 
    POST("POST"), 
    HEAD("HEAD"), // TODO HEAD method
    PUT("PUT"), // TODO PUT method
    DELETE("DELETE"), // TODO DELETE method
    TRACE("TRACE"), // TODO TRACE method
    CONNECT("CONNECT"), // TODO CONNECT method
    UNRECOGNIZED(null);

    private String value;

    MethodEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Optional<MethodEnum> parse(String value) {
        for (MethodEnum m : MethodEnum.values()) {
            if (m.getValue().equalsIgnoreCase(value))
                return Optional.of(m);
        }
        return Optional.empty();
    }
}