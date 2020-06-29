package xyz.navyd.mvc.test_pkg.router.b;

import xyz.navyd.http.Response;
import xyz.navyd.http.enums.MethodEnum;
import xyz.navyd.http.enums.StatusEnum;
import xyz.navyd.mvc.annotations.Controller;
import xyz.navyd.mvc.annotations.Router;

@Controller
@Router(value = "/")
public class HomeController {
    @Router(value = {
        "/home",
        "/index.html",
        "/"
    }, methods = MethodEnum.GET)
    Response<String> getHelloWorld() {
        return new Response<String>(StatusEnum.OK_200, "hello world!");
    }

    @Router(value = "/users/\\d+", methods = MethodEnum.GET)
    Response<String> getUserWithRegex() {
        return new Response<String>(StatusEnum.OK_200, "username=test");
    }

    @Router(value = "/users", methods = MethodEnum.POST)
    Response<String> putUser() {
        innerMethod();
        return new Response<String>(StatusEnum.OK_200, "username=test");
    }

    private void innerMethod() {}
}
