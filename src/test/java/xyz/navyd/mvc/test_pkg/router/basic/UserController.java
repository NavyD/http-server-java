package xyz.navyd.mvc.test_pkg.router.basic;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import xyz.navyd.http.Response;
import xyz.navyd.http.enums.MethodEnum;
import xyz.navyd.http.enums.StatusEnum;
import xyz.navyd.mvc.annotations.Controller;
import xyz.navyd.mvc.annotations.Router;

@Controller
@Router({"/host", "/home"})
public class UserController {

    public static String[] classPaths() {
        return new String[] {"/host", "/home"};
    }

    Map<Integer, User> users = new HashMap<>();

    public UserController() {
        setup();
    }

    @Router(value = { "/users", "/userhome" }, methods = MethodEnum.GET)
    Response<Collection<User>> getUsers() {
        return new Response<>(StatusEnum.OK_200, users.values());
    }

    @Router(value = "/users/\\d+", methods = MethodEnum.GET)
    Response<Optional<User>> getUser(Integer uid) {
        return new Response<>(StatusEnum.OK_200, Optional.ofNullable(users.get(uid)));
    }

    @Router(value = "/users", methods = MethodEnum.POST)
    Response<String> addUser(User user) {
        return new Response<String>(StatusEnum.OK_200, "successfuly. uid=" + user.getUid());
    }

    @Router(value = "/users/\\d+", methods = MethodEnum.PUT)
    Response<User> modifyUser(Integer uid, User newUser) {
        newUser.uid = uid;
        if (!users.containsKey(uid)) {
            return Response.withNotFound();
        }
        users.put(uid, newUser);
        return new Response<>(StatusEnum.OK_200, newUser);
    }

    @Router(value = "/users/\\d+", methods = MethodEnum.DELETE)
    Response<Void> deleteUser(Integer uid) {
        var user = users.get(uid);
        if (user == null)
            return Response.withNotFound();
        return Response.withOk(null);
    }

    private void setup() {
        var maxId = 100;
        var baseName = "user_name_";
        Random rand = new Random();
        for (var id = 0; id < maxId; id ++) {
            var user = new User(id, baseName + id, rand.nextInt(100) + 18);
            users.put(id, user);
        }
    }

    public static class User {
        private Integer uid; 
        private String name;
        private Integer age;

        public User(Integer uid, String name, Integer age) {
            this.uid = uid;
            this.name = name;
            this.age = age;
        }

        public Integer getUid() {
            return uid;
        }

        public void setUid(Integer uid) {
            this.uid = uid;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        @Override
        public String toString() {
            return "User [age=" + age + ", name=" + name + ", uid=" + uid + "]";
        }
    }
}
