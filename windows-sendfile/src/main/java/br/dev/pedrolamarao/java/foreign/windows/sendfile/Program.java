package br.dev.pedrolamarao.java.foreign.windows.sendfile;

import static jdk.incubator.foreign.CLinker.C_POINTER;

import br.dev.pedrolamarao.java.foreign.windows.Kernel32;
import br.dev.pedrolamarao.java.foreign.windows.Mswsock;
import br.dev.pedrolamarao.java.foreign.windows.Ws2_32;
import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.ResourceScope;

public class Program
{    
    public static void main (String[] args)
    {
    	final Program program = new Program();
    	final int status = program.run(args);
    	System.exit(status);
    }
    
    public int run (String[] args)
    {
    	if (args.length < 3) {
    		System.err.println("usage: Program [host] [service] [file]");
    		return -1;
    	}
    	
    	final String host = args[0];
    	final String service = args[1];
    	final String file = args[2];
    	
    	final int result = send(host, service, file);
    	if (result != 0) {
    		System.err.println("error: communication failure: " + String.format("%d", result));
    	}
    	
    	return result;
    }
    
    public int send (String host, String service, String file)
    {    	
    	// downcalls
    	
    	try (var scope = ResourceScope.newConfinedScope())
		{
    		// resolve address
    		
			final var host_c = CLinker.toCString(host, scope);
			final var service_c = CLinker.toCString(service, scope);
			final var hint = MemorySegment.allocateNative(Ws2_32.addrinfo.LAYOUT, scope);
			Ws2_32.addrinfo.family.set(hint, Ws2_32.AF_INET);
			Ws2_32.addrinfo.socktype.set(hint, Ws2_32.SOCK_STREAM);
    		final var addressRef = MemorySegment.allocateNative(C_POINTER, scope);
			final var r0 = (int) Ws2_32.getaddrinfo.invokeExact(host_c.address(), service_c.address(), hint.address(), addressRef.address());
			if (r0 != 0) {
				final var error = (int) Kernel32.GetLastError.invokeExact();
				return error;
			}
			
			// UNSAFE!
			final var address = MemoryAccess.getAddress(addressRef).asSegment(Ws2_32.addrinfo.LAYOUT.byteSize(), scope);

			final var addressFamily = (int) Ws2_32.addrinfo.family.get(address);
			final var addressType = (int) Ws2_32.addrinfo.socktype.get(address);
			final var addressProtocol = (int) Ws2_32.addrinfo.protocol.get(address);
			final var addressData = MemoryAccess.getAddressAtOffset(address, 32); // #TODO: bug: unsupported carrier: MemoryAccess
			final var addressLength = (long) Ws2_32.addrinfo.addrlen.get(address);
			
			// acquire socket
			
			final var socketHandle = (MemoryAddress) Ws2_32.socket.invokeExact(addressFamily, addressType, addressProtocol);
			if (socketHandle == Ws2_32.INVALID_SOCKET) {
				final var error = (int) Kernel32.GetLastError.invokeExact();
				Ws2_32.freeaddrinfo.invoke(address);
				return error;
			}
			
			// connect socket
			
			final var r1 = (int) Ws2_32.connect.invokeExact(socketHandle, addressData, (int) addressLength);
			if (r1 == -1) {
				final var error = (int) Kernel32.GetLastError.invokeExact();
				Ws2_32.closesocket.invoke(socketHandle);
				Ws2_32.freeaddrinfo.invoke(address);
				return error;
			}

			Ws2_32.freeaddrinfo.invoke(address.address());
			
			// acquire file
			
			final var path_c = CLinker.toCString(file, scope);
			final var fileHandle = (MemoryAddress) Kernel32.CreateFileA.invokeExact(path_c.address(), Kernel32.GENERIC_READ, Kernel32.FILE_SHARE_READ, MemoryAddress.NULL, Kernel32.OPEN_EXISTING, 0, MemoryAddress.NULL);
			if (fileHandle == MemoryAddress.NULL) {
				final var error = (int) Kernel32.GetLastError.invokeExact();
				Ws2_32.closesocket.invoke(socketHandle);
				return error;
			}
			
			final var r2 = (int) Mswsock.TransmitFile.invokeExact(socketHandle, fileHandle, 0, 0, MemoryAddress.NULL, MemoryAddress.NULL, Mswsock.TF_DISCONNECT);
			if (r2 != 0) {
				final var error = (int) Kernel32.GetLastError.invokeExact();
				Kernel32.CloseHandle.invoke(fileHandle);
				Ws2_32.closesocket.invoke(socketHandle);
				return error;
			}
			
			Kernel32.CloseHandle.invoke(fileHandle);
			
			Ws2_32.closesocket.invoke(socketHandle);
		} 
    	catch (RuntimeException e)
    	{
    		throw e;
    	}
    	catch (Throwable e)
		{
    		throw new RuntimeException("send: oops", e);
		}

    	return 0;
    }
}
