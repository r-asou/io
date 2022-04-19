package rsb.io.files.nio;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import rsb.io.files.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * This example is not non-blocking, but it is <em>asynchronous</em>. The client of the
 * API doesn't have to wait for the response, though there is threading behind the scenes.
 *
 * @author Josh Long
 */
@Slf4j
class Asynchronous {

	record ReadAttachment(ByteBuffer buffer, ByteArrayOutputStream byteArrayOutputStream, long position) {
	}

	@SneakyThrows
	static void read(File file, Consumer<byte[]> consumer, ExecutorService executorService) {
//  var executorService = Executors.newSingleThreadExecutor();
		var fileChannel = AsynchronousFileChannel.open(file.toPath(), Collections.singleton(StandardOpenOption.READ),
				executorService);
		var completionHandler = new CompletionHandler<Integer, ReadAttachment>() {

			@Override
			@SneakyThrows
			public void completed(Integer result, ReadAttachment attachment) {
				if (!result.equals(-1)) {
					var buffer = attachment.buffer();
					buffer.flip();
					var storage = new byte[buffer.limit()];
					buffer.get(storage);
					attachment.byteArrayOutputStream().write(storage);
					attachment.buffer().clear();
					var ra = new ReadAttachment(attachment.buffer(), //
							attachment.byteArrayOutputStream(), //
							attachment.position() + attachment.buffer().limit() //
					);
					fileChannel.read(attachment.buffer(), ra.position(), ra, this);
				} //
				else { // it's -1
					var all = attachment.byteArrayOutputStream().toByteArray();
					try {
						attachment.byteArrayOutputStream().close();
					} //
					catch (Exception e) {
						log.error("oops!", e);
					}
					consumer.accept(all);
					log.info("file read stop");
				}
			}

			@Override
			public void failed(Throwable throwable, ReadAttachment attachment) {
				log.error("something has gone terribly wrong!", throwable);
			}
		};
		var attachment = new ReadAttachment(ByteBuffer.allocate(1024), new ByteArrayOutputStream(), 0);
		fileChannel.read(attachment.buffer(), attachment.position(), attachment, completionHandler);

	}

	public static void main(String[] args) throws Exception {
		var file = FileUtils.setup();
		log.info("file read start");
		var executorService = Executors.newSingleThreadExecutor();
		read(file, bytes -> log.info("read " + bytes.length + " and the file is " + file.length()), executorService);
		Thread.sleep(5000L);
		executorService.shutdown();
	}

}