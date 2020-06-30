package xyz.navyd.mvc;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import xyz.navyd.BaseTest;
import xyz.navyd.http.Request;
import xyz.navyd.http.enums.MethodEnum;
import xyz.navyd.http.enums.VersionEnum;
import xyz.navyd.mvc.test_pkg.router.a.EmptyRouterMethodOnControllerMethod;
import xyz.navyd.mvc.test_pkg.router.basic.UserController;

public class ControllerContextTest extends BaseTest {
    @Test
    void getUsersWithRouter() {
        var context = ControllerContext.newInstance();
        context.scanPackage(UserController.class.getPackageName());
        var request = new Request(MethodEnum.GET, "/host/users", null, VersionEnum.HTTP1_1);
        var component = context.getControllerComponent(request);
        assertTrue(component.isPresent());
    }

    @Test
    void getUserWithRouterRegex() {
        var context = ControllerContext.newInstance();
        context.scanPackage(UserController.class.getPackageName());
        var request = new Request(MethodEnum.GET, "/host/users/1", null, VersionEnum.HTTP1_1);
        var component = context.getControllerComponent(request);
        assertTrue(component.isPresent());
        assertTrue(component.get().controller instanceof UserController);
    }

    @Test
    void routerMethodsEmptyAsDefaultGetMethod() {
        var context = ControllerContext.newInstance();
        context.scanPackage(EmptyRouterMethodOnControllerMethod.class.getPackageName());
        // 在单独的包
        assertTrue(context.getPathComponents().size() == 1);
        var methods = context.getPathComponents().entrySet().iterator().next().getValue().get(0).router.methods();
        assertTrue(methods.length == 1 && methods[0] == MethodEnum.GET);
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
