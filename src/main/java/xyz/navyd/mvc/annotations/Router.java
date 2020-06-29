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
     * 匹配http method
     * 
     * <p>当在controller上为空时，匹配所有http method，否则在
     * class上的范围内找controller method, 在class未定义但在method为找时，将异常
     * 
     * <p>支持多个匹配
     * 
     * 如果class与method都是多个，则组合全部/parent/child
     * 
     * 
     * 
     * @return
     */
    MethodEnum[] methods() default {};
}