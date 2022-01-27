package rsb.synchronicity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
record AsyncRunner(Fibonacci fibonacci) {

	@EventListener(ApplicationReadyEvent.class)
	public void ready() throws Exception {
		var max = 60;

		var one = fibonacci.calculateWithAsync(max);
		execute("calculateWithAsync", one);

		var two = fibonacci.calculateWithCompletableFuture(max);
		execute("calculateWithCompletableFuture", two);
	}

	private void execute(String func, CompletableFuture<long[]> completableFuture) {
		log.info("before {} @ {}", func, Instant.now().toString());
		completableFuture.whenComplete((r, t) -> handle(func, r, t));
		log.info("after {} @ {}", func, Instant.now().toString());
	}

	private void handle(String func, long[] results, Throwable t) {
		log.info(func + " : " + results.length + " : " + Arrays.toString(results));
	}
}
