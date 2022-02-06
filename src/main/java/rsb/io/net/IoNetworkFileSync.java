package rsb.io.net;

import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.net.ServerSocket;
import java.util.function.Consumer;

/**
 * Reads data in a synchronous and blocking fashion
 */
@Component
class IoNetworkFileSync implements NetworkFileSync {

	@Override
	@SneakyThrows
	public void start(int port, Consumer<byte[]> consumer) {
		try (var ss = new ServerSocket(port)) { // <1>
			while (true) {
				try (var socket = ss.accept(); // <2>
						var in = socket.getInputStream(); // <3>
						var out = new ByteArrayOutputStream()) { // <4>
					var bytes = new byte[1024];
					var read = -1;
					while ((read = in.read(bytes)) != -1)
						out.write(bytes, 0, read);

					// <5>
					consumer.accept(out.toByteArray());
				}

			}
		}
	}

}
