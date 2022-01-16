package rsb.io.nio2.completion_handler.client;


import lombok.extern.slf4j.Slf4j;
import rsb.io.nio2.completion_handler.BaseCompletionHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Aliaksandr Liakh
 */
@Slf4j
public class Nio2CompletionHandlerEchoClient {

    public static void main(String[] args) throws IOException {
        var stdIn = new BufferedReader(new InputStreamReader(System.in));
        var message = (String) null;
        while ((message = stdIn.readLine()) != null) {
            var socketChannel = AsynchronousSocketChannel.open();
            socketChannel.setOption(StandardSocketOptions.SO_RCVBUF, 1024);
            socketChannel.setOption(StandardSocketOptions.SO_SNDBUF, 1024);
            socketChannel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);

            var attachment = new Attachment(message, new AtomicBoolean(true));
            var acceptCompletionHandler = new BaseCompletionHandler<Void, Attachment>() {

                @Override
                public void completed(Void result, Attachment attachment) {
                    var message = attachment.message();
                    var buffer = ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8));
                    var writeCompletionHandler = new BaseCompletionHandler<Integer, Attachment>() {

                        @Override
                        public void completed(Integer bytesWritten, Attachment attachment) {
                            log.info("Echo client wrote: {} byte(s)", bytesWritten);
                            var buffer = ByteBuffer.allocate(1024);
                            var readCompletionHandler = new BaseCompletionHandler<Integer, Attachment>() {
                                @Override
                                public void completed(Integer result, Attachment attachment) {
                                    try {
                                        buffer.flip();
                                        log.info("Echo client received: {}", StandardCharsets.UTF_8.newDecoder().decode(buffer));
                                        attachment.active().set(false);
                                    } //
                                    catch (IOException e) {
                                        log.error("Exception during echo processing", e);
                                    }
                                }
                            };
                            socketChannel.read(buffer, attachment, readCompletionHandler);
                        }
                    };
                    socketChannel.write(buffer, attachment, writeCompletionHandler);
                }
            };
            socketChannel.connect(new InetSocketAddress("localhost", 7001), attachment, acceptCompletionHandler);
            while (attachment.active().get()) {
            }
            socketChannel.close();
            log.info("Echo client finished");
        }
    }

    record Attachment(String message, AtomicBoolean active) {
    }


}

