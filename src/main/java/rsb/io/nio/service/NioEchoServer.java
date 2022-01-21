package rsb.io.nio.service;

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
			ssc.bind(new InetSocketAddress(port()));
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
			read();
			write();
			TimeUnit.MILLISECONDS.sleep(100);
		}
	}

	private void accept() throws Exception {
		var newSocket = (Socket) this.sockets.poll();
		while (newSocket != null) {
			log.info("got a new socket...");
			newSocket.socketId(this.socketId.incrementAndGet());
			newSocket.socketChannel().configureBlocking(false);
			newSocket.socketChannel().register(this.readSelector, SelectionKey.OP_READ, newSocket);
			newSocket = this.sockets.poll();
		}
	}

	private void read() throws Exception {
		var readReady = this.readSelector.selectNow();
		if (readReady > 0) {
			var selectedKeys = this.readSelector.selectedKeys();
			var keyIterator = selectedKeys.iterator();
			while (keyIterator.hasNext()) {
				var key = keyIterator.next();
				if (key.isReadable()) {
					readFromSocket(key);
				}
				keyIterator.remove();
				log.info("removed the key");
			}
			selectedKeys.clear();
			log.info("clearing the keys");
		}
	}

	private void readFromSocket(SelectionKey key) throws Exception {
		log.info(String.format("reading for key [%s]", key));
		var socket = (Socket) key.attachment();
		var bb = ByteBuffer.allocate(1024);
		var read = socket.read(bb);
		var string = new String(bb.array()).trim();
		if (bb.remaining() == 0) {
			bb.clear();
		}
		bb.flip();
		log.info(String.format("read " + read + " byte(s): [%s]", string));

		if (socket.eos()) {
			log.info("socket closed: " + socket.socketId());
			key.attach(null);
			key.cancel();
			key.channel().close();
		}
	}

	private void write() throws Exception {

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
