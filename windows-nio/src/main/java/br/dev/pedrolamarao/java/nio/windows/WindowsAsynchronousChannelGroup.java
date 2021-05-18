package br.dev.pedrolamarao.java.nio.windows;

import java.io.IOException;
import java.nio.channels.AsynchronousChannelGroup;
import java.time.Duration;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import br.dev.pedrolamarao.java.nio.windows.internal.Bus;
import br.dev.pedrolamarao.java.nio.windows.internal.IoDevice;

/**
 * AsynchronousChannelGroup with a Panama based implementation.
 */

public final class WindowsAsynchronousChannelGroup extends AsynchronousChannelGroup
{
	private final Bus bus;
	
	private long counter = 1;
	
	private final ExecutorService executor;
	
	private final HashMap<Long, WindowsChannel> map = new HashMap<>();
	
	public WindowsAsynchronousChannelGroup (WindowsAsynchronousChannelProvider provider, ExecutorService executor)
	{
		super(provider);
		
		this.executor = executor;
		try { this.bus = new Bus(); }
			catch (Throwable e) { throw new RuntimeException("<init>: failed creating bus", e); }
		
		executor.submit(this::run);
	}
	
	@Override
	public boolean isShutdown ()
	{
		return executor.isShutdown();
	}

	@Override
	public boolean isTerminated ()
	{
		return executor.isTerminated();
	}

	@Override
	public void shutdown ()
	{
		executor.shutdown();
		
		// #TODO: stop workers
	}

	@Override
	public void shutdownNow () throws IOException
	{
		executor.shutdownNow();
		
		// #TODO: stop workers
	}

	@Override
	public boolean awaitTermination (long timeout, TimeUnit unit) throws InterruptedException
	{
		return executor.awaitTermination(timeout, unit);
	}
	
	@SuppressWarnings("exports")
	public long register (IoDevice device, WindowsChannel channel)
	{
		try
		{
			final var key = counter++;
			map.put(key, channel);
			bus.register(key, device);
			return key;
		} 
		catch (Throwable e)
		{
			throw new RuntimeException("register: failed", e);
		}
	}
	
	public void unregister (long key)
	{
		map.remove(key);
	}
	
	public Future<?> submit (Runnable task)
	{
		return executor.submit(task);
	}

	public void run ()
	{
		try
		{
			while (true)
			{
				final var pulled = bus.pull(Duration.ofMillis(Long.MAX_VALUE));
				if (pulled.isEmpty()) {
					// what!?
					continue;
				}
				
				final var event = pulled.get();
				if (event.key() == 0) {
					break;
				}				
				
				final var channel = map.get(event.key());
				if (channel == null) {
					// what!?
					continue;
				}
				
				executor.submit( () -> channel.complete(event.operation().toRawLongValue(), event.status(), event.data()) );
			}
		} 
		catch (Throwable e)
		{
			throw new RuntimeException("run: failed", e);
		}
	}
}
