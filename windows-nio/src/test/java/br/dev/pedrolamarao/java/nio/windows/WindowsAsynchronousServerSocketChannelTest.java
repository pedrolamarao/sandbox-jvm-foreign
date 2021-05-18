package br.dev.pedrolamarao.java.nio.windows;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;


public final class WindowsAsynchronousServerSocketChannelTest
{
	private final WindowsAsynchronousChannelProvider provider = new WindowsAsynchronousChannelProvider();

	private AsynchronousChannelGroup group;
	
	@BeforeEach
	public void beforeEach () throws IOException
	{
		 group = provider.openAsynchronousChannelGroup(ForkJoinPool.commonPool(), 0);
	}
	
	@AfterEach
	public void afterEach () throws IOException
	{
		group.shutdownNow();
	}

	@Test
	public void bind () throws IOException
	{		
		try (var serverSocket = AsynchronousServerSocketChannel.open(group))
		{
			serverSocket.bind(new InetSocketAddress("127.0.0.1", 0));
		}
	}
	
	@Test
	@Timeout(value=1000, unit=TimeUnit.MILLISECONDS)
	public void accept () throws Exception
	{
		try (var port = AsynchronousServerSocketChannel.open(group))
		{
			port.bind(new InetSocketAddress("127.0.0.1", 12345));

			final var future = port.accept();
			
			try (var link = AsynchronousSocketChannel.open())
			{
				link.connect(new InetSocketAddress("127.0.0.1", 12345)).get(500, TimeUnit.MILLISECONDS);
				
				future.get().close();
			}
		}
	}
}
