package xyz.navyd.mvc.response;

import java.util.Optional;

public interface Response<T> {
    Optional<T> getBody();
}