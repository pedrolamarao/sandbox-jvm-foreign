package br.dev.pedrolamarao.java.nio.windows;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import br.dev.pedrolamarao.java.foreign.windows.Ws2_32;
import br.dev.pedrolamarao.java.nio.windows.internal.Link;
import br.dev.pedrolamarao.java.nio.windows.internal.Operation;
import br.dev.pedrolamarao.java.nio.windows.internal.OperationState;
import jdk.incubator.foreign.MemoryLayout.PathElement;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.ResourceScope;
/**
 * AsynchronousSocketChannel with a Panama based implementation.
 */

public final class WindowsAsynchronousSocketChannel extends AsynchronousSocketChannel implements WindowsChannel
{
	private interface IoState
	{
		void failed (Throwable cause);
		
		Operation operation ();
		
		void succeeded (int data);
	}
	
	@SuppressWarnings({ "preview", "exports" })
	public static final record ConnectState (Operation operation, Object context, CompletionHandler<Void, Object> handler) implements IoState
	{
		public void failed (Throwable cause)
		{
			handler.failed(cause, context);			
		}
		
		public void succeeded (int data)
		{
			handler.completed(null, context);			
		}
	}
	
	private final WindowsAsynchronousChannelGroup group;
	
	private boolean isOpen;
	
	private long key;
	
	private Link link;
	
	private final HashMap<Long, IoState> pending = new HashMap<>();
	
	public WindowsAsynchronousSocketChannel (WindowsAsynchronousChannelProvider provider, WindowsAsynchronousChannelGroup group)
	{
		super(provider);
	
		this.group = group;
		this.isOpen = false;
		this.key = 0;
		this.link = null;
	}
	
	@SuppressWarnings("exports")
	public WindowsAsynchronousSocketChannel (WindowsAsynchronousChannelProvider provider, WindowsAsynchronousChannelGroup group, Link link)
	{
		super(provider);
		
		this.group = group;
		this.isOpen = true;
		this.key = group.register(link, this);
		this.link = link;
	}

	@Override
	public void close () throws IOException
	{
		if (link != null) {
			group.unregister(key);
			link.close();
		}
	}
	
	//

	@Override
	public boolean isOpen ()
	{
		return isOpen;
	}
	
	//

	@Override
	public SocketAddress getLocalAddress () throws IOException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SocketAddress getRemoteAddress () throws IOException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<SocketOption<?>> supportedOptions ()
	{
		return null;
	}

	@Override
	public <T> T getOption (SocketOption<T> name) throws IOException
	{
		return null;
	}

	@Override
	public <T> AsynchronousSocketChannel setOption (SocketOption<T> name, T value) throws IOException
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	//
	
