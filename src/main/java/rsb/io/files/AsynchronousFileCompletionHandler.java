package rsb.io.files;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.util.function.Consumer;

@Slf4j
record AsynchronousFileCompletionHandler(AsynchronousFileChannel fileChannel, //
		File source, //
		Consumer<byte[]> handler)//
		implements
			CompletionHandler<Integer, AsynchronousReadAttachment> {

	// <1>
	@Override
	@SneakyThrows
	public void completed(Integer result, AsynchronousReadAttachment attachment) {
		var byteArrayOutputStream = attachment.byteArrayOutputStream();

		if (!result.equals(-1)) {// <2>

			var buffer = attachment.buffer();
			buffer.flip();

			var storage = new byte[buffer.limit()];
			buffer.get(storage);
			byteArrayOutputStream.write(storage);
			buffer.clear();

			var readAttachment = new AsynchronousReadAttachment(this.source, attachment.buffer(), byteArrayOutputStream,
					attachment.position() + attachment.buffer().limit());
			this.fileChannel.read(attachment.buffer(), readAttachment.position(), readAttachment, this);// <3>
		} //
		else { // <4>

			var all = byteArrayOutputStream.toByteArray();
			try {
				byteArrayOutputStream.close();
			} //
			catch (Exception e) {
				log.error("error reading file '" + attachment.source().getAbsolutePath() + "'!", e);
			}
			this.handler.accept(all);
		}
	}

	@Override
	public void failed(Throwable throwable, AsynchronousReadAttachment attachment) {
		log.error("error reading file '" + attachment.source().getAbsolutePath() + "'!", throwable);
	}
}
