package br.dev.pedrolamarao.java.foreign.windows;

import static java.lang.invoke.MethodType.methodType;
import static jdk.incubator.foreign.CLinker.C_CHAR;
import static jdk.incubator.foreign.CLinker.C_INT;
import static jdk.incubator.foreign.CLinker.C_LONG;
import static jdk.incubator.foreign.CLinker.C_LONG_LONG;
import static jdk.incubator.foreign.CLinker.C_POINTER;
import static jdk.incubator.foreign.CLinker.C_SHORT;
import static jdk.incubator.foreign.MemoryLayout.sequenceLayout;
import static jdk.incubator.foreign.MemoryLayout.structLayout;
import static jdk.incubator.foreign.MemoryLayout.unionLayout;
import static jdk.incubator.foreign.MemoryLayout.PathElement.groupElement;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;

import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.GroupLayout;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemoryLayout.PathElement;
import jdk.incubator.foreign.SymbolLookup;

public final class Kernel32
{
	// constants 
	
	public static final int ERROR_IO_INCOMPLETE = 996;
	
	public static final int ERROR_IO_PENDING = 997;
	
	public static final int FALSE = 0;
	
	public static final int FILE_FLAG_BACKUP_SEMANTICS = 0x02000000;
	
	public static final int FILE_FLAG_OVERLAPPED = 0x40000000;
	
	public static final int FILE_NOTIFY_CHANGE_FILE_NAME = 0x00000001;
    
    public static final int FILE_SHARE_DELETE = 0x00000004;
    
    public static final int FILE_SHARE_READ = 0x00000001;

    public static final int FILE_SHARE_WRITE = 0x00000002;

    public static final int GENERIC_READ = 0x80000000;

    public static final MemoryAddress INVALID_HANDLE_VALUE = MemoryAddress.ofLong(-1);
	
	public static final int OPEN_EXISTING = 3;
	
	public static final int TRUE = 1;
	
    public static final int WAIT_TIMEOUT = 258;
    
    // types
    
    public static final class FILE_NOTIFY_INFORMATION 
    {
    	public static final GroupLayout LAYOUT = structLayout(
			C_INT.withName("next"),
			C_INT.withName("action"),
			C_INT.withName("length"),
			C_CHAR.withName("name")
		)
		.withBitAlignment(Integer.SIZE);
    	
    	public static final VarHandle next = LAYOUT.varHandle(int.class, groupElement("next"));
    	
    	public static final VarHandle action = LAYOUT.varHandle(int.class, groupElement("action"));
    	
    	public static final VarHandle length = LAYOUT.varHandle(int.class, groupElement("length"));
    }
    
    public static final class GUID
    {
    	public static final MemoryLayout LAYOUT = structLayout(
			C_LONG.withName("Data1"),
			C_SHORT.withName("Data2"),
			C_SHORT.withName("Data3"),
			sequenceLayout(8, C_CHAR).withName("Data4")
		);
    	
    	public static final VarHandle data1 = LAYOUT.varHandle(int.class, groupElement("Data1"));
    	
    	public static final VarHandle data2 = LAYOUT.varHandle(short.class, groupElement("Data2"));
    	
    	public static final VarHandle data3 = LAYOUT.varHandle(short.class, groupElement("Data3"));
    	
    	public static final VarHandle data4 = LAYOUT.varHandle(byte.class, groupElement("Data4"), PathElement.sequenceElement());
	}
    
    public static final class OVERLAPPED
    {
    	public static final GroupLayout LAYOUT = structLayout(
			C_POINTER, 
			C_POINTER, 
			unionLayout(
				structLayout(
					C_INT.withName("offsetLow"),
					C_INT.withName("offsetHigh")
				),
				C_LONG_LONG.withName("offset"),
				C_POINTER.withName("pointer")
			).withName("union"),
			C_POINTER.withName("event")
		);
    	
    	public static final VarHandle offset = LAYOUT.varHandle(long.class, groupElement("union"), groupElement("offset"));
    }
    
    public enum READ_DIRECTORY_NOTIFY_INFORMATION_CLASS
    {
    	ReadDirectoryNotifyInformation(0),
    	ReadDirectoryNotifyExtendedInformation(1);
    	
    	int value;
    	
    	READ_DIRECTORY_NOTIFY_INFORMATION_CLASS (int value)
    	{
    		this.value = value;
    	}
    	
    	public int value ()
    	{
    		return value;
    	}
    }
    
    // methods
	
	public static final MethodHandle CancelIoEx;
	
	public static final MethodHandle CloseHandle;
	
	public static final MethodHandle CreateFileA;

	public static final MethodHandle CreateIoCompletionPort;

	public static final MethodHandle GetLastError;
	
	public static final MethodHandle GetOverlappedResultEx;
	
	public static final MethodHandle GetQueuedCompletionStatus;
	
