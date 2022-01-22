package rsb.io.io;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.ServerSocket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author Aliaksandr Liakh
 * @author Josh Long
 */
@Slf4j
@RequiredArgsConstructor
public class IoEchoServer {


    @SneakyThrows
    public void start(int port) {
        try (var serverSocket = new ServerSocket(8008)) {// <1>
            log.info("Echo server started: {}", serverSocket);
            while (true) {
                try (var socket = serverSocket.accept()) { // <2>
                    log.info("Connection accepted: {}", socket);
                    try (var is = socket.getInputStream(); // <3>
                         var os = socket.getOutputStream()) {
                        var read = -1;
                        var bytes = new byte[1024];
                        while ((read = is.read(bytes)) != -1) { // <4>>
                            log.info("Echo server read: {} byte(s)", read);
                            var message = new String(bytes, 0, read, StandardCharsets.UTF_8);
                            log.info("Echo server received: {}", message);
                            os.write(bytes, 0, read); // <5>
                        }
                    }
                }
                log.info("Connection closed");
            }
        }
    }

    @SneakyThrows
    public static void main(String[] args) {
        var server = new IoEchoServer( );
        server.start(8008);
    }

}
