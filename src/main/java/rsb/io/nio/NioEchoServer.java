package rsb.io.nio;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.time.Instant;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Slf4j
@RequiredArgsConstructor
public class NioEchoServer {

    private final ExecutorService executorService;

    @SneakyThrows
    public void start(int port) {
        log.info("started @ " + Instant.now().toString());
        var socketQueue = new ArrayBlockingQueue<Socket>(512);
        var socketAccepter = new SocketAcceptor(port, socketQueue);
        var socketProcessor = new SocketProcessor(socketQueue, Selector.open(), Selector.open(), new AtomicInteger());
        List.of(socketProcessor, socketAccepter).forEach(executorService::submit);
    }

}

@Slf4j
record SocketAcceptor(int port, Queue<Socket> sockets) implements Runnable {

    @Override
    @SneakyThrows
    public void run() {
        try (var ssc = ServerSocketChannel.open()) {
            ssc.bind(new InetSocketAddress(port));
            while (true) {
                var socketChannel = ssc.accept();
                sockets.add(new Socket(socketChannel));
                log.info("new socket accepted and queued");
            }
        }
    }
}

@Slf4j
record SocketProcessor(Queue<Socket> sockets, Selector readSelector, Selector writeSelector,
                       AtomicInteger socketId) implements Runnable {

    @Override
    @SneakyThrows
    public void run() {
        while (true) {
            accept();
            io(this.readSelector, SelectionKey::isReadable, this::readSocket);
            io(this.writeSelector, SelectionKey::isWritable, this::writeSocket);
            TimeUnit.MILLISECONDS.sleep(100);
        }
    }

    @SneakyThrows
    private void writeSocket(SelectionKey key) {
        var attachment = (Attachment) key.attachment();
        var socket = attachment.socket();
        var reply = attachment.reply();
        var bb = ByteBuffer.wrap(reply.getBytes());
        socket.write(bb);
        if (socket.eos())  {
            key.attach(null);
            key.cancel();
            key.channel().close();
            log.info ("closing " +socket.socketId());
        }
        bb.clear();

    }

    private static void io(Selector selector, Predicate<SelectionKey> predicate, Consumer<SelectionKey> consumer) throws Exception {
        var ready = selector.selectNow();
        if (ready > 0) {
            var selectedKeys = selector.selectedKeys();
            var keyIterator = selectedKeys.iterator();
            while (keyIterator.hasNext()) {
                var key = keyIterator.next();
                if (predicate.test(key)) {
                    consumer.accept(key);
                }
                keyIterator.remove();
            }
            selectedKeys.clear();
        }
    }

    private void accept() throws Exception {
        var newSocket = (Socket) this.sockets.poll();
        while (newSocket != null) {
            log.info("got a new socket...");
            newSocket.socketId(this.socketId.incrementAndGet());
            var socketChannel = newSocket.socketChannel();
            socketChannel.configureBlocking(false);
            socketChannel.register(this.readSelector, SelectionKey.OP_READ, newSocket);
            newSocket = this.sockets.poll();
        }
    }

    @SneakyThrows
    private void readSocket(SelectionKey key) {
        log.info(String.format("reading for key [%s]", key));
        var socket = (Socket) key.attachment();
        var bb = ByteBuffer.allocate(1024);
        socket.read(bb);
        var string = new String(bb.array()).trim();
        if (bb.remaining() == 0) {
            log.info("there are zero remaining bytes to read");
            bb.clear();
        }
        key.channel().register(this.writeSelector, SelectionKey.OP_WRITE, new Attachment(socket, string.toUpperCase()));

    }

    record Attachment(Socket socket, String reply) {
    }

}

@Slf4j
class Socket {

    private long socketId;

    private final SocketChannel socketChannel;

    private volatile boolean eos = false;

    public void socketId(long socketId) {
        this.socketId = socketId;
    }

    public SocketChannel socketChannel() {
        return this.socketChannel;
    }

    public long socketId() {
        return socketId;
    }

    public void eos(boolean endOfStream) {
        this.eos = endOfStream;
    }

    public boolean eos() {
        return eos;
    }

    Socket(long socketId, SocketChannel socketChannel, boolean endOfStreamReached) {
        this.socketId = socketId;
        this.socketChannel = socketChannel;
        this.eos = endOfStreamReached;
    }

    Socket(SocketChannel socketChannel) {
        this(-1, socketChannel, false);
    }

    public int read(ByteBuffer byteBuffer) throws IOException {
        var read = 0;
        var total = read;
        var run = 0;
        do {
            read = this.socketChannel.read(byteBuffer);
            total += read;
            log.info("reading loop #" + (run++));
        }
        while (read > 0);
        this.eos = true;
        return total;
    }

    public int write(ByteBuffer buffer) throws IOException {
        var written = this.socketChannel.write(buffer);
        var total = written;
        while (written > 0 && buffer.hasRemaining()) {
            written = this.socketChannel.write(buffer);
            total += written;
        }
        return total;
    }

}
