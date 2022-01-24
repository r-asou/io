package rsb.io.net.io;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Scanner;

@Slf4j
public class Client {

	public static void main(String[] args) throws Exception {
		var host = "127.0.0.1";
		var port = 8888;
		try (var socket = new Socket(host, port);
				var in = new BufferedInputStream(socket.getInputStream());
				var out = new PrintWriter(socket.getOutputStream())) {
			var scanner = new Scanner(in, StandardCharsets.UTF_8);
			scanner.useLocale(Locale.US);
			out.println("This is a test");
			out.flush();
			log.info("next line: " + ScannerUtils.next(scanner));

		}
	}

}
