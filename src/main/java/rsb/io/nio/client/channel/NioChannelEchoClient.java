package rsb.io.nio.client.channel;


import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

/**
 * @author Aliaksandr Liakh
 */
@Slf4j
public class NioChannelEchoClient {

    public static void main(String[] args) throws IOException {
        var stdIn = new BufferedReader(new InputStreamReader(System.in));
        var message = (String) null;
        while ((message = stdIn.readLine()) != null) {
            var socketChannel = SocketChannel.open(new InetSocketAddress("localhost", 7000));
            log.info("Echo client started: {}", socketChannel);
            var buffer = ByteBuffer.wrap(message.getBytes());
            socketChannel.write(buffer);
            log.info("Echo client sent: {}", message);
            var totalRead = 0;
            while (totalRead < message.getBytes().length) {
                buffer.clear();
                var read = socketChannel.read(buffer);
                log.info("Echo client read: {} byte(s)", read);
                if (read <= 0)
                    break;
                totalRead += read;
                buffer.flip();
                log.info("Echo client received: {}", StandardCharsets.UTF_8.newDecoder().decode(buffer));
            }
            socketChannel.close();
            log.info("Echo client disconnected");
        }
    }
}
