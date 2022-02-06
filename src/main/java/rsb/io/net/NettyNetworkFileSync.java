package rsb.io.net;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Slf4j
@Component
class NettyNetworkFileSync implements NetworkFileSync {

	@Override
	@SneakyThrows
	public void start(int port, Consumer<byte[]> bytesHandler) {

		var nioEventLoopGroup = new NioEventLoopGroup();// <2>
		var serverHandler = new NettyNetworkFileSyncServerHandler(bytesHandler);
		try {
			// <3>
			var serverBootstrap = new ServerBootstrap().group(nioEventLoopGroup)//
					.channel(NioServerSocketChannel.class)//
					.option(ChannelOption.SO_BACKLOG, 100)//
					.handler(new LoggingHandler(LogLevel.INFO))//
					.childHandler(new ChannelInitializer<SocketChannel>() {

						@Override
						public void initChannel(SocketChannel ch) {
							var channelPipeline = ch.pipeline();
							channelPipeline.addLast(serverHandler);
						}
					});
			// <4>
			var channelFuture = serverBootstrap.bind(port).sync();
			channelFuture.channel().closeFuture().sync();
		} //
		finally {
			nioEventLoopGroup.shutdownGracefully();
		}
	}

}
