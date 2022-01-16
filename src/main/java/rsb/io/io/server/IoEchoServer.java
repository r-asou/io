package rsb.io.io.server;


import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;

/**
 * @author Aliaksandr Liakh
 * @author Josh Long
 */
@Slf4j
public class IoEchoServer {

    public static void main(String[] args) throws IOException {
        try (var serverSocket = new ServerSocket(7002)) {
            log.info("Echo server started: {}", serverSocket);
            while (true) {
                try (var socket = serverSocket.accept()) {  // blocking
                    log.info("Connection accepted: {}", socket);
                    try (var is = socket.getInputStream();
                         var os = socket.getOutputStream()) {
                        var read = -1;
                        var bytes = new byte[1024];
                        while ((read = is.read(bytes)) != -1) { // blocking
                            log.info("Echo server read: {} byte(s)", read);
                            var message = new String(bytes, 0, read, StandardCharsets.UTF_8);
                            log.info("Echo server received: {}", message);
                            os.write(bytes, 0, read); // blocking
                        }
                    }
                }
                log.info("Connection closed");
            }
        }
    }
}
