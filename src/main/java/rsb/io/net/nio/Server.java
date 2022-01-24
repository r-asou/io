package rsb.io.net.nio;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import rsb.io.net.FileSystemPersistingByteConsumer;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
public class Server {

    public static void main(String[] args)  {
        var port = 8888;
        var server = new Server();
        server.start(port, new FileSystemPersistingByteConsumer());
    }


    @SneakyThrows
    public void start(int port, Consumer<byte[]> bytesHandler) {

        var serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(port));

        var selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (!Thread.currentThread().isInterrupted()) {
            selector.select();
            var selectionKeys = selector.selectedKeys();
            for (var it = selectionKeys.iterator(); it.hasNext(); ) {
                var key = it.next();
                it.remove();
                if (key.isAcceptable()) {
                    var socket = serverSocketChannel.accept();
                    accept(key, selector, socket);
                } //
                else if (key.isReadable()) {
                    read(key, selector, bytesHandler);
                }
            }
        }

    }

    record ReadAttachment(SelectionKey key, List<ByteBuffer> buffers) {
    }


    @SneakyThrows
    private static void saveFile(List<ByteBuffer> buffers, Consumer<byte[]> handler) {
        try (var baos = new ByteArrayOutputStream()) {
            for (var bb : buffers) {
                bb.flip();
                var bytes = new byte[bb.limit()];
                bb.get(bytes);
                baos.write(bytes);
            }
            var bytes = baos.toByteArray();
            handler.accept(bytes);
            log.info("there are " + bytes.length + " bytes");

        }
    }

    private static void read(SelectionKey key, Selector selector,
                             Consumer<byte[]> handler) throws Exception {
        log.info("it's time to read! ");
        var ra = (ReadAttachment) key.attachment();
        var bb = ByteBuffer.allocate(1024);
        var channel = (SocketChannel) key.channel();
        var read = -1;
        while ((read = channel.read(bb)) > 0) {
//            log.info("read: " + read);
            ra.buffers().add(bb);
            channel.register(selector, SelectionKey.OP_READ, ra);
        }
        if (read == -1) {
            saveFile(ra.buffers(), handler);
            channel.register(selector, SelectionKey.OP_WRITE);
        }

    }

    @SneakyThrows
    private static void accept(SelectionKey key, Selector selector, SocketChannel socketChannel) throws Exception {
        var readAttachment = new ReadAttachment(key, new ArrayList<>());
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ, readAttachment);
    }


}

/*
@Slf4j
@RequiredArgsConstructor
class SocketProcessor implements Runnable {

    record ReadAttachment(List<ByteBuffer> buffers) {
    }

    private final Selector selector;

    @Override
    @SneakyThrows
    public void run() {
        while (true) {
            selector.select();

            log.info("run()");
            var keys = selector.selectedKeys();
            var iterator = keys.iterator();
            while (iterator.hasNext()) {
                iterator.remove();
                var key = iterator.next();
                if (key.isAcceptable()) {
                    log.info("new connection..");
                    key.channel().register(selector, SelectionKey.OP_READ, new ReadAttachment(new ArrayList<>()));
                } //
                else if (key.isReadable()) {
                    var ra = (ReadAttachment) key.attachment();
                    var bb = ByteBuffer.allocate(1024);
                    var channel = (SocketChannel) key.channel();
                    var read = -1;
                    while ((read = channel.read(bb)) != -1) {
                        ra.buffers().add(bb);
                        log.info("adding " + bb.position() + " elements");
                        channel.register(selector, SelectionKey.OP_READ, ra);
                    }
                } //
                else {

                }
            }
        }
    }
}
*/