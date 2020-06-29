package xyz.navyd.http;

import java.io.IOException;
import java.net.HttpCookie;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.navyd.http.enums.MethodEnum;
import xyz.navyd.http.enums.VersionEnum;

public class RequestParser {
    private static final Logger log = LoggerFactory.getLogger(RequestParser.class);

    private static final int BUF_SIZE_LIMIT = 1024 * 100;
    private final int bufSize;

    public RequestParser() {
        this(1024*10);
    }
    
    public RequestParser(int bufSize) {
        if (bufSize <= 0 || bufSize > BUF_SIZE_LIMIT)
            throw new IllegalArgumentException("illegal bufSize: " + bufSize);
        this.bufSize = bufSize;
    }

    public Request parse(ReadableByteChannel channel) throws IOException {
        var buf = ByteBuffer.allocate(bufSize);
        log.trace("parsing channel with buf size: {}", bufSize);
        channel.read(buf);
        log.debug("readed buf from channel, buf current position: {}", buf.position());
        if (!buf.hasRemaining()) {
            log.error("parsing overflow! buf size: {}", bufSize);
            throw new IllegalArgumentException("request too large!");
        }
        buf.flip();
        log.trace("start parsing request");
        // parse first line \r\n
        var request = parseRequestLine(buf);
        // parse header from second to double \r\n
        parseHeaders(buf, request);
        // parse body
        log.debug("set request body as buf, position: {}, remaining: {}", buf.position(), buf.remaining());
        request.setBody(buf);
        return request;
    }

    private static void parseHeaders(ByteBuffer buf, Request request) {
        var sb = new StringBuilder();
        log.trace("parsing request headers, buf started position: {}", buf.position());
        while (true) {
            readLine(buf, sb);
            // sb.length==0 on header ended
            if (sb.length() <= 2) {
                log.debug("found single '\\r\\n' in a line: {}, header ended", sb);
                break;
            }
            var header = sb.toString().split(": ");
            if (header.length != 2) {
                log.error("parsing header error with ': ', header line: {}");
                throw new IllegalArgumentException("illegal header");
            }
            request.setHeader(header[0], header[1]);
            sb.setLength(0);
        }
        parseCookies(request);
    }

    /**
     * 从request.headers中找出Cookie header解析到request.cookies
     * @param request
     */
    private static void parseCookies(Request request) {
        var header = request.getHeader("Cookie");
        log.debug("parsing cookies");
        if (header.isEmpty()) {
            log.info("not found cookies in request: {}", request);
            return;
        }
        log.trace("cookie header: {}", header.get());
        for (var s : header.get().split("; ")) {
            var cookie = s.split("=");
            if (cookie.length != 2) {
                log.error("parsing cookie error with '=', cookie str: {}", s);
                throw new IllegalArgumentException("illegal cookie format");
            }
            var hc = new HttpCookie(cookie[0], cookie[1]);
            request.addCookie(hc);
            log.trace("added cookie: {}", hc);
        }
    }

    /**
     * 解析http first line并创建request
     * @param buf
     * @return
     * @throws IOException
     */
    private static Request parseRequestLine(ByteBuffer buf) throws IOException {
        var sb = new StringBuilder();
        readLine(buf, sb);
        // parse
        log.debug("parsing reqeust line: {}", sb);
        var line = sb.toString().split(" ");
        if (line.length != 3) {
            log.error("parsing request line error with space, line: {}", sb);
            throw new IllegalArgumentException("illegal request line");
        }
        var method = MethodEnum.parse(line[0]).orElseThrow();
        var url = line[1].split("\\?");
        if (url.length > 2) {
            log.error("parsing path and qeury error with ?, line: {}", sb);
            throw new IllegalArgumentException("illegal path and query");
        }
        var path = url[0];
        var query = url.length == 2 ? url[1] : null;
        var version = VersionEnum.parse(line[2]).orElseThrow();
        log.debug("request line parsed, method: {}, path: {}, query: {}, version: {}", method, path, query, version);
        return new Request(method, path, query, version);
    }

    /**
     * 读取一行到sb中，\r\n不会到sb中
     * @param buf
     * @param sb
     */
    private static void readLine(ByteBuffer buf, StringBuilder sb) {
        while (buf.hasRemaining()) {
            var ch = (char) buf.get();
            if (ch == '\r' && buf.get() == '\n') {
                break;
            }
            sb.append(ch);
        }
        log.trace("read line: {}, buf remaining: {}", sb, buf.remaining());
    }
}