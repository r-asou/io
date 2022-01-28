package rsb.synchronicity;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@RequiredArgsConstructor
class FibonacciService {

	private final Executor executor;

	// <1>
	public long[] calculate(int n) {
		return Fibonacci.calculateSeries(n);
	}

	// <2>
	public CompletableFuture<long[]> calculateWithCompletableFuture(int n) {
		var cf = new CompletableFuture<long[]>();
		this.executor.execute(() -> cf.complete(Fibonacci.calculateSeries(n)));
		return cf;
	}

	@Async
	public CompletableFuture<long[]> calculateWithAsync(int n) {
		return CompletableFuture.completedFuture(Fibonacci.calculateSeries(n));
	}

}
