package br.dev.pedrolamarao.java.nio.windows.internal;

import br.dev.pedrolamarao.java.foreign.windows.Kernel32;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.ResourceScope;

public final class Operation implements AutoCloseable
{
	private final MemorySegment operation;
	
	public Operation ()
	{
		this.operation = MemorySegment.allocateNative(Kernel32.OVERLAPPED.LAYOUT, ResourceScope.globalScope()).fill((byte) 0);
	}
	
	public void close ()
	{
	}
	
	//
	
	public MemoryAddress handle ()
	{
		return operation.address();
	}
	
	public long offset ()
	{
		return (long) Kernel32.OVERLAPPED.offset.get(operation);
	}
	
	public void offset (long value)
	{
		Kernel32.OVERLAPPED.offset.set(operation, value);
	}
	
	public void clear ()
	{
		operation.fill((byte) 0);
	}
}
