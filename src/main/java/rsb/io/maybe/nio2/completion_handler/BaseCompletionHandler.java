package rsb.io.maybe.nio2.completion_handler;

import org.slf4j.LoggerFactory;

import java.nio.channels.CompletionHandler;

public interface BaseCompletionHandler<A, B> extends CompletionHandler<A, B> {

	@Override
	default void failed(Throwable t, B attachment) {
		LoggerFactory.getLogger(getClass()).error("Exception working with socket", t);
	}

}
