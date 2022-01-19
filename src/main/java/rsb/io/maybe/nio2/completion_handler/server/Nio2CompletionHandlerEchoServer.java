package rsb.io.maybe.nio2.completion_handler.server;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import rsb.io.maybe.nio2.completion_handler.BaseCompletionHandler;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;

/**
 * @author Aliaksandr Liakh
 */
@Slf4j
public class Nio2CompletionHandlerEchoServer {

	public static void main(String[] args) throws Exception {
		var serverSocketChannel = AsynchronousServerSocketChannel.open();
		serverSocketChannel.bind(new InetSocketAddress(7001));
		var acceptCompletionHandler = new BaseCompletionHandler<AsynchronousSocketChannel, Void>() {
			@Override
			public void completed(AsynchronousSocketChannel asynchronousSocketChannel, Void attachment) {
				serverSocketChannel.accept(null, this);
				var buffer = ByteBuffer.allocate(1024);
				var readCompletionHandler = new BaseCompletionHandler<Integer, Void>() {

					@Override
					public void completed(Integer bytesRead, Void attachment) {
						buffer.flip();
						var bytes = new byte[buffer.limit()];
						buffer.get(bytes);
						var writeCompletionHandler = new BaseCompletionHandler<Integer, Void>() {

							@Override
							@SneakyThrows
							public void completed(Integer i, Void attachment) {
								asynchronousSocketChannel.close();
							}
						};
						buffer.flip();
						asynchronousSocketChannel.write(buffer, null, writeCompletionHandler);
					}
				};
				asynchronousSocketChannel.read(buffer, null, readCompletionHandler);

			}
		};
		serverSocketChannel.accept(null, acceptCompletionHandler);
		Thread.currentThread().join();
	}

}
