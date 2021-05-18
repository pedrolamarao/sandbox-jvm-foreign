package br.dev.pedrolamarao.java.foreign.windows;

import static jdk.incubator.foreign.CLinker.C_INT;
import static jdk.incubator.foreign.CLinker.C_LONG;
import static jdk.incubator.foreign.CLinker.C_LONG_LONG;
import static jdk.incubator.foreign.CLinker.C_POINTER;
import static jdk.incubator.foreign.CLinker.C_SHORT;
import static jdk.incubator.foreign.MemoryLayout.paddingLayout;
import static jdk.incubator.foreign.MemoryLayout.structLayout;
import static jdk.incubator.foreign.MemoryLayout.valueLayout;
import static jdk.incubator.foreign.MemoryLayout.PathElement.groupElement;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;

import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.GroupLayout;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryHandles;
import jdk.incubator.foreign.SymbolLookup;
import jdk.incubator.foreign.ValueLayout;

public final class Ws2_32
{
	// constants
	
	public static final int AF_UNSPEC = 0;

	public static final int AF_INET = 2;

	public static final int AF_INET6 = 23;
	
	public static final int AI_PASSIVE = 0x01;
	
	public static final MemoryAddress INVALID_SOCKET = MemoryAddress.ofLong(0xFFFFFFFF);

	public static final int IPPROTO_ICMP = 1;

	public static final int IPPROTO_TCP = 6;

	public static final int IPPROTO_UDP = 17;
	
	public static final int NI_NUMERICHOST = 0x02;
	
	public static final int NI_NUMERICSERV = 0x08;
	
	public static final int SIO_GET_EXTENSION_FUNCTION_POINTER = (0x80000000 | 0x40000000 | 0x08000000 | 6);

	public static final int SO_DEBUG = 0x0001;

	public static final int SO_UPDATE_ACCEPT_CONTEXT = 0x700B;
	
	public static final int SOCK_STREAM = 1;

	public static final int SOCK_DGRAM = 2;
	
	public static final int SOL_SOCKET = 0xFFFF;
	
	public static final int SOMAXCONN = 0x7fffffff;
	
	public static final int WSA_IO_PENDING = 997;
	
	// types
	
	public static final class addrinfo
	{
		public static final GroupLayout LAYOUT = structLayout(
			C_INT.withName("flags"),
			C_INT.withName("family"),
			C_INT.withName("socktype"),
			C_INT.withName("protocol"),
			C_LONG_LONG.withName("addrlen"),
			C_POINTER.withName("canonname"),
			C_POINTER.withName("addr"),
			C_POINTER.withName("next")
		);
		
		public static final VarHandle flags = LAYOUT.varHandle(int.class, groupElement("flags"));
		
		public static final VarHandle family = LAYOUT.varHandle(int.class, groupElement("family"));
		
		public static final VarHandle socktype = LAYOUT.varHandle(int.class, groupElement("socktype"));
		
		public static final VarHandle protocol = LAYOUT.varHandle(int.class, groupElement("protocol"));
		
		public static final VarHandle addrlen = LAYOUT.varHandle(long.class, groupElement("addrlen"));
		
		public static final VarHandle addr = LAYOUT.varHandle(long.class, groupElement("addr"));
	}
	
	public static final class in_addr
	{
		public static final ValueLayout LAYOUT = valueLayout(32, ByteOrder.BIG_ENDIAN);
	}
	
	public static final class in6_addr
	{
		public static final ValueLayout LAYOUT = valueLayout(128, ByteOrder.BIG_ENDIAN);
	}
	
	public static final class sockaddr
	{
		public static final GroupLayout LAYOUT = structLayout(
			C_SHORT.withName("family"),
			paddingLayout(112)
		);

		public static final VarHandle family = LAYOUT.varHandle(short.class, groupElement("family"));
	}
	
	public static final class sockaddr_in
	{
		public static final GroupLayout LAYOUT = structLayout(
			C_SHORT.withName("family"),
			C_SHORT.withName("port"),
			in_addr.LAYOUT.withName("addr"),
			paddingLayout(64)
		);

		public static final VarHandle family = LAYOUT.varHandle(short.class, groupElement("family"));

		public static final VarHandle port;
		
		static {
			final var handle = MemoryHandles.varHandle(short.class, ByteOrder.BIG_ENDIAN);
			final var offset = LAYOUT.byteOffset(groupElement("port"));
			port = MemoryHandles.insertCoordinates(handle, 1, offset);			
		}
		
		public static final VarHandle addr = LAYOUT.varHandle(int.class, groupElement("addr"));
	}
	
	public static final class sockaddr_in6
	{
		public static final GroupLayout LAYOUT = structLayout(
			C_SHORT.withName("family"),
			C_SHORT.withName("port"),
			C_LONG.withName("flowinfo"),
			in6_addr.LAYOUT.withName("addr"),
			C_LONG.withName("scope_id")
		);

		public static final VarHandle family = LAYOUT.varHandle(short.class, groupElement("family"));

		public static final VarHandle port;
		
		static {
			final var handle = MemoryHandles.varHandle(short.class, ByteOrder.BIG_ENDIAN);
			final var offset = LAYOUT.byteOffset(groupElement("port"));
			port = MemoryHandles.insertCoordinates(handle, 1, offset);			
		}

		public static final VarHandle flowInfo = LAYOUT.varHandle(int.class, groupElement("flowinfo"));

		public static final VarHandle scopeId = LAYOUT.varHandle(int.class, groupElement("scope_id"));
	}
	
