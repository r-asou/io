package rsb.io.maybe.nio2.future.client;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;

/**
 * @author Aliaksandr Liakh
 */
@Slf4j
public class Nio2EchoFutureClient {

	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
		try (var stdIn = new BufferedReader(new InputStreamReader(System.in))) {
			var message = (String) null;
			while ((message = stdIn.readLine()) != null) {
				try (var socketChannel = AsynchronousSocketChannel.open()) {
					socketChannel.setOption(StandardSocketOptions.SO_RCVBUF, 1024);
					socketChannel.setOption(StandardSocketOptions.SO_SNDBUF, 1024);
					socketChannel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
					socketChannel.connect(new InetSocketAddress("localhost", 7000)).get();
					var outputBuffer = ByteBuffer.wrap(message.getBytes());
					socketChannel.write(outputBuffer).get();
					log.info("Echo client sent: {}", message);
					var inputBuffer = ByteBuffer.allocate(1024);
					while (socketChannel.read(inputBuffer).get() != -1) {
						inputBuffer.flip();
						if (inputBuffer.hasRemaining()) {
							inputBuffer.compact();
						} //
						else {
							inputBuffer.clear();
						}
					}
				}
			}
		}
	}

}
