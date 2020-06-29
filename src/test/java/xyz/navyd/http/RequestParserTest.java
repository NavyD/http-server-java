package xyz.navyd.http;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.junit.jupiter.api.Test;

import xyz.navyd.http.enums.MethodEnum;
import xyz.navyd.http.enums.VersionEnum;

public class RequestParserTest {
    RequestParser parser = new RequestParser();

    @Test
    void parseGetRequestNoBody() throws IOException {
        FileChannel fc = FileChannel.open(
            Path.of(getClass().getResource("/get_request.txt").getPath()),
            StandardOpenOption.READ);
        assertThat(parser.parse(fc))
            .isNotNull()
            .matches(r ->
                !r.getBody().hasRemaining()
                && r.getMethod() == MethodEnum.GET
                && r.getPath().equals("/search")
                && r.getQuery().get().equals("q=test")
                && r.getVersion().equals(VersionEnum.HTTP1_1)
                && r.getHeaders().size() == 12
                && r.getHeader("Host").get().equals("github.com")
                && r.getHeader("Upgrade-Insecure-Requests").get().equals("1")
                && r.getHeader("User-Agent").get().equals("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.116 Safari/537.36")
                && r.getHeader("Accept").get().equals("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                && r.getHeader("Sec-Fetch-Site").get().equals("same-origin")
                && r.getHeader("Sec-Fetch-Mode").get().equals("navigate")
                && r.getHeader("Sec-Fetch-Dest").get().equals("document")
                && r.getHeader("Referer").get().equals("https://github.com/NavyD")
                && r.getHeader("Accept-Encoding").get().equals("gzip, deflate, br")
                && r.getHeader("Accept-Language").get().equals("en,zh-CN;q=0.9,zh;q=0.8")
                && r.getHeader("Cookie").get().equals("_octo=GH1.1.493281489.1576299052; _ga=GA1.2.2066131161.1576299065; tz=Asia%2FShanghai; ignored_unsupported_browser_notice=false; _device_id=629f76e82a2879e2c05d10dfb9d030d0; SL_GWPT_Show_Hide_tmp=1; SL_wptGlobTipTmp=1; tz=Asia%2FShanghai; user_session=UHc00R8GhGKRATmO9ud0-V6vrWaoMz283RO3AT_MBtInzROm; __Host-user_session_same_site=UHc00R8GhGKRATmO9ud0-V6vrWaoMz283RO3AT_MBtInzROm; logged_in=yes; dotcom_user=NavyD; has_recent_activity=1; _gat=1; _gh_sess=L2pJOXSbRRUdJy%2FTBkWEI7rII0BeU3LZJQq37EsqEAA3Uybc41SOwEtfHVm%2BWygqOupBmdhJuGHPa8EwwMg8pg57KgeFRy9bWwDFyKxMR%2BMGATJ9QPpiv8NrAqWprQHEAjKXP6BbRks6VfS2J95Hj2eukMcju%2Fs1MUi7I61mTir7ChYRZuAxSg0DqL9G4CAd8aWXVEhmsPYcgrU3%2BpZf2f1BTZ3Bwrdg94EVpWb4oELei%2BbUT2xc73NLaE%2FJcSaHrpEKAhl6s8Gi1K4yVBHpen0xjApIJ%2BjXeQY%2BV5N7ezMAKZGpxZ%2FJqtrhsjZAUc3XYjINnLCQp8oh%2BIHxQ6vsZdKg9Zlq%2B%2FwG3CKzX1KbEtoBkPW15bCLlvSImmDZ%2FniEs7zlmq2gnMYwOS2Ub2fxdj9kMh2ck05ZgamZZ3f7iIb5Q9R0PysalkMpsv0smFBCpvX2uGgODEwpBmM8XJaR1elL%2FxANWlwIQH5zoGkpTq8aS24ql9TKFntFbI6nn17Dy2UVKFnA6zf6TOGTCAJrpDtgFgPXNw%2FWCnEZjw%2B0aPBKHPNmOe9kMHVLj1tRjpG0Su995y%2F%2BbjfQcjGO4yhifX99BbqhDWC9cEAhw5TTm0VpN9SciNxXWL0TRKxGL2KTYfT3C6b0D1oTB6GguUt4EFDiscilTuymLAH3Lz3mBLEoXaJ1lQNQk6ECOOSzOb5b47gTwwJWc%2F%2BRS3SPUA%2BQYp2EQlRKuh%2F2nMshgxtyQtcRB%2FmeYcqukl8OhExV5PMKu54j13PkJYcMJ0lrRSG57l%2F9BG2oiWDDORQDSw5QS9ZnrUJ%2BvyDHVken%2BBrwmbwPSPjjFSj9BDeCfmYFp3zhuMh7KonrfugWomgVk0TbjHpkR%2FHgZhTqhjnBKrRKW%2Byn1IsLrOI5%2FMQyH%2Bbj06SKxv5OmF6EZtFDpSY1r5N%2B%2F9Y78IDqXuOpwJu2eixTc7seED7ZWQ35O2I%2F3XLhl8dE90muBYOJx3gNJt2v5nzy1VOAJ2U80Kwh%2FKIayA%2BkOe4xsrIPXnhouNat17Z%2Fo9HGZrvdeydIWt2nMkPzJa5YzO874i%2FuCI%2Bs%2BY7zW%2Fdenl0T%2FwAWuocswlqxQOfFgjEhGBDc016ao62%2BxiFWuZyobBjeeD89UsUg11zxBq78kZ2V0IRZK3srcHG5cOzhNQYb5kxw2IuzEY6i4bR5%2FCaiiNjapHJPwfSOof7A%2BaUbqYsbhBniO8Nuw6B10JP%2BqeXlpWjlRYFcvrKJaFLFvwRF2GB7IMCA7b2chEZpVhGkUNiQPp5PVg%3D%3D--ACfco04Urht7MF%2F8--KDRxcgDdPZ4vSN7F8BJNbQ%3D%3D")
                && r.getHeader("Connection").get().equals("keep-alive")
            );
    }
}