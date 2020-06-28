package xyz.navyd.http;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

import xyz.navyd.http.enums.MethodEnum;
import xyz.navyd.http.enums.VersionEnum;

public class RequestParser {
    private static final String LINE = "\r\n";
    private final int bufSize = 1024 * 10;

    public RequestParser() {

    }

    public Request parse(ReadableByteChannel channel) throws IOException {
        var buf = ByteBuffer.allocate(bufSize);
        channel.read(buf);
        if (!buf.hasRemaining()) {
            throw new IllegalArgumentException("request too large!");
        }
        buf.flip();
        // parse first line \r\n
        var request = parseRequestLine(buf);
        // parse header from second to double \r\n
        parseHeaders(buf, request);
        // parse body
        request.setBody(buf);
        // request.setBody(channel);
        return request;
    }

    private static void parseHeaders(ByteBuffer buf, Request request) {
        var sb = new StringBuilder();
        while (true) {
            readLine(buf, sb);
            if (sb.length() <= 2) {
                break;
            }
            var header = sb.toString().split(": ");
            if (header.length != 2) {
                throw new IllegalArgumentException("illegal header: " + sb);
            }
            request.setHeader(header[0], header[1]);
            sb.setLength(0);
        }
    }

    private static Request parseRequestLine(ByteBuffer buf) throws IOException {
        var sb = new StringBuilder();
        readLine(buf, sb);
        // parse
        var line = sb.toString().split(" ");
        if (line.length != 3) {
            throw new IllegalArgumentException("illegal request line: " + sb);
        }
        var m = MethodEnum.parse(line[0]).orElseThrow();
        var url = line[1].split("\\?");
        if (url.length > 2) {
            throw new IllegalArgumentException("illegal url: " + line[1]);
        }
        var path = url[0];
        var query = url.length == 2 ? url[1] : null;
        var v = VersionEnum.parse(line[2]).orElseThrow();
        // clear
        return new Request(m, path, query, v);
    }

    private static void readLine(ByteBuffer buf, StringBuilder sb) {
        while (buf.hasRemaining()) {
            var ch = (char) buf.get();
            if (ch == '\r' && buf.get() == '\n') {
                break;
            }
            sb.append(ch);
        }
    }
}