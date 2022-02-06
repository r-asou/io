package rsb.io.files;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class Main {

	public static void main(String[] args) throws Exception {
		System.setProperty("spring.profiles.active", "files");
		SpringApplication.run(Main.class, args);
	}

	@Bean // <1>
	ApplicationRunner runner(Map<String, FilesystemFileSync> map, ExecutorService executorService) {
		return new FilesystemFileSyncApplicationRunner(map, executorService);
	}

	@Bean
	ExecutorService executorService() {
		return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	}

	@Bean
	FilesystemFileSync synchronous() {
		return new SynchronousFilesystemFileSync();
	}

	@Bean
	FilesystemFileSync asynchronous(ExecutorService executorService) {
		return new AsynchronousFilesystemFileSync(executorService);
	}

}
