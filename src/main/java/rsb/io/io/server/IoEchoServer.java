package rsb.io.io.server;


import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;

/**
 * @author Aliaksandr Liakh
 */
@Slf4j
public class IoEchoServer {

    public static void main(String[] args) throws IOException {
        var serverSocket = new ServerSocket(7000);
        log.info("Echo server started: {}", serverSocket);
        var active = true;
        while (active) {
            var socket = serverSocket.accept(); // blocking
            log.info("Connection accepted: {}", socket);
            var is = socket.getInputStream();
            var os = socket.getOutputStream();
            var read = -1;
            var bytes = new byte[1024];
            while ((read = is.read(bytes)) != -1) { // blocking
                log.info("Echo server read: {} byte(s)", read);

                String message = new String(bytes, 0, read, StandardCharsets.UTF_8);
                log.info("Echo server received: {}", message);
                if (message.trim().equals("bye")) {
                    active = false;
                }

                os.write(bytes, 0, read); // blocking
            }

            socket.close();
            log.info("Connection closed");
        }

        serverSocket.close();
        log.info("Echo server finished");
    }
}
