package rsb.io.net;

import java.util.function.Consumer;

interface NetworkFileSync {

	// <1>
	void start(int port, Consumer<byte[]> bytesHandler);

}
