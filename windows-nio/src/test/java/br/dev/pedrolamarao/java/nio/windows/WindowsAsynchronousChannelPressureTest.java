package br.dev.pedrolamarao.java.nio.windows;

import static java.lang.String.format;
import static java.lang.System.err;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public final class WindowsAsynchronousChannelPressureTest
{
	private final ExecutorService executor = Executors.newCachedThreadPool();
	
	private final WindowsAsynchronousChannelProvider provider = new WindowsAsynchronousChannelProvider();

	private AsynchronousChannelGroup group;
	
	@BeforeEach
	public void beforeEach () throws IOException
	{
		 group = provider.openAsynchronousChannelGroup(executor, 0);
	}
	
	@AfterEach
	public void afterEach () throws IOException
	{
		group.shutdownNow();
		executor.shutdownNow();
	}
	
	@Test
	public void accept__serial () throws Exception
	{
		final var target = 10000;

		final var accepted = new AtomicInteger(0);
		
		final var connected = new AsynchronousSocketChannel[target];
		
		for (int i = 0; i != target; ++i) {
			final var socket = AsynchronousSocketChannel.open();
			socket.bind(new InetSocketAddress(0));
			connected[i] = socket;
		}
		
		try (var port = AsynchronousServerSocketChannel.open(group))
		{
			port.bind(new InetSocketAddress("127.0.0.1", 12345));

			final CompletionHandler<AsynchronousSocketChannel, Void> handler = new CompletionHandler<AsynchronousSocketChannel, Void>()
			{
				@Override public void completed (AsynchronousSocketChannel socket, Void __)
				{
					port.accept(null, this);
					accepted.incrementAndGet();
					try { socket.close(); }
						catch (IOException e) { err.println(format("%s: test: failed closing accepted: %s", LocalTime.now(), e)); }
				}

				@Override public void failed (Throwable cause, Void __)
				{
					err.println(format("%s: test: failed accepting: %s", LocalTime.now(), cause));
				}
			};
			
			port.accept(null, handler);

			connect:
			for (int i = 0; i != target; ++i)
			{
				final var socket = connected[i];

				err.println(format("%s: test: connecting: %d", LocalTime.now(), i));
				final var pending = socket.connect(new InetSocketAddress("127.0.0.1", 12345));

				try
				{
					pending.get(100, TimeUnit.MILLISECONDS);
				}
				catch (Throwable cause)
				{
					err.println(format("%s: test: failed connecting: %d: %s", LocalTime.now(), i, cause));
					break connect;
				}				

				Thread.sleep(1); // #TODO: fails without this sleep
			}
		}
		
		for (int i = 0; i != target; ++i) {
			connected[i].close();
		}
		
		assertEquals(target, accepted.get());
	}
	
	@Test
	public void accept__parallel () throws Exception
	{
		final var target = 10000;
		
		final var connected = new AsynchronousSocketChannel[target];
		
		for (int i = 0; i != target; ++i) {
			final var socket = AsynchronousSocketChannel.open();
			socket.bind(new InetSocketAddress(0));
			connected[i] = socket;
		}

		final var accepted = new ArrayBlockingQueue<AsynchronousSocketChannel>(target);
		
		try (var port = AsynchronousServerSocketChannel.open(group))
		{
			port.bind(new InetSocketAddress("127.0.0.1", 12345));

			final CompletionHandler<AsynchronousSocketChannel, Void> acceptHandler = new CompletionHandler<AsynchronousSocketChannel, Void>()
			{
				@Override public void completed (AsynchronousSocketChannel socket, Void ignore)
				{
					port.accept(null, this);
					accepted.offer(socket);
				}

				@Override public void failed (Throwable cause, Void ignore)
				{
					port.accept(null, this);
					err.println(format("%s: test: accept failed: %s", LocalTime.now(), cause));
				}
			};
			
			port.accept(null, acceptHandler);
			port.accept(null, acceptHandler);

			final var pending = new Future[target];
			
			for (int i = 0; i != target; ++i)
			{
				err.println(format("%s: test: connecting: %d", LocalTime.now(), i));
				pending[i] = connected[i].connect(new InetSocketAddress("127.0.0.1", 12345));
			}

			for (int i = 0; i != target; ++i)
			{
				try
				{
					pending[i].get(200, TimeUnit.MILLISECONDS);
				}
				catch (ExecutionException e)
				{
					err.println(format("%s: test: connect failed: %d: %s", LocalTime.now(), i, e.getCause()));
				}
				catch (InterruptedException | TimeoutException e)
				{
					err.println(format("%s: test: connect failed: %d: %s", LocalTime.now(), i, e));
				}
			}
		}
		
		assertEquals(target, accepted.size());
		
		Arrays.stream(connected).forEach(this::close);
		
		accepted.forEach(this::close);
	}
	
	public void close (Closeable closeable)
	{
		try 
		{
			closeable.close(); 
		} 
		catch (IOException e) 
		{
			err.println(format("%s: test: close failed: %s", LocalTime.now(), e));
		}
	}
}
