package rsb.io.nio.server.channel;


import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * @author Aliaksandr Liakh
 */
@Slf4j
public class NioNonBlockingEchoServer {

    public static void main(String[] args) throws Exception {
        var serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        log.info("Echo server is blocking: {}", serverSocketChannel.isBlocking());
        serverSocketChannel.bind(new InetSocketAddress(7000));
        log.info("Echo server started: {}", serverSocketChannel);
        var active = true;
        while (active) {
            try (var socketChannel = serverSocketChannel.accept()){ // non-blocking
                if (socketChannel == null) {
                    log.info("waiting for incoming connection...");
                    TimeUnit.MILLISECONDS.sleep(5000);
                } //
                else {
                    log.info("Connection accepted: {}", socketChannel);
                    socketChannel.configureBlocking(false);
                    log.info("Connection is blocking: {}", socketChannel.isBlocking());
                    var buffer = ByteBuffer.allocate(1024);
                    while (true) {
                        buffer.clear();
                        int read = socketChannel.read(buffer); // non-blocking
                        log.info("Echo server read: {} byte(s)", read);
                        if (read < 0) {
                            break;
                        }
                        buffer.flip();
                       var  bytes = new byte[buffer.limit()];
                        buffer.get(bytes);
                        var message = new String(bytes, StandardCharsets.UTF_8);
                        log.info("Echo server received: {}", message);
                        buffer.flip();
                        socketChannel.write(buffer); // can be non-blocking
                    }

                    socketChannel.close();
                    log.info("Connection closed");
                }
            }

        }

        serverSocketChannel.close();
        log.info("Echo server finished");
    }
}
