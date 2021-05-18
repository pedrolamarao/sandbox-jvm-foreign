package br.dev.pedrolamarao.java.nio.windows.internal;

import br.dev.pedrolamarao.java.foreign.windows.Kernel32;
import jdk.incubator.foreign.MemoryAddress;

@SuppressWarnings("preview")
public sealed interface Device extends AutoCloseable
	permits Bus, IoDevice
{	
	default void close () throws Exception
	{
		try
		{
			final var result = (int) Kernel32.CloseHandle.invokeExact(handle());
			assert(result != 0);
		} 
		catch (Throwable e)
		{
			throw new Exception("close: failure", e);
		}
	}
	
	public MemoryAddress handle ();
}
