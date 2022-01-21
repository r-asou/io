package rsb.io.nio;

import lombok.extern.slf4j.Slf4j;
import rsb.io.nio.client.NioEchoClient;
import rsb.io.nio.service.NioEchoServer;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Main {

	public static void main(String[] a) throws Exception {
		var port = 8008;
		var executor = Executors.newFixedThreadPool(5);
		var server = new NioEchoServer(executor);
		var client = new NioEchoClient();
		executor.submit(() -> server.start(port));
		executor.submit(() -> {
			try {
				TimeUnit.MILLISECONDS.sleep(500);
			} //
			catch (InterruptedException e) {
				log.error("oops!", e);
			}
			client.start(port);
		});
		Thread.currentThread().join();
	}

}
