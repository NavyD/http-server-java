package xyz.navyd.mvc.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import xyz.navyd.http.enums.MethodEnum;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Router {
    /**
     * 匹配request.url.path部份，
     * 支持正则表达式，
     * 支持类与方法级组合
     * 
     * @return
     */
    String[] value();

    MethodEnum method();
}