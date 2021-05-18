package br.dev.pedrolamarao.java.nio.windows.internal;

import java.io.IOException;

import br.dev.pedrolamarao.java.foreign.windows.Mswsock;
import br.dev.pedrolamarao.java.foreign.windows.Ws2_32;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;

public final class Port implements IoDevice
{
	private final MemoryAddress handle;
	
	// life-cycle
	
	public Port (int family, int style, int protocol) throws IOException
	{
		handle = downcall("<init>", () -> (MemoryAddress) Ws2_32.socket.invokeExact(family, style, protocol));
		if (Ws2_32.INVALID_SOCKET.equals(handle)) {
			final var error = downcall("<init>", () -> (int) Ws2_32.WSAGetLastError.invokeExact());
			throw new IOException("<init>: system error: " + error);
		}
	}
	
	public void close () throws IOException
	{
		downcall("close", () -> (int) Ws2_32.closesocket.invokeExact(handle));
	}
	
	// properties

	@Override
	public MemoryAddress handle ()
	{
		return handle;
	}
	
	// methods
	
	public void bind (MemorySegment address) throws IOException
	{
		final int result = downcall("bind", () -> (int) Ws2_32.bind.invokeExact(handle, address.address(), (int) address.byteSize()));
		if (result == -1) {
			final var error = downcall("bind", () -> (int) Ws2_32.WSAGetLastError.invokeExact());
			throw new IOException("bind: system error: " + error);
		}
	}
	
	public void listen (int backlog) throws IOException
	{
		final int result = downcall("listen", () -> (int) Ws2_32.listen.invokeExact(handle, backlog));
		if (result == -1) {
			final var error = downcall("listen", () -> (int) Ws2_32.WSAGetLastError.invokeExact());
			throw new IOException("listen: system error: " + error);
		}
	}
	
	public boolean accept (Operation operation, MemorySegment buffer, Link link) throws IOException
	{
		final int result = 
			downcall("accept", () -> (int) Mswsock.AcceptEx.invokeExact(handle, link.handle(), buffer.address(), 0, (int) (buffer.byteSize() / 2), (int) (buffer.byteSize() / 2), MemoryAddress.NULL, operation.handle()));
		
		if (result != 0) {
			return true;
		}
		
		final var error = 
			downcall("accept", () -> (int) Ws2_32.WSAGetLastError.invokeExact());

		switch (error) {
		case Ws2_32.WSA_IO_PENDING:
			return false;
		default:
			throw new IOException("accept: system error: " + Integer.toUnsignedString(error, 10));
		}
	}
	
	public void setsockopt (int level, int option, MemorySegment value) throws IOException
	{
		final int result = downcall("setsockopt", () -> (int) Ws2_32.setsockopt.invokeExact(handle, Ws2_32.SOL_SOCKET, Ws2_32.SO_UPDATE_ACCEPT_CONTEXT, value.address(), (int) value.byteSize()));
		if (result == -1) {
			final var error = downcall("setsockopt", () -> (int) Ws2_32.WSAGetLastError.invokeExact());
			throw new IOException("setsockopt: system error: " + error);
		}
	}
	
	// utility
	
	@FunctionalInterface
	private interface Downcallable <T>
	{
		T call () throws Throwable;
	}
	
	private static <T> T downcall (String caller, Downcallable<T> callable) throws IOException
	{
		try
		{
			return callable.call();
		} 
		catch (IOException e)
		{
			throw e;
		}
		catch (Throwable e)
		{
			throw new IOException(caller + ": downcall failed", e);
		}
	}
}
