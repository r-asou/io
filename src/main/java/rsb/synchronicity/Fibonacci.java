package rsb.synchronicity;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Component
class Fibonacci {

	@SneakyThrows
	public static long[] calculateSeries(int num) {
		Thread.sleep(1000);// <1>
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
