package rsb.io.io;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.FileCopyUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * @author Aliaksandr Liakh
 * @author Josh Long
 */
@Slf4j
public class IoEchoClient {


    @SneakyThrows
    public void start(int port) {
        log.info("starting client");
        try (var bufferedReader = new BufferedReader(new InputStreamReader(
                new FileSystemResource("/Users/jlong/Desktop/content.html").getInputStream()))) { // <1>
            var message = FileCopyUtils.copyToString(bufferedReader);
            log.info("trying to write " + message.length());
            try (var socket = new Socket("localhost", port)) {
                log.info("Echo client started: {}", socket);
                try (var is = socket.getInputStream(); var os = socket.getOutputStream()) {
                    var bytes = message.getBytes();
                    os.write(bytes);// <2>
                    log.info("wrote " + bytes.length);

                    var string = FileCopyUtils.copyToByteArray(is);
                    /*var totalRead = 0;
                    while (totalRead < bytes.length) {
                        var read = is.read(bytes, totalRead, bytes.length - totalRead);// <3>
                        if (read <= 0)
                            break;
                        totalRead += read;
                        log.info("Echo client read: {} byte(s)", read);
                    }*/
                    log.info("Echo client received: {}", string);
                }
            }


        }
    }

    @SneakyThrows
    public static void main(String[] args) {
        var ioEchoClient = new IoEchoClient();
        ioEchoClient.start(8008);
    }

}