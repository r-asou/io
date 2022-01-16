package rsb.io.nio.server.channel;


import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.charset.StandardCharsets;

/**
 * @author Aliaksandr Liakh
 */
@Slf4j
public class NioBlockingEchoServer {

    public static void main(String[] args) throws IOException {
        try (var serverSocketChannel = ServerSocketChannel.open()) {
            log.info("Echo server is blocking: {}", serverSocketChannel.isBlocking());
            serverSocketChannel.bind(new InetSocketAddress("localhost", 7003));
            log.info("Echo server started: {}", serverSocketChannel);
            var active = true;
            while (active) {
                try (var socketChannel = serverSocketChannel.accept()) {
                    log.info("Connection accepted: {}", socketChannel);
                    log.info("Connection is blocking: {}", socketChannel.isBlocking());
                    var buffer = ByteBuffer.allocate(1024);
                    while (true) {
                        buffer.clear();
                        var read = socketChannel.read(buffer); // blocking
                        log.info("Echo server read: {} byte(s)", read);
                        if (read < 0) {
                            break;
                        }
                        buffer.flip();
                        var bytes = new byte[buffer.limit()];
                        buffer.get(bytes);
                        var message = new String(bytes, StandardCharsets.UTF_8);
                        log.info("Echo server received: {}", message);
                        buffer.flip();
                        socketChannel.write(buffer); // blocking
                    }
                    log.info("Connection closed");
                }
            }
            log.info("Echo server finished");
        }
    }
}
