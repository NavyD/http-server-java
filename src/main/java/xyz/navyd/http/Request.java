package xyz.navyd.http;

import java.net.HttpCookie;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import xyz.navyd.http.enums.MethodEnum;
import xyz.navyd.http.enums.VersionEnum;

public class Request {
    private MethodEnum method;
    private String path;
    private String query;
    private VersionEnum version;
    private Map<String, String> headers;
    private ByteBuffer body;
    private List<HttpCookie> cookies;

    /**
     * 不支持file上传，存在buffer size限制
     * 
     * @param <T>
     * @return
     */
    public ByteBuffer getBody() {
        return body;
    }

    public Request(MethodEnum method, String path, String query, VersionEnum version) {
        this.method = method;
        this.version = version;
        this.headers = new HashMap<>();
        this.path = path;
        this.query = query;
        this.cookies = new ArrayList<>(16);
    }

    /**
     * 返回一个unmodifiableList
     * @return
     */
    public List<HttpCookie> getCookies() {
        return Collections.unmodifiableList(cookies);
    }

    public void addCookie(HttpCookie cookie) {
        this.cookies.add(cookie);
    }

    public MethodEnum getMethod() {
        return method;
    }

    public VersionEnum getVersion() {
        return version;
    }

    public void setBody(ByteBuffer body) {
        this.body = body;
    }

    public void setHeader(String key, String val) {
        this.headers.put(key, val);
    }

    public Optional<String> getHeader(String key) {
        return Optional.ofNullable(this.headers.get(key));
    }

    /**
     * 返回一个unmodifiable map
     * @return
     */
    public Map<String, String> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }

    public String getPath() {
        return path;
    }

    public Optional<String> getQuery() {
        return Optional.ofNullable(query);
    }

    @Override
    public String toString() {
        return "Request [body=" + body + ", headers=" + headers + ", method=" + method + ", path=" + path + ", query="
                + query + ", version=" + version + "]";
    }

}