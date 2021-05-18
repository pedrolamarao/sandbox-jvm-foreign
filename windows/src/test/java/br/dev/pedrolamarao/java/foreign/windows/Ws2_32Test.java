package br.dev.pedrolamarao.java.foreign.windows;

import static jdk.incubator.foreign.CLinker.C_INT;
import static jdk.incubator.foreign.CLinker.C_POINTER;
import static jdk.incubator.foreign.MemoryAddress.NULL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.junit.jupiter.api.Test;

import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.ResourceScope;

public final class Ws2_32Test
{
	@Test
	public void bind () throws Throwable
	{
		try (var scope = ResourceScope.newConfinedScope())
		{
			final var address = MemorySegment.allocateNative(Ws2_32.sockaddr_in.LAYOUT, scope);
			address.fill((byte) 0);
			Ws2_32.sockaddr_in.family.set(address, (short) Ws2_32.AF_INET);
			
			final var handle = (MemoryAddress) Ws2_32.socket.invokeExact(Ws2_32.AF_INET, Ws2_32.SOCK_STREAM, Ws2_32.IPPROTO_TCP);
			assertNotEquals(-1, handle);
			
			assertEquals(
				0,
				(int) Ws2_32.bind.invokeExact(handle, address.address(), (int) address.byteSize())
			);
			
			assertEquals(
				0,
				(int) Ws2_32.closesocket.invokeExact(handle)
			);
		}
	}
	
	@Test
	public void getaddrinfo () throws Throwable
	{
		try (var scope = ResourceScope.newConfinedScope())
		{
			final var host = CLinker.toCString("localhost", scope);
			final var service = CLinker.toCString("http", scope);
			final var hint = MemorySegment.allocateNative(Ws2_32.addrinfo.LAYOUT, scope).fill((byte) 0);
			final var addressRef = MemorySegment.allocateNative(C_POINTER, scope);

			assertEquals(
				0, 
				(int) Ws2_32.getaddrinfo.invokeExact(host.address(), service.address(), hint.address(), addressRef.address())
			);
			
			final var address = MemoryAccess.getAddress(addressRef);
			
			Ws2_32.freeaddrinfo.invokeExact(address.address());
		}
	}
	
	@Test
	public void getnameinfo__sockaddr_in () throws Throwable
	{
		try (var scope = ResourceScope.newConfinedScope())
		{
			final var address = MemorySegment.allocateNative(Ws2_32.sockaddr_in.LAYOUT, scope);
			final var host = MemorySegment.allocateNative(1024, scope);
			final var service = MemorySegment.allocateNative(1024, scope);
			
			address.fill((byte) 0);
			host.fill((byte) 0);
			service.fill((byte) 0);
			
			Ws2_32.sockaddr_in.family.set(address, (short) Ws2_32.AF_INET);
			final var r0 = (int) Ws2_32.getnameinfo.invokeExact(address.address(), (int) address.byteSize(), host.address(), (int) host.byteSize(), service.address(), (int) service.byteSize(), (int) (Ws2_32.NI_NUMERICHOST | Ws2_32.NI_NUMERICSERV));

			assertEquals(0, r0);
			assertEquals("0.0.0.0", CLinker.toJavaString(host));
			assertEquals("0", CLinker.toJavaString(service));
			
			address.fill((byte) 0);
			host.fill((byte) 0);
			service.fill((byte) 0);
			
			Ws2_32.sockaddr_in.family.set(address, (short) Ws2_32.AF_INET);
			Ws2_32.sockaddr_in.port.set(address, (short) 80);
			final var r1 = (int) Ws2_32.getnameinfo.invokeExact(address.address(), (int) address.byteSize(), host.address(), (int) host.byteSize(), service.address(), (int) service.byteSize(), (int) (Ws2_32.NI_NUMERICHOST | Ws2_32.NI_NUMERICSERV));

			assertEquals(0, r1);
			assertEquals("0.0.0.0", CLinker.toJavaString(host));
			assertEquals("80", CLinker.toJavaString(service));
			
			address.fill((byte) 0);
			host.fill((byte) 0);
			service.fill((byte) 0);
			
			Ws2_32.sockaddr_in.family.set(address, (short) Ws2_32.AF_INET);
			Ws2_32.sockaddr_in.port.set(address, (short) 443);
			Ws2_32.sockaddr_in.addr.set(address, networkInt((1 << 24)));
			final var r2 = (int) Ws2_32.getnameinfo.invokeExact(address.address(), (int) address.byteSize(), host.address(), (int) host.byteSize(), service.address(), (int) service.byteSize(), (int) (Ws2_32.NI_NUMERICHOST | Ws2_32.NI_NUMERICSERV));

			assertEquals(0, r2);
			assertEquals("0.0.0.1", CLinker.toJavaString(host));
			assertEquals("443", CLinker.toJavaString(service));
			
			Ws2_32.sockaddr_in.family.set(address, (short) Ws2_32.AF_INET);
			Ws2_32.sockaddr_in.port.set(address, (short) 12345);
			Ws2_32.sockaddr_in.addr.set(address, networkInt((1 << 24) | 0x7F));
			final var r3 = (int) Ws2_32.getnameinfo.invokeExact(address.address(), (int) address.byteSize(), host.address(), (int) host.byteSize(), service.address(), (int) service.byteSize(), (int) (Ws2_32.NI_NUMERICHOST | Ws2_32.NI_NUMERICSERV));

			assertEquals(0, r3);
			assertEquals("127.0.0.1", CLinker.toJavaString(host));
			assertEquals("12345", CLinker.toJavaString(service));
		}
	}
	
