package br.dev.pedrolamarao.java.nio.windows.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import jdk.incubator.foreign.MemoryAddress;

public class BusTest
{
	@Test
	public void smoke () throws Exception, Throwable
	{
		try (var bus = new Bus()) { }
		try (var bus = new Bus(0)) { }
	}

	@Test
	public void pushPull () throws Exception, Throwable
	{
		try (var bus = new Bus()) 
		{
			bus.push(1, MemoryAddress.ofLong(2), 3);
			final var pull = bus.pull(Duration.ZERO);
			assertTrue(pull.isPresent());
			assertEquals(1, pull.get().key());
			assertEquals(MemoryAddress.ofLong(2), pull.get().operation());
			assertEquals(3, pull.get().data());
		}
	}

	@Test
	public void timeLimit () throws Exception, Throwable
	{
		try (var bus = new Bus()) 
		{
			final var pull = bus.pull(Duration.ZERO);
			assertTrue(pull.isEmpty());
		}
	}
}
