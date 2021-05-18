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


public final class WindowsAsynchronousSocketChannelTest
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
		try (var socket = AsynchronousSocketChannel.open(group))
		{
			socket.bind(new InetSocketAddress("127.0.0.1", 0));
		}
	}
	
	@Test
	@Timeout(value=1000, unit=TimeUnit.MILLISECONDS)
	public void connect__bound () throws Exception
	{
		try (var socket = AsynchronousSocketChannel.open(group))
		{
			socket.bind(new InetSocketAddress("127.0.0.1", 0));
			
			try (var port = AsynchronousServerSocketChannel.open())
			{
				port.bind(new InetSocketAddress("127.0.0.1", 12345));
				final var accept = port.accept();				
				final var connect = socket.connect(new InetSocketAddress("127.0.0.1", 12345));
				connect.get(500, TimeUnit.MILLISECONDS);
				final var accepted = accept.get(500, TimeUnit.MILLISECONDS);
				accepted.close();
			}
		}
	}
	
	@Test
	@Timeout(value=1000, unit=TimeUnit.MILLISECONDS)
	public void connect__unbound () throws Exception
	{
		try (var socket = AsynchronousSocketChannel.open(group))
		{			
			try (var port = AsynchronousServerSocketChannel.open())
			{
				port.bind(new InetSocketAddress("127.0.0.1", 12345));
				final var accept = port.accept();				
				final var connect = socket.connect(new InetSocketAddress("127.0.0.1", 12345));
				connect.get(500, TimeUnit.MILLISECONDS);
				final var accepted = accept.get(500, TimeUnit.MILLISECONDS);
				accepted.close();
			}
		}
	}
}
