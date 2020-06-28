package xyz.navyd.mvc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ControllerContext {
    private final Map<String, ControllerMethod> context = new ConcurrentHashMap<>();

    public void initContext() {

    }

    static class ControllerMethod {

    }
}