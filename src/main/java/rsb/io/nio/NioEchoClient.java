package rsb.io.nio;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.function.Supplier;

@Slf4j
@RequiredArgsConstructor
public class NioEchoClient {

	private final Supplier<BufferedReader> bufferedReaderSupplier;

	@SneakyThrows
	public void start(int port) {
		log.info("started @ " + Instant.now().toString());
		try (var stdIn = bufferedReaderSupplier.get()) {
			var message = (String) null;
			while ((message = stdIn.readLine()) != null) {
				message = message.strip().trim();
				log.info("going to send [" + message + "]");
				var address = new InetSocketAddress(port);
				try (var socketChannel = SocketChannel.open(address)) {
					log.info("echo client started: {}", socketChannel);
					var buffer = ByteBuffer.wrap(message.getBytes());
					var written = socketChannel.write(buffer);
					var length = message.getBytes().length;
					if (written == length)
						log.info("wrote as many bytes as there are to write");
					log.info("echo client sent: {}", message);
					var totalRead = 0;
					while (totalRead < length) {
						buffer.clear();
						var read = socketChannel.read(buffer);
						log.info("echo client read: {} byte(s)", read);
						if (read <= 0) {
							log.info("read < 0: " + read);
							break;
						}
						totalRead += read;
						buffer.flip();
						log.info("echo client received: {}", StandardCharsets.UTF_8.newDecoder().decode(buffer));
					}
				}

			}
			log.info("echo client disconnected");
		}
	}

}
