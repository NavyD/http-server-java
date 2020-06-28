package xyz.navyd.http;

import java.util.List;

public class Response {
    private Object body;

    public <T> T getBody() {
        return (T)body;
    }
}

interface Filter {
    boolean filter(Request request, Response response);
}


class FilterManager {
    private List<Filter> filters;
    private List<ResponseHandler> respHandlers;

    public boolean doFilter(Request request, Response response) {
        for (Filter filter : filters) {
            // reponse write
            if (!filter.filter(request, response)) {
                respHandlers.forEach(r -> {
                    r.handle(response);
                });
                return false;
            }
        }
        return true;
    }
}

interface ResponseHandler {
    void handle(Response response);
}