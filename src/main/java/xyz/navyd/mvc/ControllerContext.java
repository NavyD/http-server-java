package xyz.navyd.mvc;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.navyd.http.Request;
import xyz.navyd.http.Response;
import xyz.navyd.http.enums.MethodEnum;
import xyz.navyd.mvc.annotations.Body;
import xyz.navyd.mvc.annotations.Controller;
import xyz.navyd.mvc.annotations.PathParam;
import xyz.navyd.mvc.annotations.QueryParam;
import xyz.navyd.mvc.annotations.Router;

public class ControllerContext {
    private static final Logger log = LoggerFactory.getLogger(Controller.class);

    private final Map<String, List<ControllerComponent>> pathComponents = new HashMap<>();
    private ObjectMapper mapper;

    ControllerContext() {

    }

    public static ControllerContext newInstance() {
        return new ControllerContext();
    }

    public void scanPackage(String pkg) {
        log.debug("start scanning package: {}", pkg);
        var refs = new Reflections(pkg);
        // true避免 org.reflections.ReflectionsException: Scanner SubTypesScanner was not
        // configured
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
                } else if (!method.getReturnType().equals(Response.class)) {
                    log.error("found return type {} is not {} on {}.{}()", method.getReturnType().getName(),
                            Response.class.getName(), controllerClazz.getName(), method.getName());
                    throw new IllegalArgumentException("return type is not " + Response.class.getName());
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

    @SuppressWarnings("unchecked")
    public <T> Optional<Response<T>> handle(Request request) {
        var component = getControllerComponent(request);
        if (component.isEmpty()) {
            return Optional.empty();
        }
        var parameters = component.get().method.getParameters();
        var paramObjects = new Object[parameters.length];
        for (var i = 0; i < parameters.length; i++) {
            var param = parameters[i];
            var type = param.getType();
            // 注入request
            if (type.equals(Request.class)) {
                paramObjects[i] = request;
                continue;
            }
            var annotations = param.getAnnotations();
            // 除了request 不允许自动注入未注解类型
            Annotation annotation = getPredefinedAnnotation(annotations);
            Optional<Object> o = Optional.empty();
            if (annotation == null) {
                log.error("unannotated class parameter: {}.{}({})", component.get().controller.getClass().getName(),
                        component.get().method.getName(), param.getName());
                throw new IllegalArgumentException("unannotated class parameter!");
            } else if (annotation instanceof PathParam) {
                o = getPathParamVal(component.get().path, (PathParam) annotation, request, type);

            } else if (annotation instanceof QueryParam) {
                o = getQueryParamVal((QueryParam) annotation, request, type, param.getName());
            } else if (annotation instanceof Body) {
                o = getBodyVal((Body) annotation, request, type);
            } else {
                log.error("unknown annotation: {}", annotation.getClass().getName());
                throw new IllegalArgumentException("unknown annotation");
            }
            // null
            if (o.isEmpty()) {
                throw new IllegalArgumentException("parsing annotation error!");
            }
            paramObjects[i] = o.get();
        }
        try {
            var response = component.get().method.invoke(component.get().controller, paramObjects);
            return Optional.of((Response<T>) response);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            log.error("method invoke error: {}", e);
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * 如果request.path在pathControllers中被找到或正则匹配到，并request.method被
     * 包含在Router中，则返回ControllerComponent
     * 
     * @param request
     * @return
     */
    Optional<ControllerComponent> getControllerComponent(Request request) {
        return getControllerComponent(request.getPath(), request.getMethod());
    }

    Map<String, List<ControllerComponent>> getPathComponents() {
        return pathComponents;
    }

    private Optional<Object> getBodyVal(Body bodyAnno, Request request, Class<?> paramClazz) {
        var buf = request.getBody();
        var bytes = new byte[buf.remaining()];
        buf.get(bytes, buf.position(), buf.limit());
        try {
            return Optional.of(mapper.readValue(bytes, paramClazz));
        } catch (IOException e) {
            log.error("parsing body error: {}", e);
            e.printStackTrace();
        }
        return Optional.empty();
    }

    private Optional<Object> getQueryParamVal(QueryParam queryAnno, Request request, Class<?> paramClazz, String inputParamName) {
        final var queryName = queryAnno.value() != null && queryAnno.value().isEmpty() ? queryAnno.value() : inputParamName;
        return request.getQuery().map(queryStr -> {
            for (var s : queryStr.split("&")) {
                var query = s.split("=");
                if (query.length != 2) {
                    log.error("parsing query error! {}", s);
                    throw new IllegalArgumentException("parsing query error: {}" + s);
                }
                if (query[0].equals(queryName)) {
                    try {
                        return mapper.readValue(query[1], paramClazz);
                    } catch (JsonProcessingException e) {
                        log.error("parsing query error: {}", e);
                        e.printStackTrace();
                    }
                }
            }
            return null;
        });
    }

    private Optional<Object> getPathParamVal(String regex, PathParam pathAnno, Request request, Class<?> paramClazz) {
        var matcher = Pattern.compile(regex).matcher(request.getPath());
        var pathVal = matcher.group(pathAnno.value());
        try {
            return Optional.ofNullable(mapper.readValue(pathVal, paramClazz));
        } catch (JsonProcessingException e) {
            log.error("parsing path param val error: {}", e);
            e.printStackTrace();
        }
        return Optional.empty();
    }

    private Annotation getPredefinedAnnotation(Annotation[] annotations) {
        var myAnnos = new Class<?>[] { Router.class, PathParam.class, QueryParam.class, Body.class };
        for (var clazz : myAnnos) {
            var anno = getFirst(annotations, clazz);
            if (anno != null) {
                return (Annotation) anno;
            }
        }
        return null;
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
                var component = new ControllerComponent(controller, method, child, path);
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
        final String path;

        public ControllerComponent(Object controller, Method method, Router router, String path) {
            this.controller = controller;
            this.method = method;
            this.router = router;
            this.path = path;
        }

        @Override
        public String toString() {
            return "ControllerComponent [controller=" + controller + ", method=" + method + ", path=" + path
                    + ", router=" + router + "]";
        }
        
    }

    private static <T> boolean contains(T[] arrays, T t) {
        return getFirst(arrays, t) != null;
    }

    private static <T> T getFirst(T[] arrays, T t) {
        if (t == null) {
            throw new NullPointerException();
        }
        for (var e : arrays) {
            if (e == null) 
                continue;
            else if (e.equals(t))
                return e;
        }
        return null;
    }
}