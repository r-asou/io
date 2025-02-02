package rsb.io.files;

import lombok.SneakyThrows;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.function.Consumer;

class SynchronousFilesystemFileSync implements FilesystemFileSync {

	@Override
	@SneakyThrows
	public void start(File source, Consumer<byte[]> consumer) {

		try (// <1>
				var in = new BufferedInputStream(new FileInputStream(source)); //
				var out = new ByteArrayOutputStream() //
		) {
			var read = -1;
			var bytes = new byte[1024];
			while ((read = in.read(bytes)) != -1) { // <2>
				out.write(bytes, 0, read);
			}
			// <3>
			consumer.accept(out.toByteArray());
		}
	}

}
