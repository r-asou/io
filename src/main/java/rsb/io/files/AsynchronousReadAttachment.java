package rsb.io.files;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;

// <1>
record AsynchronousReadAttachment(File source, ByteBuffer buffer, ByteArrayOutputStream byteArrayOutputStream,
		long position) {
}
