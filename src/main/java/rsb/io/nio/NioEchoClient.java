package rsb.io.nio;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

@Slf4j
public class NioEchoClient {

	public static void main(String[] args) throws IOException {
		try (var stdIn = new BufferedReader(new InputStreamReader(System.in))) {
			var message = (String) null;
			while ((message = stdIn.readLine()) != null) {
				var address = new InetSocketAddress("localhost", 8089);
				try (var socketChannel = SocketChannel.open(address)) {
					log.info("Echo client started: {}", socketChannel);
					var buffer = ByteBuffer.wrap(message.getBytes());
					socketChannel.write(buffer);
					log.info("Echo client sent: {}", message);
					var totalRead = 0;
					while (totalRead < message.getBytes().length) {
						buffer.clear();
						var read = socketChannel.read(buffer);
						log.info("Echo client read: {} byte(s)", read);
						if (read <= 0)
							break;
						totalRead += read;
						buffer.flip();
						log.info("Echo client received: {}", StandardCharsets.UTF_8.newDecoder().decode(buffer));
					}
				}
			}
			log.info("Echo client disconnected");
		}
	}

}
