package rsb.io.net;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.AbstractReferenceCountedByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.AbstractEventExecutorGroup;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@Slf4j
class NettyNetworkFileSync implements NetworkFileSync {

	public static void main(String[] args) throws Exception {
		var nfs = new NettyNetworkFileSync();
		nfs.start(8888, new FileSystemPersistingByteConsumer("netty"));
	}

	@Override
	@SneakyThrows
	public void start(int port, Consumer<byte[]> bytesHandler) {

		var bossEventLoopGroup = new NioEventLoopGroup(1);
		var nioEventLoopGroup = new NioEventLoopGroup();
		var serverHandler = new NetworkFileSyncServerHandler(bytesHandler);
		try {
			var serverBootstrap = new ServerBootstrap();
			serverBootstrap.group(bossEventLoopGroup, nioEventLoopGroup).channel(NioServerSocketChannel.class)
					.option(ChannelOption.SO_BACKLOG, 100).handler(new LoggingHandler(LogLevel.INFO))
					.childHandler(new ChannelInitializer<SocketChannel>() {

						@Override
						public void initChannel(SocketChannel ch) {
							var channelPipeline = ch.pipeline();
							channelPipeline.addLast(serverHandler);
						}
					});
			var channelFuture = serverBootstrap.bind(port).sync();
			channelFuture.channel().closeFuture().sync();
		} //
		finally {
			shutdown(List.of(bossEventLoopGroup, nioEventLoopGroup));
		}
	}

	private static void shutdown(List<NioEventLoopGroup> groups) {
		groups.forEach(AbstractEventExecutorGroup::shutdownGracefully);
	}

}

@Slf4j
@RequiredArgsConstructor
@ChannelHandler.Sharable
class NetworkFileSyncServerHandler extends ChannelInboundHandlerAdapter {

	private final Consumer<byte[]> consumer;

	private final AtomicReference<ByteArrayOutputStream> byteArrayOutputStream = new AtomicReference<>(
			new ByteArrayOutputStream());

	@Override
	@SneakyThrows
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		if (msg instanceof AbstractReferenceCountedByteBuf buf) {
			var bytes = new byte[buf.readableBytes()];
			buf.readBytes(bytes);
			this.byteArrayOutputStream.get().write(bytes);
		}
	}

	@SneakyThrows
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		var baos = this.byteArrayOutputStream.get();
		if (null != baos) {
			try {
				var bytes = baos.toByteArray();
				if (bytes.length != 0) {
					this.consumer.accept(bytes);
				}
				// we've read the bytes,
				// time to reset for a new request
				this.byteArrayOutputStream.set(new ByteArrayOutputStream());
			} //
			finally {
				ctx.flush();
				baos.close();
			}
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		log.error("oh no!", cause);
		ctx.close();
	}

}
