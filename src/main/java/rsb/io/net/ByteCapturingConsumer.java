package rsb.io.net;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.function.Consumer;

@Slf4j
record ByteCapturingConsumer(String prefix) implements Consumer<byte[]> {

	@Override
	public void accept(byte[] bytes) {
		log.info("the bytes length is " + bytes.length);
		var outputDirectory = new File(new File(System.getenv("HOME"), "Desktop"), "output");
		Assert.isTrue(outputDirectory.mkdirs() || outputDirectory.exists(),
				() -> "the folder " + outputDirectory.getAbsolutePath() + " does not exist");
		var file = new File(outputDirectory, this.prefix + ".download");
		try (var fous = new FileOutputStream(file)) {
			FileCopyUtils.copy(bytes, fous);
		} //
		catch (Exception e) {
			log.error("oops!", e);
		}
	}
}
