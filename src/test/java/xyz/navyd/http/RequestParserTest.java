package xyz.navyd.http;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.junit.jupiter.api.Test;

public class RequestParserTest {
    RequestParser parser = new RequestParser();
    
    @Test
    void parseGetRequest() throws IOException {
        FileChannel fc = FileChannel.open(
            Path.of(getClass().getResource("/get_request.txt").getPath()),
            StandardOpenOption.READ);
        var reqeust = parser.parse(fc);
        System.out.println(reqeust);
    }

    public static void main(String[] args) throws IOException {
        var test = new RequestParserTest();
        test.parseGetRequest();
    }
}