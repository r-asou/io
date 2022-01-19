package rsb.io.nio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Locale;

@Slf4j
public class NioEchoServer {

    public static void main(String[] args) throws Exception {
        var selector = Selector.open();
        var socket = ServerSocketChannel.open();
        var serverSocket = socket.socket();
        serverSocket.bind(new InetSocketAddress("localhost", 8089));
        socket.configureBlocking(false);
        socket.register(selector, SelectionKey.OP_ACCEPT, null);
        while (true) {
            if (selector.select() == 0)
                continue;
            var selectedKeys = selector.selectedKeys();
            var keyIterator = selectedKeys.iterator();

            while (keyIterator.hasNext()) {
                var key = keyIterator.next();

                if (!key.isValid())
                    continue;

                if (key.isAcceptable()) {
                    var client = socket.accept();
                    client.configureBlocking(false);
                    client.register(selector, SelectionKey.OP_READ);
                } //
                else if (key.isReadable()) {
                    var client = (SocketChannel) key.channel();
                    var capture = ByteBuffer.allocate(1024);
                    client.read(capture);
                    var incoming = new String(capture.array()).trim().toUpperCase(Locale.ROOT);
                    var reply = buildBuffer(incoming);
                    client.register(selector, SelectionKey.OP_WRITE, reply);
                } //
                else if (key.isWritable()) {
                    var client = (SocketChannel) key.channel();
                    var buffer = (ByteBuffer) key.attachment();
                    client.write(buffer);
                }
                keyIterator.remove();
            }
        }
    }

    private static ByteBuffer buildBuffer(String contents) {
        var byteBuffer = ByteBuffer.allocate(1024);
        byteBuffer.clear();
        byteBuffer.put(contents.getBytes());
        byteBuffer.flip();
        return byteBuffer;
    }

    private static void attemptWrite(SocketChannel channel, Selector selector, ByteBuffer toWrite) throws IOException {
        if (channel.write(toWrite) == 0) {
            channel.register(selector, SelectionKey.OP_WRITE, toWrite);
        }
    }

}
