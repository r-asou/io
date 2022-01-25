package rsb.io.net;

import lombok.SneakyThrows;

import java.io.ByteArrayOutputStream;
import java.net.ServerSocket;
import java.util.function.Consumer;

/**
 * Reads data in a synchronous and blocking fashion
 */
class IoNetworkFileSync implements NetworkFileSync {

	@Override
	@SneakyThrows
	public void start(int port, Consumer<NetworkFileSyncBytes> consumer) {
		try (var ss = new ServerSocket(port)) {
			while (true) {
				try (var socket = ss.accept();
						var in = socket.getInputStream();
						var out = new ByteArrayOutputStream()) {
					var bytes = new byte[1024];
					var read = -1;
					while ((read = in.read(bytes)) != -1)
						out.write(bytes, 0, read);
					consumer.accept(new NetworkFileSyncBytes(getClass().getSimpleName(), out.toByteArray()));
				}

			}
		}
	}

}