	public static final MethodHandle LockFileEx;
	
	public static final MethodHandle PostQueuedCompletionStatus;

	public static final MethodHandle ReadDirectoryChangesW;
	
	public static final MethodHandle ReadDirectoryChangesExW;
	
	public static final MethodHandle ReadFile;
	
	public static final MethodHandle ReadFileEx;

	static
	{
		System.loadLibrary("kernel32");
		
		final var loader = SymbolLookup.loaderLookup();
    	
    	final var linker = CLinker.getInstance();

		CancelIoEx = linker.downcallHandle(
			loader.lookup("CancelIoEx").get(),
			methodType(int.class, MemoryAddress.class, MemoryAddress.class),
			FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER)
		);

		CloseHandle = linker.downcallHandle(
			loader.lookup("CloseHandle").get(),
			MethodType.methodType(int.class, MemoryAddress.class),
			FunctionDescriptor.of(C_INT, C_POINTER)
		);
    	
		CreateFileA = linker.downcallHandle(
			loader.lookup("CreateFileA").get(),
			MethodType.methodType(MemoryAddress.class, MemoryAddress.class, int.class, int.class, MemoryAddress.class, int.class, int.class, MemoryAddress.class),
			FunctionDescriptor.of(C_POINTER, C_POINTER, C_INT, C_INT, C_POINTER, C_INT, C_INT, C_POINTER)
		);

		CreateIoCompletionPort = linker.downcallHandle(
			loader.lookup("CreateIoCompletionPort").get(),
			methodType(MemoryAddress.class, MemoryAddress.class, MemoryAddress.class, MemoryAddress.class, int.class),
			FunctionDescriptor.of(C_POINTER, C_POINTER, C_POINTER, C_POINTER, C_INT)
		);
		
		GetLastError = linker.downcallHandle(
			loader.lookup("GetLastError").get(),
			MethodType.methodType(int.class),
			FunctionDescriptor.of(C_INT)
		);
		
		GetOverlappedResultEx = linker.downcallHandle(
			loader.lookup("GetOverlappedResultEx").get(),
			methodType(int.class, MemoryAddress.class, MemoryAddress.class, MemoryAddress.class, int.class, int.class),
			FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER, C_POINTER, C_INT, C_INT)
		);

		GetQueuedCompletionStatus = linker.downcallHandle(
			loader.lookup("GetQueuedCompletionStatus").get(),
			methodType(int.class, MemoryAddress.class, MemoryAddress.class, MemoryAddress.class, MemoryAddress.class, int.class),
			FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER, C_POINTER, C_POINTER, C_INT)
		);

		LockFileEx = linker.downcallHandle(
			loader.lookup("LockFileEx").get(),
			methodType(int.class, MemoryAddress.class, int.class, int.class, int.class, int.class, MemoryAddress.class),
			FunctionDescriptor.of(C_INT, C_POINTER, C_INT, C_INT, C_INT, C_INT, C_POINTER)
		);
		
		PostQueuedCompletionStatus = linker.downcallHandle(
			loader.lookup("PostQueuedCompletionStatus").get(),
			methodType(int.class, MemoryAddress.class, int.class, MemoryAddress.class, MemoryAddress.class),
			FunctionDescriptor.of(C_INT, C_POINTER, C_INT, C_POINTER, C_POINTER)
		);
		
		ReadDirectoryChangesW = linker.downcallHandle(
			loader.lookup("ReadDirectoryChangesW").get(),
			methodType(int.class, MemoryAddress.class, MemoryAddress.class, int.class, int.class, int.class, MemoryAddress.class, MemoryAddress.class, MemoryAddress.class),
			FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER, C_INT, C_INT, C_INT, C_POINTER, C_POINTER, C_POINTER)
		);
		
		ReadDirectoryChangesExW = linker.downcallHandle(
			loader.lookup("ReadDirectoryChangesExW").get(),
			methodType(int.class, MemoryAddress.class, MemoryAddress.class, int.class, int.class, int.class, MemoryAddress.class, MemoryAddress.class, MemoryAddress.class, int.class),
			FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER, C_INT, C_INT, C_INT, C_POINTER, C_POINTER, C_POINTER, C_INT)
		);
		
		ReadFile = linker.downcallHandle(
			loader.lookup("ReadFile").get(),
			methodType(int.class, MemoryAddress.class, MemoryAddress.class, int.class, MemoryAddress.class, MemoryAddress.class),
			FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER, C_INT, C_POINTER, C_POINTER)
		);
		
		ReadFileEx = linker.downcallHandle(
			loader.lookup("ReadFileEx").get(),
			methodType(int.class, MemoryAddress.class, MemoryAddress.class, int.class, MemoryAddress.class, MemoryAddress.class),
			FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER, C_INT, C_POINTER, C_POINTER)
		);
	}
}
