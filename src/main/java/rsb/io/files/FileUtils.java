package rsb.io.files;

import lombok.SneakyThrows;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;

public class FileUtils {

	@SneakyThrows
	public static File setup() {
		var file = Files//
				.createTempFile("io-content-data", ".txt")//
				.toFile();
		file.deleteOnExit();
		try (var in = new ClassPathResource("/content").getInputStream(); var out = new FileOutputStream(file)) {
			FileCopyUtils.copy(in, out);
		}
		return file;
	}

}
