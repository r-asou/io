package rsb.io.files;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
public class Main {

	public static void main(String[] args) throws Exception {
		var file = Files//
				.createTempFile("io-content-data", ".txt")//
				.toFile();
		file.deleteOnExit();

		try (var in = Main.class.getResourceAsStream("/content"); //
				var out = new FileOutputStream(file)//
		) {
			FileCopyUtils.copy(in, out);
		}
		log.info("file.length: " + file.length());
		var syncs = Map.of(//
				"nio", new AsynchronousFilesystemFileSync(), //
				"io", new SynchronousFilesystemFileSync()//
		);
		syncs.forEach((key, fileSync) -> fileSync.start(file, new BytesConsumer(file, key)));
	}

	record BytesConsumer(File source, String prefix) implements Consumer<byte[]> {

		@Override
		public void accept(byte[] bytes) {
			log.info(prefix + ':' + bytes.length + ':' + source.getAbsolutePath());
		}
	}

}
