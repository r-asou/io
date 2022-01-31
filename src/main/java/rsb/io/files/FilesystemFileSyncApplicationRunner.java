package rsb.io.files;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

@Slf4j
record FilesystemFileSyncApplicationRunner(Map<String, FilesystemFileSync> filesystemFileSyncMap, // <1>
		ExecutorService executorService) implements ApplicationRunner {

	@Override
	public void run(ApplicationArguments args) {
		var countDownLatch = new CountDownLatch(this.filesystemFileSyncMap.size()); // <2>
		var file = this.createTempFile();// <3>
		this.executorService.submit(() -> {// <4>
			try {
				countDownLatch.await();
				this.executorService.shutdown();
				log.info("shutdown()");
			} //
			catch (InterruptedException e) {
				log.error("something went wrong!", e);
			}
		});
		this.filesystemFileSyncMap.forEach((beanName, filesystemFileSync) -> {// <5>
			filesystemFileSync.start(file, bytes -> {
				log.info(beanName + ", " + bytes.length + " bytes, " + file.getAbsolutePath());
				countDownLatch.countDown();
				log.info("countDown()");
			});
		});
	}

	@SneakyThrows
	private File createTempFile() {
		var file = Files.createTempFile("rsb-io-content-data", ".txt").toFile();
		file.deleteOnExit();
		try (var in = Main.class.getResourceAsStream("/content"); var out = new FileOutputStream(file)) {
			FileCopyUtils.copy(in, out);
		}
		return file;
	}

}
