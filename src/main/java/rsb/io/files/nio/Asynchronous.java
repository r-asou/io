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
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

@Slf4j
class Asynchronous {

	@SneakyThrows
	static void read(File file, Consumer<byte[]> consumer) {
		var es = Executors.newSingleThreadExecutor();
		var fileChannel = AsynchronousFileChannel.open(file.toPath(), Collections.singleton(StandardOpenOption.READ),
				es);
		var chunk = 1024;
		var buffer = ByteBuffer.allocate(chunk);
		var total = new AtomicLong(0);

		var baos = new ByteArrayOutputStream();
		var ch = new CompletionHandler<Integer, ReadAttachment>() {

			@Override
			@SneakyThrows
			public void completed(Integer result, ReadAttachment attachment) {
				total.set(total.get() + result);
				log.info("result: " + result);
				if (!result.equals(-1)) {

					var b = attachment.buffer();
					b.flip();
					var storage = new byte[b.limit()];
					b.get(storage);
					baos.write(storage);
					var np = new AtomicLong(attachment.position().get() + chunk);
					var ra = new ReadAttachment(attachment.buffer(), np);
					attachment.buffer().clear();

					fileChannel.read(attachment.buffer(), np.get(), ra, this);
				} //
				else { // it's -1
					log.info("got 'em all! " + total.get());
					var all = baos.toByteArray();
					try {
						baos.close();
					} //
					catch (Exception e) {
						log.error("oops!", e);
					}

					consumer.accept(all);

				}

			}

			@Override
			public void failed(Throwable exc, ReadAttachment attachment) {
				log.error("can't read any more", exc);
			}
		};
		var ra = new ReadAttachment(buffer, new AtomicLong());
		fileChannel.read(buffer, ra.position().get(), ra, ch);

	}

	public static void main(String[] args) throws Exception {
		var file = FileUtils.setup();
		log.info("file read start");
		read(file, bytes -> log.info("read " + bytes.length + " and the file is " + file.length()));
		log.info("file read stop");

	}

	record ReadAttachment(ByteBuffer buffer, AtomicLong position) {
	}
	/*
	 * private final ExecutorService executorService = Executors.newFixedThreadPool(10);
	 *
	 * private int bytesRead;
	 *
	 * private long position;
	 *
	 * private AsynchronousFileChannel fileChannel;
	 *
	 * private Consumer<Bytes> consumer;
	 *
	 * private Runnable finished;
	 *
	 * public void read(File file, Consumer<Bytes> c, Runnable finished) throws
	 * IOException { this.consumer = c; this.finished = finished; var path =
	 * file.toPath(); // <1> this.fileChannel = AsynchronousFileChannel.open(path,
	 * Collections.singleton(StandardOpenOption.READ), this.executorService); // <2> var
	 * buffer = ByteBuffer.allocate(FileCopyUtils.BUFFER_SIZE);
	 * this.fileChannel.read(buffer, position, buffer, this); // <3> while (this.bytesRead
	 * > 0) { this.position = this.position + this.bytesRead;
	 * this.fileChannel.read(buffer, this.position, buffer, this); } }
	 *
	 * @Override public void completed(Integer result, ByteBuffer buffer) {
	 *
	 * this.bytesRead = result; // <4>
	 *
	 * if (this.bytesRead < 0) { this.finished.run(); return; }
	 *
	 * buffer.flip();
	 *
	 * var data = new byte[buffer.limit()]; buffer.get(data);
	 *
	 * // <5> consumer.accept(Bytes.from(data, data.length));
	 *
	 * buffer.clear();
	 *
	 * this.position = this.position + this.bytesRead; this.fileChannel.read(buffer,
	 * this.position, buffer, this); }
	 *
	 * @Override public void failed(Throwable exc, ByteBuffer attachment) {
	 * log.error("something has gone wrong", exc); }
	 */

}
