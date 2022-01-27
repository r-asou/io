package rsb.synchronicity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Slf4j
@Component
record AsyncRunner(Fibonacci fibonacci) {

	@EventListener(ApplicationReadyEvent.class)
	public void ready() throws Exception {
		var max = 60;
		execute("calculate", () -> fibonacci.calculate(max));
		executeCompletableFuture("calculateWithAsync", () -> fibonacci.calculateWithAsync(max));
		executeCompletableFuture("calculateWithCompletableFuture", () -> fibonacci.calculateWithCompletableFuture(max));
	}

	private void execute(String func, Supplier<long[]> supplier) {
		logBefore(func);
		var results = supplier.get();
		log.info(func + " : " + results.length + " : " + Arrays.toString(results));
		logAfter(func);
	}

	private void executeCompletableFuture(String func, Supplier<CompletableFuture<long[]>> completableFuture) {
		logBefore(func);
		completableFuture.get().whenComplete((r, t) -> handle(func, r, t));
		logAfter(func);
	}

	private void handle(String func, long[] results, Throwable t) {
		log.info(func + " : " + results.length + " : " + Arrays.toString(results));
		logHandle(func);
	}

	private static void logBefore(String func) {
		log.info("before {} @ {}", func, Instant.now().toString());
	}

	private static void logAfter(String func) {
		log.info("after {} @ {}", func, Instant.now().toString());
	}

	private static void logHandle(String func) {
		log.info("handle {} @ {}", func, Instant.now().toString());
	}
}
