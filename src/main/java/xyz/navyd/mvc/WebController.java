package xyz.navyd.mvc;

import xyz.navyd.http.Request;
import xyz.navyd.http.Response;

public interface WebController {
    void handle(Request request, Response response);
}

interface WebMatcher {
    boolean matchs(String path, String method);
}

class UserController {
    static WebMatcher USER_MATCHER = (p, m) -> p.equals("/users");

    static WebMatcher GET_USER = (p, m) -> p.equals("/users") && m.equals("get");

    public ResponseBody<User> getUser() {
        return () -> new User();
    }
}

interface ResponseBody<T> {
    T getBody();
}

class User {

}