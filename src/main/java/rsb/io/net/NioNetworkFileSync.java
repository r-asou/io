package rsb.io.net;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Reads data in a non-blocking and asynchronous fashion
 */
@Slf4j
class NioNetworkFileSync implements NetworkFileSync {

	@Override
	@SneakyThrows
	public void start(int port, Consumer<NetworkFileSyncBytes> bytesHandler) {

		var serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.configureBlocking(false);
		serverSocketChannel.bind(new InetSocketAddress(port));

		var selector = Selector.open();// <1>
		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);// <2>

		while (!Thread.currentThread().isInterrupted()) { // <3>
			selector.select();// <4>
			var selectionKeys = selector.selectedKeys();// <5>
			for (var it = selectionKeys.iterator(); it.hasNext();) {
				var key = it.next();
				it.remove();
				if (key.isAcceptable()) { // <6>
					var socket = serverSocketChannel.accept();
					accept(key, selector, socket);
				} //
				else if (key.isReadable()) {// <7>
					read(key, selector, bytesHandler);
				}
			}
		}

	}

	record ReadAttachment(SelectionKey key, List<ByteBuffer> buffers) {
	}

	@SneakyThrows
	private static void accept(SelectionKey key, Selector selector, SocketChannel socketChannel) {
		var readAttachment = new ReadAttachment(key, new CopyOnWriteArrayList<>());// <8>
		socketChannel.configureBlocking(false);// <7>
		socketChannel.register(selector, SelectionKey.OP_READ, readAttachment);
	}

	@SneakyThrows
	private static void saveFile(List<ByteBuffer> buffers, Consumer<NetworkFileSyncBytes> handler) {

		try (var baos = new ByteArrayOutputStream()) {
			for (var bb : buffers) {
				bb.flip();
				var bytes = new byte[bb.limit()];
				bb.get(bytes);
				baos.write(bytes, 0, bb.position());
			}
			var bytes = baos.toByteArray();
			handler.accept(new NetworkFileSyncBytes(NioNetworkFileSync.class.getSimpleName(), bytes));

		}
	}

	private static void read(SelectionKey key, Selector selector, Consumer<NetworkFileSyncBytes> handler)
			throws Exception {
		var ra = (ReadAttachment) key.attachment();
		var len = 1000;
		var bb = ByteBuffer.allocate(len);
		var channel = (SocketChannel) key.channel();
		var read = -1;

		if ((read = channel.read(bb)) >= 0) {
			ra.buffers().add(bb);
			channel.register(selector, SelectionKey.OP_READ, new ReadAttachment(ra.key(), ra.buffers()));
		}

		if (read == -1) {
			saveFile(ra.buffers(), handler);
			channel.register(selector, SelectionKey.OP_WRITE);
		}
	}

}
