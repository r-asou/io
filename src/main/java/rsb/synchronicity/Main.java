package rsb.synchronicity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;

// <1>
@EnableAsync // <2>
@SpringBootApplication
public class Main implements AsyncConfigurer {

	private final int max = 12; // 11 is almost instantaneous! 12 takes muuuch longer

	private final Executor executor;

	@Bean
	AsyncRunner asyncRunner(LongRunningAlgorithm algorithm) {
		return new AsyncRunner(algorithm, this.max);
	}

	@Bean
	SyncRunner syncRunner(LongRunningAlgorithm algorithm) {
		return new SyncRunner(algorithm, this.max);
	}

	Main(Executor executor) {
		this.executor = executor;
	}

	@Override
	public Executor getAsyncExecutor() {
		return this.executor;
	}

	public static void main(String[] args) throws Exception {
		SpringApplication.run(Main.class, args);
	}

}