	@Override
	public AsynchronousSocketChannel bind (SocketAddress local) throws IOException
	{		
		try (var scope = ResourceScope.newConfinedScope())
		{
			final var sockaddr = toSockaddr(local, scope);
			final var family = (short) Ws2_32.sockaddr.family.get(sockaddr);
			link = new Link(family, Ws2_32.SOCK_STREAM, 0);
			link.bind(sockaddr);
			key = group.register(link, this);
			return this;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <A> void connect (SocketAddress remote, A attachment, CompletionHandler<Void, ? super A> handler)
	{
		try (var scope = ResourceScope.newConfinedScope())
		{
			if (link == null) try 
			{
				var sockaddr = toWildSockaddr(remote, scope);
				final var family = (short) Ws2_32.sockaddr.family.get(sockaddr);
				link = new Link(family, Ws2_32.SOCK_STREAM, 0);
				link.bind(sockaddr);
				key = group.register(link, this);
			}
			catch (Throwable e)
			{
				group.submit(() -> handler.failed(e, attachment));
			}

			try
			{
				final var sockaddr = toSockaddr(remote, scope);
				final var operation = new Operation();				
				final var state = new ConnectState(operation, attachment, (CompletionHandler<Void, Object>) handler);
				pending.put(operation.handle().toRawLongValue(), state);				
				link.connect(operation, sockaddr);
			}
			catch (Throwable e)
			{
				group.submit(() -> handler.failed(e, attachment));
			}
		}
	}

	@Override
	public Future<Void> connect (SocketAddress remote)
	{
		final var future = new CompletableFuture<Void>();
		connect(remote, future, new CompletableFutureHandler<>());
		return future;
	}

	@Override
	public <A> void read (ByteBuffer dst, long timeout, TimeUnit unit, A attachment, CompletionHandler<Integer, ? super A> handler)
	{		
		ForkJoinPool.commonPool().submit(() -> handler.failed(new IOException("oops"), attachment));
	}

	@Override
	public Future<Integer> read (ByteBuffer dst)
	{		
		return CompletableFuture.failedFuture(new IOException("oops"));
	}

	@Override
	public <A> void read (ByteBuffer[] dsts, int offset, int length, long timeout, TimeUnit unit, A attachment, CompletionHandler<Long, ? super A> handler)
	{		
		ForkJoinPool.commonPool().submit(() -> handler.failed(new IOException("oops"), attachment));
	}

	@Override
	public <A> void write (ByteBuffer src, long timeout, TimeUnit unit, A attachment, CompletionHandler<Integer, ? super A> handler)
	{		
		ForkJoinPool.commonPool().submit(() -> handler.failed(new IOException("oops"), attachment));
	}

	@Override
	public Future<Integer> write (ByteBuffer src)
	{		
		return CompletableFuture.failedFuture(new IOException("oops"));
	}

	@Override
	public <A> void write (ByteBuffer[] srcs, int offset, int length, long timeout, TimeUnit unit, A attachment, CompletionHandler<Long, ? super A> handler)
	{		
		ForkJoinPool.commonPool().submit(() -> handler.failed(new IOException("oops"), attachment));
	}

	@Override
	public AsynchronousSocketChannel shutdownInput () throws IOException
	{		
		throw new IOException("oops");
	}

	@Override
	public AsynchronousSocketChannel shutdownOutput () throws IOException
	{		
		throw new IOException("oops");
	}
	
	public void complete (long key, boolean ignore0, int ignore1)
	{
		// do we know this operation?
		
		final var state = pending.remove(key);
		if (state == null) {
			// what!?
			return;
		}
		
		// get operation result

		final OperationState systemState;
		
		try 
		{ 
			systemState = link.query(state.operation());
		}
	    catch (Throwable cause) 
		{
	    	// #TODO: record this event
	    	state.failed(cause);
	    	state.operation().close();
	    	return;
	    }
	    
		if (! systemState.complete()) {
			// what!?
			return;
		}
		
		// evaluate operation result
		
		final var systemResult = systemState.result();

		try
		{
			if (systemResult == 0)
			{
				state.succeeded(systemState.data());
			}
			else
			{
				state.failed(new IOException("operation failed: code = " + Integer.toUnsignedString(systemResult, 10)));
			}
		}
		catch (Throwable cause)
		{
			// #TODO: record this event
		}

		state.operation().close();
	}
	
	@SuppressWarnings({ "preview", "exports" })
	public static MemorySegment toSockaddr (SocketAddress address, ResourceScope scope) throws IOException
	{
		if (address instanceof InetSocketAddress inetAddress)
		{
			final var length = inetAddress.getAddress().getAddress().length;
			
			if (length == 4) 
			{
				final var sockaddr = MemorySegment.allocateNative(Ws2_32.sockaddr_in.LAYOUT, scope);
				Ws2_32.sockaddr_in.family.set(sockaddr, (short) Ws2_32.AF_INET);
				Ws2_32.sockaddr_in.port.set(sockaddr, (short) inetAddress.getPort());
				final var addrOffset = Ws2_32.sockaddr_in.LAYOUT.byteOffset(PathElement.groupElement("addr"));
				final var addr = sockaddr.asSlice(addrOffset, Ws2_32.in_addr.LAYOUT.byteSize());
				addr.copyFrom(MemorySegment.ofArray(inetAddress.getAddress().getAddress()));
				return sockaddr;
			}
			else if (length == 16) 
			{
				final var sockaddr = MemorySegment.allocateNative(Ws2_32.sockaddr_in6.LAYOUT, scope);
				Ws2_32.sockaddr_in6.family.set(sockaddr, (short) Ws2_32.AF_INET6);
				Ws2_32.sockaddr_in6.port.set(sockaddr, (short) inetAddress.getPort());
				final var addrOffset = Ws2_32.sockaddr_in6.LAYOUT.byteOffset(PathElement.groupElement("addr"));
				final var addr = sockaddr.asSlice(addrOffset, Ws2_32.in6_addr.LAYOUT.byteSize());
				addr.copyFrom(MemorySegment.ofArray(inetAddress.getAddress().getAddress()));
				return sockaddr;
			}
			else
			{
				throw new IOException("unexpected IP address length: " + length);
			}
		}
		else
		{
			throw new IOException("unexpected address type: " + address.getClass());
		}
	}
	
	@SuppressWarnings({ "preview", "exports" })
	public static MemorySegment toWildSockaddr (SocketAddress address, ResourceScope scope) throws IOException
	{
		if (address instanceof InetSocketAddress inetAddress)
		{
			final var length = inetAddress.getAddress().getAddress().length;
			
			if (length == 4) 
			{
				final var sockaddr = MemorySegment.allocateNative(Ws2_32.sockaddr_in.LAYOUT, scope).fill((byte) 0);
				Ws2_32.sockaddr_in.family.set(sockaddr, (short) Ws2_32.AF_INET);
				return sockaddr;
			}
			else if (length == 16) 
			{
				final var sockaddr = MemorySegment.allocateNative(Ws2_32.sockaddr_in6.LAYOUT, scope).fill((byte) 0);
				Ws2_32.sockaddr_in6.family.set(sockaddr, (short) Ws2_32.AF_INET6);
				return sockaddr;
			}
			else
			{
				throw new IOException("unexpected IP address length: " + length);
			}
		}
		else
		{
			throw new IOException("unexpected address type: " + address.getClass());
		}
	}
}
