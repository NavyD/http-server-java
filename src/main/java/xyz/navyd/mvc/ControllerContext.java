package xyz.navyd.mvc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.navyd.http.Request;
import xyz.navyd.http.Response;
import xyz.navyd.http.enums.MethodEnum;
import xyz.navyd.mvc.annotations.Controller;
import xyz.navyd.mvc.annotations.Router;

public class ControllerContext {
    private static final Logger log = LoggerFactory.getLogger(Controller.class);
    // private static final ControllerContext INSTANCE = new ControllerContext();

    private final Set<ControllerMethod> controllers = new HashSet<>(256);

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
            log.trace("scanning controller: {}", controllerClazz.getCanonicalName());
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
                    build(controllers, routerOnClass, routerOnMethod, controller, method);
                    log.debug("controller method added: {}.{}()", controllerClazz.getName(), method.getName());
                }
            }
        }
    }

    public <T> Optional<Response<T>> handle(Request request) {
        return Optional.empty();
    }

    /**
     * test可用
     * @param request
     * @return
     */
    Optional< ControllerMethod> getControllerMethod(Request request) {
        log.debug("controllers size: {}", controllers.size());
        for (var cm : controllers) {
            log.debug("cm: {}, request: {}", cm, request);
            if (cm.httpMethod == request.getMethod() && request.getPath().matches(cm.getPath())) {
                return Optional.of(cm);
            }
        }
        return Optional.empty();
    }

    private static void build(Set<ControllerMethod> controllers, Router parent, Router child, Object controller, Method method) {
        log.trace("start build ControllerMethod");
        
        // paths
        StringBuilder sb = new StringBuilder();
        for (var parentPath : parent.value()) {
            if (parentPath.isBlank() || parentPath.isEmpty()) {
                continue;
            } else if (!"/".equals(parentPath)) {
                sb.append(parentPath);
            }
            for (var childPath : child.value()) {
                if (childPath.isBlank() || childPath.isEmpty()) {
                    continue;
                } else if (child.methods().length == 0) {
                    log.error("not found router.methods on {}.{}()", controller.getClass().getName(), method.getName());
                    throw new IllegalArgumentException("router.methods is empty");
                }
                var path = sb.append(childPath).toString();
                // methods
                for (var childHttpMethod : child.methods()) {
                    // allow all when class methods is empty
                    boolean httpMethodExists = parent.methods().length == 0;
                    // find method in parent methods
                    for (var parentHttpMethod : parent.methods()) {
                        if (parentHttpMethod == childHttpMethod) {
                            httpMethodExists = true;
                            break;
                        }
                    }
                    if (!httpMethodExists) {
                        log.error(
                            "skipped http method, child exists in a http method: {}, but parent does not http methods: {}." +
                            "on controller: {}, method: {}",
                            childHttpMethod, 
                            Arrays.toString(parent.methods()),
                            controller.getClass().getName(),
                            method.getName());
                        throw new IllegalArgumentException("router on class http methods not found on methods");
                    }
                    var cm = new ControllerMethod(controller, method, childHttpMethod, path);
                    if (!controllers.add(cm)) {
                        ControllerMethod dupCm = null;
                        for (var c : controllers) {
                            if (c.equals(cm)) {
                                dupCm = c;
                                break;
                            }
                        }
                        log.error(
                            "found duplicate for path: {}, http method: {} in {}.{}() and {}.{}()",
                            cm.getPath(),
                            cm.getHttpMethod(),
                            dupCm.getController().getClass().getName(),
                            dupCm.getMethod().getName(),
                            cm.getController().getClass().getName(),
                            cm.getMethod().getName());
                        throw new IllegalArgumentException("found duplicate controller router!");
                    }
                    log.trace("context added: {}", cm);
                }
                sb.setLength(parentPath.length());
            }
            sb.setLength(0);
        }
    }
    /**
     * 在sb中找出反斜线'\'并插入一个'\'使用于正则表达式
     * @param sb
     * @return
     */
    static StringBuilder appendBlackSlash(StringBuilder sb) {
        int i = 0;
        while (true) {
            i = sb.indexOf("\\", i);
            if (i < 0) {
                break;
            }
            sb.insert(i, "\\");
            i++;
        }
        return sb;
    }

    /**
     * test可用
     */
    static class ControllerMethod {
        private final Object controller;
        private final Method method;
        private final MethodEnum httpMethod;
        private final String path;

        public ControllerMethod(Object controller, Method method, MethodEnum httpMethod, String path) {
            this.controller = controller;
            this.method = method;
            this.httpMethod = httpMethod;
            this.path = path;
        }

        @Override
        public String toString() {
            return "ControllerMethod [controller=" + controller + ", httpMethod=" + httpMethod + ", method=" + method
                    + ", path=" + path + "]";
        }

        public Object getController() {
            return controller;
        }

        public Method getMethod() {
            return method;
        }

        public MethodEnum getHttpMethod() {
            return httpMethod;
        }

        public String getPath() {
            return path;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((httpMethod == null) ? 0 : httpMethod.hashCode());
            result = prime * result + ((path == null) ? 0 : path.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ControllerMethod other = (ControllerMethod) obj;
            if (httpMethod != other.httpMethod)
                return false;
            if (path == null) {
                if (other.path != null)
                    return false;
            } else if (!path.equals(other.path))
                return false;
            return true;
        }
    }
}