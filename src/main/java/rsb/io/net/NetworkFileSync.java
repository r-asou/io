package rsb.io.net;

import java.util.function.Consumer;

/**
 * Receives inbound files
 */
public interface NetworkFileSync {

	void start(int port, Consumer<byte[]> bytesHandler) throws Exception;

}
