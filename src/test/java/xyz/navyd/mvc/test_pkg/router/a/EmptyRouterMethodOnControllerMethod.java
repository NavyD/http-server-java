package xyz.navyd.mvc.test_pkg.router.a;

import xyz.navyd.mvc.annotations.Controller;
import xyz.navyd.mvc.annotations.Router;

@Controller
@Router("/")
public class EmptyRouterMethodOnControllerMethod {
    @Router("/empty_error")
    void emptyRouterMethod() {

    }
}