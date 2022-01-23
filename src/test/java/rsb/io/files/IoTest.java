package rsb.io.files;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import rsb.io.files.Bytes;
import rsb.io.files.Io;

import java.io.*;
import java.nio.file.Files;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

@Slf4j
public class IoTest {

	private final AtomicLong count = new AtomicLong();

	private final Consumer<Bytes> bytesConsumer = //
			bytes -> this.count.getAndAccumulate(bytes.length(), Long::sum);

	private final Resource resource = new ClassPathResource("/data.txt");

	private final Io io = new Io();

	private final File file = Files//
			.createTempFile("io-content-data", ".txt")//
			.toFile();

	private final CountDownLatch latch = new CountDownLatch(1);

	private final Runnable onceDone = () -> {
		log.info("counted " + this.count.get() + " bytes");
		this.latch.countDown();
	};

	public IoTest() throws IOException {
	}

	@BeforeEach
	public void before() throws IOException {
		this.count.set(0);
		try (var in = this.resource.getInputStream(); //
				var out = new FileOutputStream(this.file)//
		) {
			FileCopyUtils.copy(in, out);
		}
	}

	@AfterEach
	public void tearDown() {
		if (this.file.exists()) {
			Assertions.assertTrue(this.file.delete());
		}
	}

	@Test
	public void synchronousRead() {
		test(() -> this.io.synchronousRead(this.file, this.bytesConsumer, this.onceDone));
	}

	@Test
	public void asynchronousRead() {
		test(() -> this.io.asynchronousRead(this.file, this.bytesConsumer, this.onceDone));
	}

	private void test(Runnable r) {
		try {
			r.run();
			this.latch.await();
			Assertions.assertEquals(this.count.get(), this.file.length());
		} //
		catch (InterruptedException e) {
			log.error("something has gone wrong!", e);
		}

	}

}