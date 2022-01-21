package rsb.io.nio;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
public class Main {

	public static void main(String[] a) throws Exception {
		var readerForStandardIn = (Supplier<BufferedReader>) () -> new BufferedReader(new InputStreamReader(System.in));
		var stringBufferedReaderFunction = (Function<String, BufferedReader>) text -> new BufferedReader(
				new StringReader(text));
		var readerSupplier = (Supplier<BufferedReader>) () -> stringBufferedReaderFunction.apply("""
				bonjour
				nihao
				buon giorno
				hola
				""".strip().trim());
		var port = 8008;
		var executor = Executors.newFixedThreadPool(5);
		var server = new NioEchoServer(executor);
		var client = new NioEchoClient(readerSupplier);
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
