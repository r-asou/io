package rsb.io.files;

import org.springframework.util.ReflectionUtils;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

class Io {

	public void synchronousRead(File source, Consumer<Bytes> onBytes, Runnable whenDone) {
		try {
			var synchronous = new Synchronous();
			synchronous.read(source, onBytes, whenDone);
		}
		catch (IOException e) {
			ReflectionUtils.rethrowRuntimeException(e);
		}
	}

	public void asynchronousRead(File source, Consumer<Bytes> onBytes, Runnable whenDone) {
		try {
			var asynchronous = new Asynchronous();
			asynchronous.read(source, onBytes, whenDone);
		}
		catch (Exception ex) {
			ReflectionUtils.rethrowRuntimeException(ex);
		}
	}

}
