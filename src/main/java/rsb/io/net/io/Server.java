package rsb.io.net.io;

import rsb.io.net.NetworkFileSync;
import rsb.io.net.FileSystemPersistingByteConsumer;

import java.io.ByteArrayOutputStream;
import java.net.ServerSocket;
import java.util.function.Consumer;

/**
 * Reads data in a synchronous and blocking fashion
 */
public class Server implements NetworkFileSync {

	public static void main(String[] args) throws Exception {
		var port = 8888;
		var server = new Server();
		server.start(port, new FileSystemPersistingByteConsumer("io"));
	}

	@Override
	public void start(int port, Consumer<byte[]> bytesHandler) throws Exception {
		try (var ss = new ServerSocket(port)) {
			while (true) {
				try (var socket = ss.accept();
						var in = socket.getInputStream();
						var out = new ByteArrayOutputStream()) {
					var bytes = new byte[1024];
					var read = -1;
					while ((read = in.read(bytes)) != -1)
						out.write(bytes, 0, read);
					bytesHandler.accept(out.toByteArray());
				}

			}
		}
	}

}
