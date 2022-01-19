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
public class NioMultiplexingEchoServer {

	private static boolean active = true;

	public static void main(String[] args) throws IOException {
		var ports = 8;
		var serverSocketChannels = new ServerSocketChannel[ports];
		var selector = Selector.open();
		for (var p = 0; p < ports; p++) {
			var serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannels[p] = serverSocketChannel;
			serverSocketChannel.configureBlocking(false);
			serverSocketChannel.bind(new InetSocketAddress("localhost", 7000 + p));
			log.info("Echo server started: {}", serverSocketChannel);
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		}

		while (active) {
			var selected = selector.select(); // blocking
			log.info("selected: {} key(s)", selected);
			var keysIterator = selector.selectedKeys().iterator();
			while (keysIterator.hasNext()) {
				SelectionKey key = keysIterator.next();

				if (key.isAcceptable()) {
					accept(selector, key);
				}
				if (key.isReadable()) {
					keysIterator.remove();
					read(selector, key);
				}
				if (key.isWritable()) {
					keysIterator.remove();
					write(key);
				}
			}
		}
		for (var serverSocketChannel : serverSocketChannels) {
			serverSocketChannel.close();
		}
		log.info("Echo server finished");
	}

	private static void accept(Selector selector, SelectionKey key) throws IOException {
		var serverSocketChannel = (ServerSocketChannel) key.channel();
		var socketChannel = serverSocketChannel.accept(); // can be non-blocking
		if (socketChannel != null) {
			log.info("Connection is accepted: {}", socketChannel);

			socketChannel.configureBlocking(false);
			socketChannel.register(selector, SelectionKey.OP_READ);
		}
	}

	private static void read(Selector selector, SelectionKey key) throws IOException {
		var socketChannel = (SocketChannel) key.channel();
		var buffer = ByteBuffer.allocate(1024);
		var read = socketChannel.read(buffer); // can be non-blocking
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
