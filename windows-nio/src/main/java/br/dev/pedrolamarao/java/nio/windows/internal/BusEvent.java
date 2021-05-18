package br.dev.pedrolamarao.java.nio.windows.internal;

import jdk.incubator.foreign.MemoryAddress;

@SuppressWarnings("preview")
public final record BusEvent(long key, MemoryAddress operation, boolean status, int data) 
{
	@Override
	public String toString ()
	{
		return String.format("{ handler = %d, operation = %d, status = %b, data = %d }", key, operation.toRawLongValue(), status, data);
	}
}
