package rsb.io.net;

import java.util.function.Consumer;

/**
 * Receives inbound files
 */
public interface FileSyncService {

	void start(int port, Consumer<byte[]> bytesHandler) throws Exception;

}
