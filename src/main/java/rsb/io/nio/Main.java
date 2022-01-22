package rsb.io.nio;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.FileCopyUtils;
import rsb.io.io.IoEchoClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// https://crunchify.com/java-nio-non-blocking-io-with-server-client-example-java-nio-bytebuffer-and-channels-selector-java-nio-vs-io/

@Slf4j
public class Main {

    record ReadAttachment(
            SelectionKey key,
            ByteBuffer buffer,
            ByteArrayOutputStream bytes) {
    }


    @SneakyThrows
    static void server(int port) {
        log.info("starting a server on port " + port);

        var address = new InetSocketAddress(port);
        try (var selector = Selector.open();
             var socket = ServerSocketChannel.open()) {
            socket.bind(address);
            socket.configureBlocking(false);
            socket.register(selector, SelectionKey.OP_ACCEPT);
            while (true) {
                selector.select();
                var iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    var key = iterator.next();
                    iterator.remove();
                    if (key.isAcceptable()) {
                        doAccept(selector, socket, key);
                    } //
                    else if (key.isReadable()) {
                        key.interestOps(key.interestOps() & (~SelectionKey.OP_READ));
                        doRead(key);
                    }
                }
            }
        }
    }

    private static void doAccept(Selector selector, ServerSocketChannel socket, SelectionKey key) throws IOException {
        var client = socket.accept();
        client.configureBlocking(false);
        var readAttachment = new ReadAttachment(key, ByteBuffer.allocate(1024 / 2), new ByteArrayOutputStream());
        client.register(selector, SelectionKey.OP_READ, readAttachment);
        log.info("connection accepted: " + client.getLocalAddress());
    }


    @SneakyThrows
    static void doRead(SelectionKey key) {
        try {
            var attachment = (ReadAttachment) key.attachment();
            var buffer = attachment.buffer();
            buffer.clear();
            var channel = (SocketChannel) key.channel();
            var size = 0;
            var byteArrayOutputStream = attachment.bytes();
            while ((size = channel.read(buffer)) > 0) {
                buffer.flip();
                byteArrayOutputStream.write(buffer.array(), 0, size);
                buffer.clear();
            }
            if (size == -1) {
                FileCopyUtils.copy(byteArrayOutputStream.toByteArray(), new File(System.getenv("HOME") + "/Desktop/out"));
                byteArrayOutputStream.close();
                return;
            }
            key.interestOps(key.interestOps() | SelectionKey.OP_READ);
            key.selector().wakeup();

        } //
        catch (Throwable e) {
            log.error("oops!", e);
        }

    }


    public static void main(String[] a) throws Exception {
        var port = 8008;
        var executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        executor.submit(() -> server(port));
        TimeUnit.MILLISECONDS.sleep(1000);
        executor.submit(() -> IoEchoClient.main(new String[0]));
        Thread.currentThread().join();
    }

}
