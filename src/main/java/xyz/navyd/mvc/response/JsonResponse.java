package xyz.navyd.mvc.response;

import java.util.Optional;

public class JsonResponse<T> implements Response<T> {

    @Override
    public Optional<T> getBody() {
        return null;
    }
    
}