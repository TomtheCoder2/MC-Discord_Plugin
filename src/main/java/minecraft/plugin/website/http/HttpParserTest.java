package minecraft.plugin.website.http;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HttpParserTest {
    private HttpParser httpParser;

    @BeforeAll
    public void beforeClass() {
        httpParser = new HttpParser();
    }

    @Test
    void parserHttpRequest() {
        HttpRequest request = null;
        try {
            request = httpParser.parserHttpRequest(
                    generateValidGETTestCase()
            );
        } catch (HttpParsingException e) {
            fail(e);
        }

        assertEquals(request.getMethod(), HttpMethod.GET);
    }

    @Test
    void parserHttpRequestBadMethod1() {
        try {
            HttpRequest request = httpParser.parserHttpRequest(
                    generateBadTestCaseMethod()
            );
            fail();
        } catch (HttpParsingException e) {
            assertEquals(e.getErrorCode(), HttpStatusCode.SERVER_ERROR_501_NOT_IMPLEMENTED);
        }
    }

    @Test
    void parserHttpRequestBadMethod2() {
        try {
            HttpRequest request = httpParser.parserHttpRequest(
                    generateBadTestCaseMethod2()
            );
            fail();
        } catch (HttpParsingException e) {
            assertEquals(e.getErrorCode(), HttpStatusCode.SERVER_ERROR_501_NOT_IMPLEMENTED);
        }
    }

    @Test
    void parserHttpRequestBadMethod3() {
        try {
            HttpRequest request = httpParser.parserHttpRequest(
                    generateBadTestCaseMethod3()
            );
            fail();
        } catch (HttpParsingException e) {
            assertEquals(e.getErrorCode(), HttpStatusCode.CLIENT_ERROR_400_BAD_REQUEST);
        }
    }



    private InputStream generateValidGETTestCase() {
        String rawData = """
                GET / HTTP/1.1\r
                Host: localhost:8080\r
                Connection: keep-alive\r
                Cache-Control: max-age=0\r
                sec-ch-ua: " Not A;Brand";v="99", "Chromium";v="96", "Google Chrome";v="96"\r
                sec-ch-ua-mobile: ?0\r
                sec-ch-ua-platform: "Windows"\r
                Upgrade-Insecure-Requests: 1\r
                User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36\r
                Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9\r
                Sec-Fetch-Site: none\r
                Sec-Fetch-Mode: navigate\r
                Sec-Fetch-User: ?1\r
                Sec-Fetch-Dest: document\r
                Accept-Encoding: gzip, deflate, br\r
                Accept-Language: en,de-DE;q=0.9,de;q=0.8,en-US;q=0.7\r
                \r
                """;

        return new ByteArrayInputStream(
                rawData.getBytes(StandardCharsets.US_ASCII)
        );
    }

    private InputStream generateBadTestCaseMethod() {
        String rawData = """
                fGET / HTTP/1.1\r
                Host: localhost:8080\r
                Accept-Language: en,de-DE;q=0.9,de;q=0.8,en-US;q=0.7\r
                \r
                """;

        return new ByteArrayInputStream(
                rawData.getBytes(StandardCharsets.US_ASCII)
        );
    }

    private InputStream generateBadTestCaseMethod2() {
        String rawData = """
                fGETTTTTTTTTT / HTTP/1.1\r
                Host: localhost:8080\r
                Accept-Language: en,de-DE;q=0.9,de;q=0.8,en-US;q=0.7\r
                \r
                """;

        return new ByteArrayInputStream(
                rawData.getBytes(StandardCharsets.US_ASCII)
        );
    }

    private InputStream generateBadTestCaseMethod3() {
        String rawData = """
                GET / AAAA HTTP/1.1\r
                Host: localhost:8080\r
                Accept-Language: en,de-DE;q=0.9,de;q=0.8,en-US;q=0.7\r
                \r
                """;

        return new ByteArrayInputStream(
                rawData.getBytes(StandardCharsets.US_ASCII)
        );
    }
}