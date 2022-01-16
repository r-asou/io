package rsb.io.io.client;


import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * @author Aliaksandr Liakh
 */
@Slf4j
public class IoEchoClient {

    public static void main(String[] args) throws IOException {
        var stdIn = new BufferedReader(new InputStreamReader(System.in));
        var message = (String) null;
        while ((message = stdIn.readLine()) != null) {
            var socket = new Socket("localhost", 7000);
            log.info("Echo client started: {}", socket);

            var is = socket.getInputStream();
            var os = socket.getOutputStream();
            var bytes = message.getBytes();
            os.write(bytes);
            var totalRead = 0;
            while (totalRead < bytes.length) {
                int read = is.read(bytes, totalRead, bytes.length - totalRead);
                if (read <= 0)
                    break;
                totalRead += read;
                log.info("Echo client read: {} byte(s)", read);
            }
            log.info("Echo client received: {}", new String(bytes, StandardCharsets.UTF_8));
            socket.close();
            log.info("Echo client disconnected");
        }
    }
}