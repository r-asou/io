package rsb.io.files;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.StandardOpenOption;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

@Slf4j
record AsynchronousFilesystemFileSync(ExecutorService executorService) implements FilesystemFileSync {

	@Override
	@SneakyThrows
	public void start(File source, Consumer<byte[]> consumer) {
		var fileChannel = AsynchronousFileChannel.open(source.toPath(), Set.of(StandardOpenOption.READ),
				this.executorService);// <2>
		var completionHandler = new AsynchronousFileCompletionHandler(fileChannel, source, consumer); // <3>
		var attachment = new AsynchronousReadAttachment(source, ByteBuffer.allocate(1024), new ByteArrayOutputStream(),
				0); // <4>
		fileChannel.read(attachment.buffer(), attachment.position(), attachment, completionHandler); // <5>
	}
}
