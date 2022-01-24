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
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Slf4j
class Asynchronous {

	record ReadAttachment(ByteBuffer buffer, long position) {
	}

	@SneakyThrows
	static void read(File file, Consumer<byte[]> consumer) {
		var executorService = Executors.newSingleThreadExecutor();
		var fileChannel = AsynchronousFileChannel.open(file.toPath(), Collections.singleton(StandardOpenOption.READ),
				executorService);
		var byteBuffer = ByteBuffer.allocate(1024);
		var byteArrayOutputStream = new ByteArrayOutputStream();
		var completionHandler = new CompletionHandler<Integer, ReadAttachment>() {

			@Override
			@SneakyThrows
			public void completed(Integer result, ReadAttachment attachment) {
				log.info("result: " + result);
				if (!result.equals(-1)) {
					var buffer = attachment.buffer();
					buffer.flip();
					var storage = new byte[buffer.limit()];
					buffer.get(storage);
					byteArrayOutputStream.write(storage);
					attachment.buffer().clear();
					var ra = new ReadAttachment(attachment.buffer(),
							attachment.position() + attachment.buffer().limit());
					fileChannel.read(attachment.buffer(), ra.position(), ra, this);
				} //
				else { // it's -1
					var all = byteArrayOutputStream.toByteArray();
					try {
						byteArrayOutputStream.close();
					} //
					catch (Exception e) {
						log.error("oops!", e);
					}
					consumer.accept(all);
				}
			}

			@Override
			public void failed(Throwable throwable, ReadAttachment attachment) {
				log.error("something has gone terribly wrong!", throwable);
			}
		};
		var ra = new ReadAttachment(byteBuffer, 0);
		fileChannel.read(byteBuffer, ra.position(), ra, completionHandler);

	}

	public static void main(String[] args) throws Exception {
		var file = FileUtils.setup();
		log.info("file read start");
		read(file, bytes -> log.info("read " + bytes.length + " and the file is " + file.length()));
		log.info("file read stop");

	}

}