	@Test
	public void listen () throws Throwable
	{
		try (var scope = ResourceScope.newConfinedScope())
		{
			final var address = MemorySegment.allocateNative(Ws2_32.sockaddr_in.LAYOUT, scope);
			address.fill((byte) 0);
			Ws2_32.sockaddr_in.family.set(address, (short) Ws2_32.AF_INET);
			
			final var handle = (MemoryAddress) Ws2_32.socket.invokeExact(Ws2_32.AF_INET, Ws2_32.SOCK_STREAM, Ws2_32.IPPROTO_TCP);
			assertNotEquals(-1, handle);
			
			assertEquals(
				0,
				(int) Ws2_32.bind.invokeExact(handle, address.address(), (int) address.byteSize())
			);
			
			assertEquals(
				0,
				(int) Ws2_32.listen.invokeExact(handle, 0)
			);
			
			assertEquals(
				0,
				(int) Ws2_32.closesocket.invokeExact(handle)
			);
		}
	}
	
	@Test
	public void socket () throws Throwable
	{
		final var handle = (MemoryAddress) Ws2_32.socket.invokeExact(Ws2_32.AF_INET, Ws2_32.SOCK_STREAM, Ws2_32.IPPROTO_TCP);
		assertNotEquals(-1, handle);
		final var r0 = (int) Ws2_32.closesocket.invokeExact(handle);
		assertNotEquals(-1, r0);
	}
	
	@Test
	public void setsockopt () throws Throwable
	{
		final var handle = (MemoryAddress) Ws2_32.socket.invokeExact(Ws2_32.AF_INET, Ws2_32.SOCK_STREAM, Ws2_32.IPPROTO_TCP);
		assertNotEquals(-1, handle);
		
		try (var scope = ResourceScope.newConfinedScope())
		{
			final var value = MemorySegment.allocateNative(C_INT, scope);
			MemoryAccess.setInt(value, 1);
			assertNotEquals(
				-1,
				(int) Ws2_32.setsockopt.invokeExact(handle, Ws2_32.SOL_SOCKET, Ws2_32.SO_DEBUG, value.address(), (int) value.byteSize())
			);
		}
		
		assertNotEquals(
			-1,
			(int) Ws2_32.closesocket.invokeExact(handle)
		);
	}
	
	@Test
	public void WSAIoctl () throws Throwable
	{
		final var handle = (MemoryAddress) Ws2_32.socket.invokeExact(Ws2_32.AF_INET, Ws2_32.SOCK_STREAM, Ws2_32.IPPROTO_TCP);
		assertEquals(0, (int) Ws2_32.WSAGetLastError.invokeExact());
		assertNotEquals(-1, handle.toRawLongValue());
		
		try (var scope = ResourceScope.newConfinedScope())
		{
			final var address = MemorySegment.allocateNative(Ws2_32.sockaddr_in.LAYOUT, scope);
			address.fill((byte) 0);
			Ws2_32.sockaddr_in.family.set(address, (short) Ws2_32.AF_INET);
			
			final var r10 = (int) Ws2_32.bind.invokeExact(handle, address.address(), (int) address.byteSize());
			assertEquals(0, (int) Ws2_32.WSAGetLastError.invokeExact());
			assertEquals(0, r10);
			
			final var in = MemorySegment.allocateNative(Kernel32.GUID.LAYOUT, scope);
			Mswsock.WSAID_CONNECTEX.set(in);			
			final var out = MemorySegment.allocateNative(C_POINTER, scope);
			final var length = MemorySegment.allocateNative(C_INT, scope);
			
			final var r20 = (int) Ws2_32.WSAIoctl.invokeExact(handle, Ws2_32.SIO_GET_EXTENSION_FUNCTION_POINTER, in.address(), (int) in.byteSize(), out.address(), (int) out.byteSize(), length.address(), NULL, NULL);
			assertEquals(0, (int) Ws2_32.WSAGetLastError.invokeExact());
			assertEquals(0, r20);
		}
		
		final var r30 = (int) Ws2_32.closesocket.invokeExact(handle);
		assertEquals(0, (int) Ws2_32.WSAGetLastError.invokeExact());
		assertEquals(0, r30);
	}
	
	public static int networkInt (int value)
	{
		return ByteBuffer.allocate(Integer.BYTES).order(ByteOrder.LITTLE_ENDIAN).putInt(0, value).order(ByteOrder.BIG_ENDIAN).getInt(0);
	}
}
