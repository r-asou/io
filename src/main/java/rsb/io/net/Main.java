package rsb.io.net;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@Slf4j
@SpringBootApplication
public class Main {

	public static void main(String[] args) {
		var appProfile = Profiles.NIO;
		var profileString = appProfile.name().toLowerCase(Locale.ROOT);
		System.setProperty("spring.profiles.active", "net," + profileString);
		SpringApplication.run(Main.class, args);
	}

	@Bean
	ApplicationRunner runner(Map<String, NetworkFileSync> syncs, Executor executor) {
		var portIncrementer = new AtomicInteger(8008);
		return args -> syncs.forEach((beanName, nfs) -> {
			var port = portIncrementer.incrementAndGet();
			log.info("running " + nfs.getClass().getName() + " on port " + port);
			var classSimpleName = nfs.getClass().getSimpleName();
			var consumer = new ByteCapturingConsumer(classSimpleName.toLowerCase(Locale.ROOT));
			executor.execute(() -> nfs.start(port, consumer));
		});
	}

	@Bean
	NetworkFileSync netty() {
		return new NettyNetworkFileSync();
	}

	@Bean
	NetworkFileSync io() {
		return new IoNetworkFileSync();
	}

	@Bean
	NetworkFileSync nio() {
		return new NioNetworkFileSync();
	}

	enum Profiles {

		NIO, NETTY, IO

	}

}
