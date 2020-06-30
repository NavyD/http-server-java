package xyz.navyd.mvc.test_pkg.router.b;

import xyz.navyd.mvc.annotations.Controller;
import xyz.navyd.mvc.annotations.Router;

@Controller
@Router("/")
public class NoResponseOnRouterMethod {
    
    @Router("/no_resp")
    void noResponse() {
    }

    @Router("/non_resp")
    String nonResponse() {
        return "non reps";
    }
}