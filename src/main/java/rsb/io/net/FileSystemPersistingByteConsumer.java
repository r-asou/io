package rsb.io.net;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class FileSystemPersistingByteConsumer implements Consumer<byte[]> {

	private final String prefix;

	@Override
	@SneakyThrows
	public void accept(byte[] bytes) {
		var outputDirectory = new File(new File(new File(System.getenv("HOME"), "Desktop"), "output"), this.prefix);
		Assert.isTrue(outputDirectory.mkdirs() || outputDirectory.exists(),
				() -> "the folder " + outputDirectory.getAbsolutePath() + " does not exist");
		var file = new File(outputDirectory, System.currentTimeMillis() + ".download");
		try (var fout = new FileOutputStream(file)) {
			FileCopyUtils.copy(bytes, fout);
		}
	}

}