	public static final class sockaddr_storage
	{
		public static final GroupLayout LAYOUT = structLayout(
			C_SHORT.withName("family"),
			paddingLayout(1008)
		);

		public static final VarHandle family = LAYOUT.varHandle(short.class, groupElement("family"));
	}
	
	public static final class SOCKET
	{
		public static final ValueLayout LAYOUT = CLinker.C_LONG_LONG;
	}
	
	// methods
	
	public static final MethodHandle bind;
	
	public static final MethodHandle closesocket;
	
	public static final MethodHandle connect;
	
	public static final MethodHandle freeaddrinfo;
	
	public static final MethodHandle getaddrinfo;
	
	public static final MethodHandle getnameinfo;
	
	public static final MethodHandle getsockname;
	
	public static final MethodHandle getsockopt;
	
	public static final MethodHandle listen;
	
	public static final MethodHandle socket;

	public static final MethodHandle setsockopt;
		
	public static final MethodHandle WSAGetLastError;
	
	public static final MethodHandle WSAGetOverlappedResult;
	
	public static final MethodHandle WSAIoctl;
	
	static
	{
		System.loadLibrary("ws2_32");
		
		final var loader = SymbolLookup.loaderLookup();
    	
    	final var linker = CLinker.getInstance();

		bind = linker.downcallHandle(
			loader.lookup("bind").get(),
			MethodType.methodType(int.class, MemoryAddress.class, MemoryAddress.class, int.class),
			FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER, C_INT)
		);

		connect = linker.downcallHandle(
			loader.lookup("connect").get(),
			MethodType.methodType(int.class, MemoryAddress.class, MemoryAddress.class, int.class),
			FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER, C_INT)
		);

		closesocket = linker.downcallHandle(
			loader.lookup("closesocket").get(),
			MethodType.methodType(int.class, MemoryAddress.class),
			FunctionDescriptor.of(C_INT, C_POINTER)
		);

    	freeaddrinfo = linker.downcallHandle(
    		loader.lookup("freeaddrinfo").get(),
			MethodType.methodType(void.class, MemoryAddress.class),
			FunctionDescriptor.ofVoid(C_POINTER)
		);
		
    	getaddrinfo = linker.downcallHandle(
    		loader.lookup("getaddrinfo").get(),
			MethodType.methodType(int.class, MemoryAddress.class, MemoryAddress.class, MemoryAddress.class, MemoryAddress.class),
			FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER, C_POINTER, C_POINTER)
		);
		
    	getnameinfo = linker.downcallHandle(
    		loader.lookup("getnameinfo").get(),
			MethodType.methodType(int.class, MemoryAddress.class, int.class, MemoryAddress.class, int.class, MemoryAddress.class, int.class, int.class),
			FunctionDescriptor.of(C_INT, C_POINTER, C_INT, C_POINTER, C_INT, C_POINTER, C_INT, C_INT)
		);
		
    	getsockname = linker.downcallHandle(
    		loader.lookup("getnameinfo").get(),
			MethodType.methodType(int.class, MemoryAddress.class, int.class),
			FunctionDescriptor.of(C_INT, C_POINTER, C_INT)
		);

		getsockopt = linker.downcallHandle(
			loader.lookup("getsockopt").get(),
			MethodType.methodType(int.class, MemoryAddress.class, int.class, int.class, MemoryAddress.class, MemoryAddress.class),
			FunctionDescriptor.of(C_INT, C_POINTER, C_INT, C_INT, C_POINTER, C_POINTER)
		);

		listen = linker.downcallHandle(
			loader.lookup("listen").get(),
			MethodType.methodType(int.class, MemoryAddress.class, int.class),
			FunctionDescriptor.of(C_INT, C_POINTER, C_INT)
		);

		socket = linker.downcallHandle(
			loader.lookup("socket").get(),
			MethodType.methodType(MemoryAddress.class, int.class, int.class, int.class),
			FunctionDescriptor.of(C_POINTER, C_INT, C_INT, C_INT)
		);

		setsockopt = linker.downcallHandle(
			loader.lookup("setsockopt").get(),
			MethodType.methodType(int.class, MemoryAddress.class, int.class, int.class, MemoryAddress.class, int.class),
			FunctionDescriptor.of(C_INT, C_POINTER, C_INT, C_INT, C_POINTER, C_INT)
		);

		WSAGetLastError = linker.downcallHandle(
			loader.lookup("WSAGetLastError").get(),
			MethodType.methodType(int.class),
			FunctionDescriptor.of(C_INT)
		);

		WSAGetOverlappedResult = linker.downcallHandle(
			loader.lookup("WSAGetOverlappedResult").get(),
			MethodType.methodType(int.class, int.class, MemoryAddress.class, MemoryAddress.class, int.class, MemoryAddress.class),
			FunctionDescriptor.of(C_INT, C_INT, C_POINTER, C_POINTER, C_INT, C_POINTER)
		);

		WSAIoctl = linker.downcallHandle(
			loader.lookup("WSAIoctl").get(),
			MethodType.methodType(int.class, MemoryAddress.class, int.class, MemoryAddress.class, int.class, MemoryAddress.class, int.class, MemoryAddress.class, MemoryAddress.class, MemoryAddress.class),
			FunctionDescriptor.of(C_INT, C_POINTER, C_INT, C_POINTER, C_INT, C_POINTER, C_INT, C_POINTER, C_POINTER, C_POINTER)
		);
	}
}
