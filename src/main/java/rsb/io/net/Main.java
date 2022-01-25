package rsb.io.net;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.function.Consumer;

@Slf4j
@SpringBootApplication
public class Main {

	public static void main(String[] args) {
		SpringApplication.run(Main.class, args);
	}

	@Bean
	Consumer<NetworkFileSyncBytes> consumer() {
		return bytes -> {
			log.info("the bytes length is " + bytes.bytes().length);
			var outputDirectory = new File(new File(System.getenv("HOME"), "Desktop"), "output");
			Assert.isTrue(outputDirectory.mkdirs() || outputDirectory.exists(),
					() -> "the folder " + outputDirectory.getAbsolutePath() + " does not exist");
			var file = new File(outputDirectory, bytes.prefix() + ".download");
			try (var fous = new FileOutputStream(file)) {
				FileCopyUtils.copy(bytes.bytes(), fous);
			} //
			catch (Exception e) {
				log.error("oops!", e);
			}
		};
	}

	@Bean
	ApplicationRunner runner(NetworkFileSync nfs, Consumer<NetworkFileSyncBytes> consumer) {
		log.info("using " + nfs.getClass().getName());
		return args -> nfs.start(8888, consumer);
	}

	@Bean
	@Profile("netty")
	NetworkFileSync netty() {
		return new NettyNetworkFileSync();
	}

	@Bean
	@Profile("io")
	NetworkFileSync io() {
		return new IoNetworkFileSync();
	}

	@Bean
	@Profile("nio")
	NetworkFileSync nio() {
		return new NioNetworkFileSync();
	}

}
