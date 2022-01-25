package rsb.io.files;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * This example is not non-blocking, but it is <em>asynchronous</em>. The client of the
 * API doesn't have to wait for the response, though there is threading behind the scenes.
 *
 */
@Slf4j
class AsynchronousFilesystemFileSync implements FilesystemFileSync {

	record ReadAttachment(File source, ByteBuffer buffer, ByteArrayOutputStream byteArrayOutputStream, long position) {
	}

	@Override
	@SneakyThrows
	public void start(File source, Consumer<byte[]> handler) {
		var executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		var fileChannel = AsynchronousFileChannel.open(source.toPath(), Collections.singleton(StandardOpenOption.READ),
				executorService);
		var completionHandler = new CompletionHandler<Integer, ReadAttachment>() {

			@Override
			@SneakyThrows
			public void completed(Integer result, ReadAttachment attachment) {
				var byteArrayOutputStream = attachment.byteArrayOutputStream();
				if (!result.equals(-1)) {
					var buffer = attachment.buffer();
					buffer.flip();
					var storage = new byte[buffer.limit()];
					buffer.get(storage);
					byteArrayOutputStream.write(storage);
					attachment.buffer().clear();
					var ra = new ReadAttachment(source, attachment.buffer(), //
							byteArrayOutputStream, //
							attachment.position() + attachment.buffer().limit() //
					);
					fileChannel.read(attachment.buffer(), ra.position(), ra, this);
				} //
				else { // it's -1
					var all = byteArrayOutputStream.toByteArray();
					try {
						byteArrayOutputStream.close();
						executorService.shutdown();
					} //
					catch (Exception e) {
						error(e, attachment);
					}
					handler.accept(all);

				}
			}

			@Override
			public void failed(Throwable throwable, ReadAttachment attachment) {
				error(throwable, attachment);
			}
		};
		var attachment = new ReadAttachment(source, ByteBuffer.allocate(1024), new ByteArrayOutputStream(), 0);
		fileChannel.read(attachment.buffer(), attachment.position(), attachment, completionHandler);
	}

	private static void error(Throwable throwable, ReadAttachment attachment) {
		log.error("error reading file '" + attachment.source().getAbsolutePath() + "'!", throwable);
	}

}
