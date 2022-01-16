package rsb.io.nio2.future.server;


import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.ExecutionException;

/**
 * @author Aliaksandr Liakh
 */
@Slf4j
public class Nio2EchoFutureServer {


    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        var active = true;
        var serverSocketChannel = AsynchronousServerSocketChannel.open();
        serverSocketChannel.setOption(StandardSocketOptions.SO_RCVBUF, 1024);
        serverSocketChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        serverSocketChannel.bind(new InetSocketAddress("localhost", 7000));
        log.info("Echo server started: {}", serverSocketChannel);
        while (active) {
            var socketChannelFuture = serverSocketChannel.accept();
            var socketChannel = socketChannelFuture.get();
            log.info("Connection: {}", socketChannel);
            var buffer = ByteBuffer.allocate(1024);
            while (socketChannel.read(buffer).get() != -1) {
                buffer.flip();
                socketChannel.write(buffer).get();
                if (buffer.hasRemaining()) {
                    buffer.compact();
                } else {
                    buffer.clear();
                }
            }

            socketChannel.close();
            log.info("Connection finished");
        }

        serverSocketChannel.close();
        log.info("Echo server finished");
    }
}
