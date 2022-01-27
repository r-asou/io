package rsb.synchronicity.async;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import rsb.synchronicity.FibonacciCalculator;

import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@EnableAsync
@SpringBootApplication
public class Main implements AsyncConfigurer {

	private final Executor executor;

	Main(Executor executor) {
		this.executor = executor;
	}

	@Override
	public Executor getAsyncExecutor() {
		return this.executor;
	}

	@Bean
	ApplicationRunner runner(Fibonacci fibonacci) {
		return args -> {

			var max = 60;

			var one = fibonacci.calculateWithAsync(max);
			execute("calculateWithAsync", one);

			var two = fibonacci.calculateWithCompletableFuture(max);
			execute("calculateWithCompletableFuture", two);
		};
	}

	private void execute(String func, CompletableFuture<long[]> completableFuture) {
		log.info("before {} @ {}", func, Instant.now().toString());
		completableFuture.whenComplete((r, t) -> handle(func, r, t));
		log.info("after {} @ {}", func, Instant.now().toString());
	}

	private void handle(String func, long[] results, Throwable t) {
		log.info(func + " : " + results.length + " : " + Arrays.toString(results));
	}

	public static void main(String args[]) throws Exception {
		SpringApplication.run(Main.class, args);
		Thread.sleep(1_000);
	}

}

@Slf4j
@Component
class Fibonacci {

	private final Executor executor;

	Fibonacci(Executor executor) {
		this.executor = executor;
	}

	public CompletableFuture<long[]> calculateWithCompletableFuture(int n) {
		var cf = new CompletableFuture<long[]>();
		this.executor.execute(() -> cf.complete(FibonacciCalculator.calculateSeries(n)));
		return cf;
	}

	@Async
	public CompletableFuture<long[]> calculateWithAsync(int n) {
		return CompletableFuture.completedFuture(FibonacciCalculator.calculateSeries(n));
	}

}