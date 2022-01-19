package rsb.io.io;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * @author Aliaksandr Liakh
 * @author Josh Long
 */
@Slf4j
public class IoEchoClient {

	public static void main(String[] args) throws IOException {
		try (var stdIn = new BufferedReader(new InputStreamReader(System.in))) { // <1>
			var message = (String) null;
			while ((message = stdIn.readLine()) != null) {
				try (var socket = new Socket("localhost", 7002)) {
					log.info("Echo client started: {}", socket);
					try (var is = socket.getInputStream(); var os = socket.getOutputStream()) {
						var bytes = message.getBytes();
						os.write(bytes);// <2>
						var totalRead = 0;
						while (totalRead < bytes.length) {
							var read = is.read(bytes, totalRead, bytes.length - totalRead);// <3>
							if (read <= 0)
								break;
							totalRead += read;
							log.info("Echo client read: {} byte(s)", read);
						}
						log.info("Echo client received: {}", new String(bytes, StandardCharsets.UTF_8));
					}
				}
				log.info("Echo client disconnected");
			}
		}
	}

}