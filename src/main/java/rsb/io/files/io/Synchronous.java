package rsb.io.files.io;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import rsb.io.files.FileUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.function.Consumer;

@Slf4j
class Synchronous {

	@SneakyThrows
	static void read(File file, Consumer<byte[]> consumer) {
		try (var in = new BufferedInputStream(new FileInputStream(file)); var out = new ByteArrayOutputStream()) {
			var read = -1;
			var bytes = new byte[1024];
			while ((read = in.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			consumer.accept(out.toByteArray());
		}
	}

	public static void main(String[] args) {
		var file = FileUtils.setup();
		log.info("file read start");
		read(file, bytes -> log.info("read " + bytes.length + " and the file is " + file.length()));
		log.info("file read stop");
	}

}
