package br.dev.pedrolamarao.java.nio.windows;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import br.dev.pedrolamarao.java.foreign.windows.Ws2_32;
import br.dev.pedrolamarao.java.nio.windows.internal.Link;
import br.dev.pedrolamarao.java.nio.windows.internal.Operation;
import br.dev.pedrolamarao.java.nio.windows.internal.OperationState;
import br.dev.pedrolamarao.java.nio.windows.internal.Port;
import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.MemoryLayout.PathElement;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.ResourceScope;
/**
 * AsynchronousServerSocketChannel with a Panama based implementation.
 */

public final class WindowsAsynchronousServerSocketChannel extends AsynchronousServerSocketChannel implements WindowsChannel
{
	@SuppressWarnings({ "preview", "exports" })
	public final record AcceptState (Operation operation, MemorySegment buffer, Link link, Object context, CompletionHandler<AsynchronousSocketChannel, Object> handler)
	{
		public void complete (AsynchronousSocketChannel channel)
		{
			handler().completed(channel, context());			
		}
		
		public void fail (Throwable cause)
		{
			handler().failed(cause, context());
		}
	}

	private int family = Ws2_32.AF_UNSPEC;
	
	private final WindowsAsynchronousChannelGroup group;
	
	private long key = 0;
	
	private final HashMap<Long, AcceptState> operations = new HashMap<>();
	
	private Port port = null;
	
	// life-cycle
	
	public WindowsAsynchronousServerSocketChannel (WindowsAsynchronousChannelProvider provider, WindowsAsynchronousChannelGroup group)
	{
		super(provider);
		this.group = group;
	}

	@Override
	public void close () throws IOException
	{
		if (port != null) {
			group.unregister(key);
			port.close();
		}
		
		operations.forEach((key, state) -> state.operation().close());
	}
	
	// properties

	@Override
	public boolean isOpen ()
	{
		return (port != null);
	}
	
	// methods

	@Override
	public <T> T getOption (SocketOption<T> name) throws IOException
	{		
		throw new IOException("oops");
	}

	@Override
	public Set<SocketOption<?>> supportedOptions ()
	{
		throw new RuntimeException("oops");
	}
	
	@Override
	public AsynchronousServerSocketChannel bind (SocketAddress address, int backlog) throws IOException
	{
		var sockaddr = toSockaddr(address);
		family = (short) Ws2_32.sockaddr.family.get(sockaddr);
		port = new Port(family, Ws2_32.SOCK_STREAM, 0);
		port.bind(sockaddr);
		key = group.register(port, this::complete);
		port.listen(Ws2_32.SOMAXCONN);
		return this;
	}

	@Override
	public <T> AsynchronousServerSocketChannel setOption (SocketOption<T> name, T value) throws IOException
	{		
		throw new IOException("oops");
	}

	@SuppressWarnings("unchecked")
	@Override
	public <A> void accept (A attachment, CompletionHandler<AsynchronousSocketChannel, ? super A> handler)
	{		
		try
		{
			final var operation = new Operation();
			final var buffer = MemorySegment.allocateNative(2048, ResourceScope.globalScope());
			final var link = new Link(family, Ws2_32.SOCK_STREAM, 0);
			final var state = new AcceptState(operation, buffer, link, attachment, (CompletionHandler<AsynchronousSocketChannel, Object>) handler);
			operations.put(operation.handle().toRawLongValue(), state);
			port.accept(operation, buffer, link);			
		} 
		catch (IOException e)
		{
			group.submit(() -> handler.failed(e, attachment));
		}
	}

	@Override
	public Future<AsynchronousSocketChannel> accept ()
	{
		final var future = new CompletableFuture<AsynchronousSocketChannel>();
		accept(future, new CompletableFutureHandler<AsynchronousSocketChannel>());
		return future;
	}

	@Override
	public SocketAddress getLocalAddress () throws IOException
	{		
		throw new IOException("oops");
	}
	
	public void complete (long operation, boolean status, int ignore)
	{
		final var state = operations.remove(operation);
		if (state == null) {
			// what!?
			return;
		}

		final OperationState systemState;
		
		try 
		{ 
			systemState = port.query(state.operation());
		}
	    catch (Throwable cause) 
		{
	    	// #TODO: record this event
	    	state.handler().failed(cause, state.context());
	    	state.operation().close();
	    	return;
	    }
	    
		if (! systemState.complete()) {
			// what!?
			return;
		}
		
		final var systemResult = systemState.result();

		handler:
		if (systemResult == 0)
		{
			final var link = state.link();
			
			try (var scope = ResourceScope.newConfinedScope()) 
			{
				final var value = MemorySegment.allocateNative(CLinker.C_POINTER, scope);
				MemoryAccess.setAddress(value, port.handle());
				link.setsockopt(Ws2_32.SOL_SOCKET, Ws2_32.SO_UPDATE_ACCEPT_CONTEXT, value);
			}
			catch (IOException e) 
			{
				state.fail(e);
				break handler;
			}

			final var channel = new WindowsAsynchronousSocketChannel((WindowsAsynchronousChannelProvider) this.provider(), group, link);
			
			try {
				state.complete(channel);
			}
			catch (Throwable cause) {
				// #TODO: callback failed				
			}
		}
		else
		{
			try {
				state.fail(new IOException("operation failed with status code " + Integer.toUnsignedString(systemResult, 10)));
			}
			catch (Throwable cause) {
				// #TODO: callback failed
			}
		}

		state.operation().close();
	}
	
	@SuppressWarnings({ "preview", "exports" })
	public static MemorySegment toSockaddr (SocketAddress address) throws IOException
	{
		if (address instanceof InetSocketAddress inetAddress)
		{
			final var length = inetAddress.getAddress().getAddress().length;
			
			if (length == 4) 
			{
				final var sockaddr = MemorySegment.allocateNative(Ws2_32.sockaddr_in.LAYOUT, ResourceScope.globalScope());
				Ws2_32.sockaddr_in.family.set(sockaddr, (short) Ws2_32.AF_INET);
				Ws2_32.sockaddr_in.port.set(sockaddr, (short) inetAddress.getPort());
				final var addrOffset = Ws2_32.sockaddr_in.LAYOUT.byteOffset(PathElement.groupElement("addr"));
				final var addr = sockaddr.asSlice(addrOffset, Ws2_32.in_addr.LAYOUT.byteSize());
				addr.copyFrom(MemorySegment.ofArray(inetAddress.getAddress().getAddress()));
				return sockaddr;
			}
			else if (length == 16) 
			{
				final var sockaddr = MemorySegment.allocateNative(Ws2_32.sockaddr_in6.LAYOUT, ResourceScope.globalScope());
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
}
