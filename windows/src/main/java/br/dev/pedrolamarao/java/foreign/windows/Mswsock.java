package br.dev.pedrolamarao.java.foreign.windows;

import static java.lang.invoke.MethodType.methodType;
import static jdk.incubator.foreign.CLinker.C_INT;
import static jdk.incubator.foreign.CLinker.C_POINTER;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import br.dev.pedrolamarao.java.foreign.windows.Kernel32.GUID;
import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.SymbolLookup;

public final class Mswsock
{
	// constants
    
    public static final int TF_DISCONNECT = 1;
	
	public static final class WSAID_CONNECTEX
	{
		public static final int data1 = 0x25a207b9;
		
		public static final short data2 = (short) 0xddf3;
		
		public static final short data3 = (short) 0x4660;
				
		public static final byte[] data4 = { (byte) 0x8e, (byte) 0xe9, (byte) 0x76, (byte) 0xe5, (byte) 0x8c, (byte) 0x74, (byte) 0x06, (byte) 0x3e };
		
		public static void set (MemorySegment guid)
		{
			GUID.data1.set(guid, data1);
			GUID.data2.set(guid, data2);
			GUID.data3.set(guid, data3);
			GUID.data4.set(guid, 0, data4[0]);
			GUID.data4.set(guid, 1, data4[1]);
			GUID.data4.set(guid, 2, data4[2]);
			GUID.data4.set(guid, 3, data4[3]);
			GUID.data4.set(guid, 4, data4[4]);
			GUID.data4.set(guid, 5, data4[5]);
			GUID.data4.set(guid, 6, data4[6]);
			GUID.data4.set(guid, 7, data4[7]);
		}
	}
	
	// types
	
	public static final MethodType ConnectExType = MethodType.methodType(int.class, MemoryAddress.class, MemoryAddress.class, int.class, MemoryAddress.class, int.class, MemoryAddress.class, MemoryAddress.class);
	
	public static final FunctionDescriptor ConnectExDescriptor = FunctionDescriptor.of(C_INT,  C_POINTER, C_POINTER, C_INT, C_POINTER, C_INT, C_POINTER, C_POINTER);
	
	// methods

	public static final MethodHandle AcceptEx;
	
	public static final MethodHandle TransmitFile;
	
	static
	{
		System.loadLibrary("mswsock");
		
		final var loader = SymbolLookup.loaderLookup();
    	
    	final var linker = CLinker.getInstance();

		AcceptEx = linker.downcallHandle(
			loader.lookup("AcceptEx").get(),
			methodType(int.class, MemoryAddress.class, MemoryAddress.class, MemoryAddress.class, int.class, int.class, int.class, MemoryAddress.class, MemoryAddress.class),
			FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER, C_POINTER, C_INT, C_INT, C_INT, C_POINTER, C_POINTER)
		);

    	TransmitFile = linker.downcallHandle(
			loader.lookup("TransmitFile").get(),
			MethodType.methodType(int.class, MemoryAddress.class, MemoryAddress.class, int.class, int.class, MemoryAddress.class, MemoryAddress.class, int.class),
			FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER, C_INT, C_INT, C_POINTER, C_POINTER, C_INT)
		);
	}
}
