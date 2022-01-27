package rsb.synchronicity;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Component
class Fibonacci {

	private final Executor executor;

	Fibonacci(Executor executor) {
		this.executor = executor;
	}

	public CompletableFuture<long[]> calculateWithCompletableFuture(int n) {
		var cf = new CompletableFuture<long[]>();
		this.executor.execute(() -> cf.complete(calculateSeries(n)));
		return cf;
	}

	@Async
	public CompletableFuture<long[]> calculateWithAsync(int n) {
		return CompletableFuture.completedFuture(calculateSeries(n));
	}

	@SneakyThrows
	private static long[] calculateSeries(int num) {
		Thread.sleep(1000);// artificial sleep!
		var i = 1;
		var t1 = 0;
		var t2 = 1;
		var results = new long[num + 1];
		while (i <= num) {
			int sum = t1 + t2;
			results[i] = sum;
			t1 = t2;
			t2 = sum;
			i++;
		}
		return results;

	}

}
