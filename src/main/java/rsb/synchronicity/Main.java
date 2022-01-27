package rsb.synchronicity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;

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

	public static void main(String args[]) throws Exception {
		SpringApplication.run(Main.class, args);
		Thread.sleep(1_000);
	}

}
