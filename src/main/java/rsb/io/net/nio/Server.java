package rsb.io.net.nio;

import lombok.SneakyThrows;
import rsb.io.net.FileSyncService;
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

/**
 * Reads data in a non-blocking and asynchronous fashion
 */
public class Server implements FileSyncService {

	public static void main(String[] args) throws Exception {
		var port = 8888;
		var server = new Server();
		server.start(port, new FileSystemPersistingByteConsumer("nio"));
	}

	@Override
	public void start(int port, Consumer<byte[]> bytesHandler) throws Exception {

		var serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.configureBlocking(false);
		serverSocketChannel.bind(new InetSocketAddress(port));

		var selector = Selector.open();
		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

		while (!Thread.currentThread().isInterrupted()) {
			selector.select();
			var selectionKeys = selector.selectedKeys();
			for (var it = selectionKeys.iterator(); it.hasNext();) {
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

		}
	}

	private static void read(SelectionKey key, Selector selector, Consumer<byte[]> handler) throws Exception {
		var ra = (ReadAttachment) key.attachment();
		var bb = ByteBuffer.allocate(1024);
		var channel = (SocketChannel) key.channel();
		var read = -1;
		while ((read = channel.read(bb)) > 0) {
			ra.buffers().add(bb);
			channel.register(selector, SelectionKey.OP_READ, ra);
		}
		if (read == -1) {
			saveFile(ra.buffers(), handler);
			channel.register(selector, SelectionKey.OP_WRITE);
		}
	}

	@SneakyThrows
	private static void accept(SelectionKey key, Selector selector, SocketChannel socketChannel) {
		var readAttachment = new ReadAttachment(key, new ArrayList<>());
		socketChannel.configureBlocking(false);
		socketChannel.register(selector, SelectionKey.OP_READ, readAttachment);
	}

}
