package rsb.io.files;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Main {

	@Bean
	FilesystemFileSync synchronous() {
		return new SynchronousFilesystemFileSync();
	}

	@Bean
	FilesystemFileSync asynchronous() {
		return new AsynchronousFilesystemFileSync();
	}

	public static void main(String[] args) {
		System.setProperty("spring.profiles.active", "files");
		SpringApplication.run(Main.class, args);
	}

	@Bean
	ApplicationRunner runner(Map<String, FilesystemFileSync> fss) throws Exception {
		return args -> fss.forEach((beanName, fileSync) -> {
			var file = this.createTempFile();
			fileSync.start(file, bytes -> log.info(beanName + ':' + bytes.length + ':' + file.getAbsolutePath()));
			this.sleep();
		});
	}

	@SneakyThrows
	private void sleep() {
		TimeUnit.MILLISECONDS.sleep(100);
	}

	@SneakyThrows
	private File createTempFile() {
		var file = Files//
				.createTempFile("rsb-io-content-data", ".txt")//
				.toFile();
		file.deleteOnExit();
		try (var in = Main.class.getResourceAsStream("/content"); //
				var out = new FileOutputStream(file)//
		) {
			FileCopyUtils.copy(in, out);
		}
		return file;
	}

}
