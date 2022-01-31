package rsb.io.files;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.ByteBuffer;

// <1>
record AsynchronousReadAttachment(File source, ByteBuffer buffer, ByteArrayOutputStream byteArrayOutputStream,
		long position) {
}
