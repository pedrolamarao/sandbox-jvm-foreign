package br.dev.pedrolamarao.java.foreign.windows;

import static jdk.incubator.foreign.CLinker.C_INT;
import static jdk.incubator.foreign.CLinker.C_POINTER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.ResourceScope;

public final class MswsockTest
{
	@Test
	public void AcceptEx () throws Throwable
	{
		try (var scope = ResourceScope.newConfinedScope())
		{
			final var address = MemorySegment.allocateNative(Ws2_32.sockaddr_in.LAYOUT, scope);
			address.fill((byte) 0);
			Ws2_32.sockaddr_in.family.set(address, (short) Ws2_32.AF_INET);
			
			final var port = (MemoryAddress) Ws2_32.socket.invokeExact(Ws2_32.AF_INET, Ws2_32.SOCK_STREAM, Ws2_32.IPPROTO_TCP);
			assertNotEquals(-1, port);
			
			assertEquals(
				0,
				(int) Ws2_32.bind.invokeExact(port, address.address(), (int) address.byteSize())
			);
			
			assertEquals(
				0,
				(int) Ws2_32.listen.invokeExact(port, 0)
			);
			
			final var link = (MemoryAddress) Ws2_32.socket.invokeExact(Ws2_32.AF_INET, Ws2_32.SOCK_STREAM, Ws2_32.IPPROTO_TCP);
			assertNotEquals(-1, link);
			
			final var operation = MemorySegment.allocateNative(Kernel32.OVERLAPPED.LAYOUT, scope).fill((byte) 0);
			final var buffer = MemorySegment.allocateNative(2048, scope);
			final var read = MemorySegment.allocateNative(CLinker.C_INT, scope);
			
			assertEquals(
				0,
				(int) Mswsock.AcceptEx.invokeExact(port, link, buffer.address(), 0, 1024, 1024, read.address(), operation.address())
			);
				
			assertEquals(
				Ws2_32.WSA_IO_PENDING,
				(int) Ws2_32.WSAGetLastError.invokeExact()
			);
			
			assertEquals(
				0,
				(int) Ws2_32.closesocket.invokeExact(link)
			);

			
			assertEquals(
				0,
				(int) Ws2_32.closesocket.invokeExact(port)
			);
		}
	}
		
	@Test
	@Timeout(value=1000, unit=TimeUnit.MILLISECONDS)
	public void ConnectEx () throws Throwable
	{
		try (var scope = ResourceScope.newConfinedScope())
		{
			final var socket = (MemoryAddress) Ws2_32.socket.invokeExact(Ws2_32.AF_INET, Ws2_32.SOCK_STREAM, Ws2_32.IPPROTO_TCP);
			assertEquals(0, (int) Ws2_32.WSAGetLastError.invokeExact());
			assertNotEquals(Ws2_32.INVALID_SOCKET, socket);

			final var address = MemorySegment.allocateNative(Ws2_32.sockaddr_in.LAYOUT, scope).fill((byte) 0);
			Ws2_32.sockaddr_in.family.set(address, (short) Ws2_32.AF_INET);

			final var r10 = (int) Ws2_32.bind.invokeExact(socket, address.address(), (int) address.byteSize());
			assertEquals(0, (int) Ws2_32.WSAGetLastError.invokeExact());
			assertEquals(0, r10);

			final var extensionId = MemorySegment.allocateNative(Kernel32.GUID.LAYOUT, scope);
			Mswsock.WSAID_CONNECTEX.set(extensionId);
			final var extensionValue = MemorySegment.allocateNative(C_POINTER, scope);
			final var length = MemorySegment.allocateNative(C_INT, scope);
			
			final var r20 = 
				(int) Ws2_32.WSAIoctl.invokeExact(
					socket, Ws2_32.SIO_GET_EXTENSION_FUNCTION_POINTER, 
					extensionId.address(), (int) extensionId.byteSize(), 
					extensionValue.address(), (int) extensionValue.byteSize(), 
					length.address(), MemoryAddress.NULL, MemoryAddress.NULL
				);
			assertEquals(0, (int) Ws2_32.WSAGetLastError.invokeExact());
			assertEquals(0, r20);
			
			final var ConnectEx = CLinker.getInstance().downcallHandle(MemoryAccess.getAddress(extensionValue), Mswsock.ConnectExType, Mswsock.ConnectExDescriptor);
			
			Ws2_32.sockaddr_in.port.set(address, (short) 12345);
			Ws2_32.sockaddr_in.addr.set(address, (int) ((1 << 24) | 0x7F));
			
			final var operation = MemorySegment.allocateNative(Kernel32.OVERLAPPED.LAYOUT, scope).fill((byte) 0);
			
			final var r30 = (int) ConnectEx.invokeExact(socket, address.address(), (int) address.byteSize(), MemoryAddress.NULL, 0, MemoryAddress.NULL, operation.address());
			assertEquals(Ws2_32.WSA_IO_PENDING, (int) Ws2_32.WSAGetLastError.invokeExact());
			assertEquals(Kernel32.FALSE, r30);
			
			final var r99 = (int) Ws2_32.closesocket.invokeExact(socket);
			assertEquals(0, (int) Ws2_32.WSAGetLastError.invokeExact());
			assertEquals(0, r99);
		}
	}
}
