package rsb.io.nio;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import rsb.io.io.IoEchoClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Locale;
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

    record WriteAttachment(SelectionKey key, ByteBuffer buffer) {
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
                        log.info("read");
                        doRead(selector, key);
                    } //
                    else if (key.isWritable()) {
//                        log.info("write");
                        doWrite(selector, key);
                    } //
                }
            }
        }
    }

    private static void doWrite(Selector selector, SelectionKey key) throws IOException {
        Assert.isTrue(selector.isOpen(), "the channels not open");
        var attachment = (WriteAttachment) key.attachment();
        var bb = attachment.buffer();
        var channel = (SocketChannel) key.channel();
        while (bb.hasRemaining()) {
            var written = channel.write(bb);
            log.info("remaining=" + (written > 0) + "? wrote=" + written);
        }
    }


    static void doRead(Selector selector, SelectionKey key) {

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
            if (size == 0) { // can't read anymore
                log.info("there is no more data to read");
                var string = byteArrayOutputStream.toString().toUpperCase(Locale.ROOT);
                var bytesArray = string.getBytes();
                var bb = ByteBuffer.wrap(bytesArray);
                log.info("the byte buffer length is " + bytesArray.length);
                channel.register(selector, SelectionKey.OP_WRITE, new WriteAttachment(key, bb));
                return;
            }
            if (size == -1) { // means disconnect
                log.info("disconnected && closed baos");
                byteArrayOutputStream.close();
                return;
            }
            log.info("registering for more reads..");
            channel.register(selector, SelectionKey.OP_READ, attachment);

        } //
        catch (Throwable e) {
            log.error("oops!", e);
        }

    }

    private static void doAccept(Selector selector, ServerSocketChannel socket, SelectionKey key) throws IOException {
        var client = socket.accept();
        client.configureBlocking(false);
        var readAttachment = new ReadAttachment(key, ByteBuffer.allocate(1024 / 2), new ByteArrayOutputStream());
        client.register(selector, SelectionKey.OP_READ, readAttachment);
        log.info("connection accepted: " + client.getLocalAddress());
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
