package rsb.io.net;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.FileCopyUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Reads data in a non-blocking and asynchronous fashion
 */
@Slf4j
class NioNetworkFileSync implements NetworkFileSync {

	public static void main(String[] args) throws Exception {
		var nfs = new NioNetworkFileSync();
		var file = new File("/Users/jlong/code/reactive-spring-book/io/content");
		log.info("bytes in source file: " + FileCopyUtils.copyToByteArray(file).length);
		nfs.start(8888, new FileSystemPersistingByteConsumer("nio"));
	}

	@Override
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
				var pos = bb.position();
				bb.flip();
				var bytes = new byte[bb.limit()];
				bb.get(bytes);
				baos.write(bytes, 0, bb.position());
			}
			// baos.flush();
			var bytes = baos.toByteArray();
			handler.accept(bytes);

		}
	}

	private final static AtomicInteger byteBufferCount = new AtomicInteger(0);

	private static void read(SelectionKey key, Selector selector, Consumer<byte[]> handler) throws Exception {
		var ra = (ReadAttachment) key.attachment();
		var len = 1000;
		log.info("c: " + byteBufferCount.getAndIncrement());
		var bb = ByteBuffer.allocate(len);
		var channel = (SocketChannel) key.channel();
		var read = -1;

		while ((read = channel.read(bb)) >= 0) {
			log.info("read: " + read);
			ra.buffers().add(bb);
			bb = ByteBuffer.allocate(len);
			channel.register(selector, SelectionKey.OP_READ, new ReadAttachment(ra.key(), ra.buffers()));
		}

		if (read == -1) {
			log.info("< 0");
			saveFile(ra.buffers(), handler);
			channel.register(selector, SelectionKey.OP_WRITE);
		}
		log.info("buffers size: " + ra.buffers().size());
	}

	@SneakyThrows
	private static void accept(SelectionKey key, Selector selector, SocketChannel socketChannel) {
		var readAttachment = new ReadAttachment(key, new CopyOnWriteArrayList<>());
		socketChannel.configureBlocking(false);
		socketChannel.register(selector, SelectionKey.OP_READ, readAttachment);
	}

}
