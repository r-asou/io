package rsb.io.net.nio;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

@Slf4j
public class Server {


    record ReadAttachment(SelectionKey key, List<ByteBuffer> buffers) {
    }

    public static void main(String[] args) throws Exception {
        var executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        var port = 8888;
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
                    read(key, selector);
                }
            }
        }
    }

    private static void read(SelectionKey key, Selector selector) throws Exception {
        log.info("it's time to read! ");

        var ra = (ReadAttachment) key.attachment();
        var bb = ByteBuffer.allocate(1024);
        var channel = (SocketChannel) key.channel();
        var read = -1;
        while ((read = channel.read(bb)) > 0) {
            log.info("read: " + read);
            ra.buffers().add(bb);
            channel.register(selector, SelectionKey.OP_READ, ra);
        }
        if (read == -1 ){
            log.info ("there are " + ra.buffers().size() + " batches of 1024");
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