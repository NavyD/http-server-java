package xyz.navyd.mvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import xyz.navyd.BaseTest;
import xyz.navyd.http.Request;
import xyz.navyd.http.enums.MethodEnum;
import xyz.navyd.http.enums.VersionEnum;
import xyz.navyd.mvc.test_pkg.router.a.EmptyRouterMethodOnControllerMethod;
import xyz.navyd.mvc.test_pkg.router.b.HomeController;

public class ControllerContextTest extends BaseTest {
    @Test
    void basicsScanRouter() {
        var context = ControllerContext.newInstance();
        context.scanPackage(HomeController.class.getPackageName());
        var request = new Request(MethodEnum.GET, "/home", null, VersionEnum.HTTP1_1);
        var cm = context.getControllerMethod(request);
        assertThat(cm)
            .isNotNull()
            .isNotEmpty();
        assertThat(cm.get()).matches(con ->
            con.getController() instanceof HomeController)
            .matches(con -> con.getHttpMethod() == request.getMethod())
            .matches(con -> con.getMethod().getName().contains("getHelloWorld"))
            .matches(con -> con.getPath().equals("/home"));
    }

    @Test
    void regexMatchTest() {
        var context = ControllerContext.newInstance();
        context.scanPackage(HomeController.class.getPackageName());
        var request = new Request(MethodEnum.GET, "/users/123", null, VersionEnum.HTTP1_1);
        var cm = context.getControllerMethod(request);
        assertThat(cm)
            .isNotNull()
            .isNotEmpty();
        assertThat(cm.get())
            .matches(con -> con.getController() instanceof HomeController)
            .matches(con -> request.getPath().matches(con.getPath()))
            .matches(con -> con.getHttpMethod() == request.getMethod())
            .matches(con -> con.getMethod().getName().contains("getUser"));
    }

    @Test
    void routerMethodsEmptyError() {
        var exc = assertThrows(IllegalArgumentException.class, () -> ControllerContext
                .newInstance()
                .scanPackage(EmptyRouterMethodOnControllerMethod.class.getPackageName()));
        assertTrue(exc.getMessage().contains("empty") && exc.getMessage().contains("methods"));
    }


}

