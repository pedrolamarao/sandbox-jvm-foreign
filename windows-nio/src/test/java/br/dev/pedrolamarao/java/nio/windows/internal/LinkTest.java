package br.dev.pedrolamarao.java.nio.windows.internal;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import br.dev.pedrolamarao.java.foreign.windows.Ws2_32;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.ResourceScope;

public final class LinkTest
{
	@Test
	public void bind () throws Throwable
	{
		final var address = MemorySegment.allocateNative(Ws2_32.sockaddr_in.LAYOUT, ResourceScope.globalScope()).fill((byte) 0);
		Ws2_32.sockaddr_in.family.set(address, (short) Ws2_32.AF_INET);
		
		try (var link = new Link(Ws2_32.AF_INET, Ws2_32.SOCK_STREAM, Ws2_32.IPPROTO_TCP)) {
			link.bind(address);
		}
	}
	
	@Test
	@Timeout(value=1000, unit=TimeUnit.MILLISECONDS)
	public void connect () throws Throwable
	{
		final var address = MemorySegment.allocateNative(Ws2_32.sockaddr_in.LAYOUT, ResourceScope.globalScope()).fill((byte) 0);
		Ws2_32.sockaddr_in.family.set(address, (short) Ws2_32.AF_INET);
		
		try (var link = new Link(Ws2_32.AF_INET, Ws2_32.SOCK_STREAM, Ws2_32.IPPROTO_TCP))
		{
			link.bind(address);

			Ws2_32.sockaddr_in.port.set(address, (short) 12345);
			Ws2_32.sockaddr_in.addr.set(address, (int) ((1 << 24) | 0x7F));

			try (var operation = new Operation()) { link.connect(operation, address); }
		}
	}
}
