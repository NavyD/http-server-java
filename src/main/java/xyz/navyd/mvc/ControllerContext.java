package xyz.navyd.mvc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.navyd.http.Request;
import xyz.navyd.http.Response;
import xyz.navyd.http.enums.MethodEnum;
import xyz.navyd.mvc.annotations.Controller;
import xyz.navyd.mvc.annotations.Router;

public class ControllerContext {
    private static final Logger log = LoggerFactory.getLogger(Controller.class);

    private final Map<String, List<ControllerComponent>> pathComponents = new HashMap<>();

    ControllerContext() {

    }

    public static ControllerContext newInstance() {
        return new ControllerContext();
    }

    public void scanPackage(String pkg) {
        log.debug("start scanning package: {}", pkg);
        var refs = new Reflections(pkg);
        // true避免 org.reflections.ReflectionsException: Scanner SubTypesScanner was not configured
        for (var controllerClazz : refs.getTypesAnnotatedWith(Controller.class, true)) {
            log.trace("scanning controller: {}", controllerClazz.getName());
            var routerOnClass = controllerClazz.getAnnotation(Router.class);
            // router class not found
            if (routerOnClass == null) {
                log.info("ignore controller: {}, not found Router on class", controllerClazz.getName());
                return;
            }
            // scan methods
            log.trace("scanning controller methods with: {}", Router.class.getName());
            for (var method : controllerClazz.getDeclaredMethods()) {
                var routerOnMethod = method.getAnnotation(Router.class);
                if (routerOnMethod == null) {
                    continue;
                }
                log.trace("found router method on {}.{}()", controllerClazz.getName(), method.getName());
                Object controller = null;
                try {
                    controller = controllerClazz.getDeclaredConstructor().newInstance();
                } catch (NoSuchMethodException e) {
                    log.error("not found No arguments constructor on {}, skip error: {}", controllerClazz.getName(),
                            e.getMessage());
                } catch (SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException e) {
                    log.error("skip new constructor error: {}", e);
                    e.printStackTrace();
                }
                if (controller != null) {
                    buildComponents(routerOnClass, routerOnMethod, controller, method);
                    log.debug("controller method added: {}.{}()", controllerClazz.getName(), method.getName());
                }
            }
        }
    }

    public <T> Optional<Response<T>> handle(Request request) {
        return Optional.empty();
    }

    /**
     * 如果request.path在pathControllers中被找到或正则匹配到，并request.method被
     * 包含在Router中，则返回ControllerComponent
     * @param request
     * @return
     */
    Optional<ControllerComponent> getControllerComponent(Request request) {
        return getControllerComponent(request.getPath(), request.getMethod());
    }

    Map<String, List<ControllerComponent>> getPathComponents() {
        return pathComponents;
    }

    private Optional<ControllerComponent> getControllerComponent(String path, MethodEnum method) {
        // 直接匹配
        var components = pathComponents.get(path);
        if (components != null) {
            for (var component : components) {
                if (contains(component.router.methods(), method)) {
                    return Optional.of(component);
                }
            }
        }
        
        // 正则匹配
        for (var entry : pathComponents.entrySet()) {
            components = entry.getValue();
            if (path.matches(entry.getKey())) {
                for (var component : components) {
                    if (contains(component.router.methods(), method)) {
                        return Optional.of(component);
                    }
                }
            }
        }
        return Optional.empty();
    }

    private void buildComponents(Router parent, Router child, Object controller, Method method) {
        log.trace("start build ControllerMethod");
        // paths
        StringBuilder sb = new StringBuilder();
        for (var parentPath : parent.value()) {
            if (parentPath.isBlank() || parentPath.isEmpty()) {
                continue;
            } 
            // ignore single '/' on class router
            else if (!"/".equals(parentPath)) {
                sb.append(parentPath);
            }
            for (var childPath : child.value()) {
                if (childPath.isBlank() || childPath.isEmpty()) {
                    continue;
                } 
                // router.methods empty is not allowed on methods
                if (child.methods().length == 0) {
                    log.error("not found router.methods on {}.{}()", controller.getClass().getName(), method.getName());
                    throw new IllegalArgumentException("router.methods is empty");
                }
                var path = sb.append(childPath).toString();
                checkDuplicateRouter(path, child, controller, method);
                var component = new ControllerComponent(controller, method, child);
                var components = pathComponents.getOrDefault(path, new ArrayList<>(8));
                components.add(component);
                pathComponents.put(path, components);
                log.trace("context added: {}", component);
                sb.setLength(parentPath.length());
            }
            sb.setLength(0);
        }
    }

    /**
     * 如果path与childRouter已存在则异常
     * @param path
     * @param childRouter
     * @param controller
     * @param method
     */
    private void checkDuplicateRouter(String path, Router childRouter, Object controller, Method method) {
        for (var childHttpMethod : childRouter.methods()) {
            // only one router for path, method
            var oldComponent = getControllerComponent(path, childHttpMethod);
            // 路径一样 method一样
            if (oldComponent.isPresent()) {
                log.error("found duplicate for path: {}, http method: {} in {}.{}() and {}.{}()", 
                    path,
                    childHttpMethod, 
                    oldComponent.get().controller.getClass().getName(), 
                    oldComponent.get().method.getName(),
                    controller.getClass().getName(), 
                    method.getName());
                throw new IllegalArgumentException("found duplicate controller router!");
            }
        }
        
    }

    static class ControllerComponent {
        final Object controller;
        final Method method;
        final Router router;

        public ControllerComponent(Object controller, Method method, Router router) {
            this.controller = controller;
            this.method = method;
            this.router = router;
        }

        @Override
        public String toString() {
            return "ControllerComponent [controller=" + controller + ", method=" + method + ", router=" + router + "]";
        }
        
    }

    private static <T> boolean contains(T[] arrays, T t) {
        if (t == null) {
            throw new NullPointerException();
        }
        for (var e : arrays) {
            if (e == null) 
                continue;
            else if (e.equals(t))
                return true;
        }
        return false;
    }
}