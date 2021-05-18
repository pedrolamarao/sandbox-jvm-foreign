package br.dev.pedrolamarao.java.nio.windows;

import java.io.IOException;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.spi.AsynchronousChannelProvider;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * AsynchronousChannelProvider with a Panama based implementation.
 */

public final class WindowsAsynchronousChannelProvider extends AsynchronousChannelProvider
{
	@Override
	public AsynchronousChannelGroup openAsynchronousChannelGroup (int nThreads, ThreadFactory threadFactory) throws IOException
	{
		return new WindowsAsynchronousChannelGroup(this, Executors.newFixedThreadPool(nThreads, threadFactory));
	}

	@Override
	public AsynchronousChannelGroup openAsynchronousChannelGroup (ExecutorService executor, int initialSize) throws IOException
	{
		return new WindowsAsynchronousChannelGroup(this, executor);
	}

	@Override
	public AsynchronousServerSocketChannel openAsynchronousServerSocketChannel (AsynchronousChannelGroup group) throws IOException
	{
		final var group_ = (WindowsAsynchronousChannelGroup) group;
		return new WindowsAsynchronousServerSocketChannel(this, group_);
	}

	@Override
	public AsynchronousSocketChannel openAsynchronousSocketChannel (AsynchronousChannelGroup group) throws IOException
	{
		final var group_ = (WindowsAsynchronousChannelGroup) group;
		return new WindowsAsynchronousSocketChannel(this, group_);
	}
}
