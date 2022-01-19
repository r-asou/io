package rsb.io.maybe.nio.server.selector;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

/**
 * @author Aliaksandr Liakh
 */
@Slf4j
public class NioSelectorEchoServer {

	private static boolean active = true;

	public static void main(String[] args) throws IOException {
		var serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.configureBlocking(false);
		serverSocketChannel.bind(new InetSocketAddress("localhost", 7000));
		log.info("Echo server started: {}", serverSocketChannel);
		var selector = Selector.open();
		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		while (active) {
			var selected = selector.select(); // blocking
			log.info("selected: {} key(s)", selected);
			var keysIterator = selector.selectedKeys().iterator();
			while (keysIterator.hasNext()) {
				var selectionKey = keysIterator.next();
				if (selectionKey.isAcceptable()) {
					accept(selector, selectionKey);
				}
				if (selectionKey.isReadable()) {
					keysIterator.remove();
					read(selector, selectionKey);
				}
				if (selectionKey.isWritable()) {
					keysIterator.remove();
					write(selectionKey);
				}
			}
		}

		serverSocketChannel.close();
		log.info("Echo server finished");
	}

	private static void accept(Selector selector, SelectionKey key) throws IOException {
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
		SocketChannel socketChannel = serverSocketChannel.accept(); // can be non-blocking
		if (socketChannel != null) {
			log.info("Connection is accepted: {}", socketChannel);

			socketChannel.configureBlocking(false);
			socketChannel.register(selector, SelectionKey.OP_READ);
		}
	}

	private static void read(Selector selector, SelectionKey key) throws IOException {
		var socketChannel = (SocketChannel) key.channel();
		var buffer = ByteBuffer.allocate(1024);
		int read = socketChannel.read(buffer); // can be non-blocking
		log.info("Echo server read: {} byte(s)", read);

		buffer.flip();
		byte[] bytes = new byte[buffer.limit()];
		buffer.get(bytes);
		String message = new String(bytes, StandardCharsets.UTF_8);
		log.info("Echo server received: {}", message);
		if (message.trim().equals("bye")) {
			active = false;
		}
		buffer.flip();
		socketChannel.register(selector, SelectionKey.OP_WRITE, buffer);
	}

	private static void write(SelectionKey key) throws IOException {
		var socketChannel = (SocketChannel) key.channel();
		var buffer = (ByteBuffer) key.attachment();
		socketChannel.write(buffer); // can be non-blocking
		socketChannel.close();

		buffer.flip();
		byte[] bytes = new byte[buffer.limit()];
		buffer.get(bytes);
		String message = new String(bytes, StandardCharsets.UTF_8);
		log.info("Echo server sent: {}", message);
	}

}
