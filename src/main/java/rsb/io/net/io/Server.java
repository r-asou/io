package rsb.io.net.io;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Scanner;

@Slf4j
public class Server {

	public static void main(String[] args) throws Exception {
		var port = 8888;
		try (var serverSocket = new ServerSocket(port)) {
			while (true) {
				try (var clientSocket = serverSocket.accept();
						var in = new BufferedInputStream(clientSocket.getInputStream());
						var out = new PrintWriter(clientSocket.getOutputStream(), true);) {
					var scanner = new Scanner(in, StandardCharsets.UTF_8);
					scanner.useLocale(Locale.US);
					var string = (String) null;
					while ((string = ScannerUtils.next(scanner)) != null) {
						out.println(string.toUpperCase(Locale.ROOT));
					}
				}
			}
		}
	}

}
