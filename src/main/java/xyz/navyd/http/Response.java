package xyz.navyd.http;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import xyz.navyd.http.enums.StatusEnum;

public class Response<T> {
    private final StatusEnum status;
    private final Map<String, String> headers;
    private final T body;

    public Response(StatusEnum status) {
        this(status, null);
    }

    public Response(StatusEnum status, T body) {
        this.status = status;
        this.body = body;
        this.headers = new HashMap<>();
    }

    public StatusEnum getStatus() {
        return status;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Optional<T> getBody() {
        return Optional.ofNullable(body);
    }

    public Response<T> addHeader(String key, String val) {
        this.headers.put(key, val);
        return this;
    }
}
