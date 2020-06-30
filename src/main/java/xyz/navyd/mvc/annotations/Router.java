package xyz.navyd.mvc.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import xyz.navyd.http.enums.MethodEnum;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Router {
    /**
     * 匹配request.url.path部份，
     * 支持正则表达式，
     * 支持类与方法级组合
     * 支持多个path同时匹配
     * @return
     */
    String[] value();

    /**
     * 在controller method上匹配http method
     * 支持多个匹配
     * 
     * <p>在class上定义的没有限定作用
     * 
     * @return
     */
    MethodEnum[] methods() default MethodEnum.GET;
}