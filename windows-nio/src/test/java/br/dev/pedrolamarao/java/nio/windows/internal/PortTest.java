package br.dev.pedrolamarao.java.nio.windows.internal;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import br.dev.pedrolamarao.java.foreign.windows.Ws2_32;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.ResourceScope;

public final class PortTest
{
	@Test
	public void listen () throws Throwable
	{
		final var address = MemorySegment.allocateNative(Ws2_32.sockaddr_in.LAYOUT, ResourceScope.globalScope());
		Ws2_32.sockaddr_in.family.set(address, (short) Ws2_32.AF_INET);
		
		try (var port = new Port(Ws2_32.AF_INET, Ws2_32.SOCK_STREAM, Ws2_32.IPPROTO_TCP)) {
			port.bind(address);
			port.listen(Ws2_32.SOMAXCONN);
		}
	}
	
	@Test
	@Timeout(value=1000, unit=TimeUnit.MILLISECONDS)
	public void accept () throws Throwable
	{
		final var address = MemorySegment.allocateNative(Ws2_32.sockaddr_in.LAYOUT, ResourceScope.globalScope());
		Ws2_32.sockaddr_in.family.set(address, (short) Ws2_32.AF_INET);
		
		try (var port = new Port(Ws2_32.AF_INET, Ws2_32.SOCK_STREAM, Ws2_32.IPPROTO_TCP))
		{
			port.bind(address);
			port.listen(Ws2_32.SOMAXCONN);
			
			try (var link = new Link(Ws2_32.AF_INET, Ws2_32.SOCK_STREAM, Ws2_32.IPPROTO_TCP))
			{
				try (var operation = new Operation()) {
					final var buffer = MemorySegment.allocateNative(2048, ResourceScope.globalScope());
					port.accept(operation, buffer, link);
					port.query(operation);
					port.cancel(operation);
				}
			}
		}
	}
}
