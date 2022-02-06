package rsb.io.net;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.util.SocketUtils;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;

@Slf4j
@SpringBootApplication
public class Main {

	public static void main(String[] args) {
		SpringApplication.run(Main.class, args);
	}

	// <1>
	@Bean
	ApplicationRunner runner(Map<String, NetworkFileSync> networkFileSyncMap, Executor executor) {
		return args -> networkFileSyncMap.forEach((beanName, nfs) -> {
			var port = SocketUtils.findAvailableTcpPort(8008);// <1>
			var classSimpleName = nfs.getClass().getSimpleName().toLowerCase(Locale.ROOT);
			log.info("running " + classSimpleName + " on port " + port);
			// <2>
			executor.execute(() -> nfs.start(port, // <3>
					bytes -> log.info(beanName + " read " + bytes.length + " bytes")));
		});
	}

}
