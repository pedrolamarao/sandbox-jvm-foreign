package br.dev.pedrolamarao.java.foreign.windows;

import static br.dev.pedrolamarao.java.foreign.windows.Kernel32.FILE_SHARE_READ;
import static br.dev.pedrolamarao.java.foreign.windows.Kernel32.GENERIC_READ;
import static br.dev.pedrolamarao.java.foreign.windows.Kernel32.INVALID_HANDLE_VALUE;
import static br.dev.pedrolamarao.java.foreign.windows.Kernel32.OPEN_EXISTING;
import static java.nio.charset.StandardCharsets.UTF_8;
import static jdk.incubator.foreign.CLinker.C_INT;
import static jdk.incubator.foreign.CLinker.C_POINTER;
import static jdk.incubator.foreign.MemoryAddress.NULL;
import static jdk.incubator.foreign.MemoryAddress.ofLong;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.ResourceScope;

public final class Kernel32Test
{
	@TempDir Path tmp;
	
	@SuppressWarnings("unused")
	@Test
	public void createFileA () throws Throwable
	{
		final var path = Files.createTempFile(tmp, "createFileA", ".tmp");
		try (var scope = ResourceScope.newConfinedScope()) {
			final var path_ntbs = CLinker.toCString(path.toString(), UTF_8, scope);
			final var handle = (MemoryAddress) Kernel32.CreateFileA.invokeExact(path_ntbs.address(), GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, 0, NULL);
			assertNotEquals(handle, INVALID_HANDLE_VALUE);
			final var r0 = (int) Kernel32.CloseHandle.invokeExact(handle);
		}
	}
	
	@SuppressWarnings("unused")
	@Test
	public void createIoCompletionPort () throws Throwable
	{
		final var handle = (MemoryAddress) Kernel32.CreateIoCompletionPort.invokeExact(INVALID_HANDLE_VALUE, NULL, NULL, 0);
		assertNotEquals(NULL, handle);
		final var r0 = (int) Kernel32.CloseHandle.invokeExact(handle);
	}
	
	@SuppressWarnings("unused")
	@Test
	public void getLastError () throws Throwable
	{
		final var r0 = (int) Kernel32.GetLastError.invokeExact();
	}

	@SuppressWarnings("unused")
	@Test
	public void getQueuedCompletionStatus () throws Throwable
	{
		final var handle = (MemoryAddress) Kernel32.CreateIoCompletionPort.invokeExact(INVALID_HANDLE_VALUE, NULL, NULL, 0);
		assertNotEquals(NULL, handle);
		final var r0 = (int) Kernel32.PostQueuedCompletionStatus.invokeExact(handle, 1, ofLong(2), ofLong(3));
		assertNotEquals(0, r0);
		try (var scope = ResourceScope.newConfinedScope()) {
			final var dataRef = MemorySegment.allocateNative(C_INT, scope);
			final var keyRef = MemorySegment.allocateNative(C_POINTER, scope);
			final var operationRef = MemorySegment.allocateNative(C_POINTER, scope);
			final var r1 = (int) Kernel32.GetQueuedCompletionStatus.invokeExact(handle, dataRef.address(), keyRef.address(), operationRef.address(), 0);
			assertNotEquals(0, r1);
			assertEquals(1, MemoryAccess.getInt(dataRef));
			assertEquals(2, MemoryAccess.getLong(keyRef));
			assertEquals(3, MemoryAccess.getLong(operationRef));
		}
		final var r2 = (int) Kernel32.CloseHandle.invokeExact(handle);
	}
	
	@SuppressWarnings("unused")
	@Test
	public void lockFileEx () throws Throwable
	{
		final var path = Files.createTempFile(tmp, "lockFileEx", ".tmp");
		try (var scope = ResourceScope.newConfinedScope()) {
			final var path_ntbs = CLinker.toCString(path.toString(), UTF_8, scope);
			final var handle = (MemoryAddress) Kernel32.CreateFileA.invokeExact(path_ntbs.address(), GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, 0, NULL);
			assertNotEquals(handle, INVALID_HANDLE_VALUE);
			final var overlapped = MemorySegment.allocateNative(Kernel32.OVERLAPPED.LAYOUT, scope).fill((byte) 0);
			final var r0 = (int) Kernel32.LockFileEx.invokeExact(handle, 0, 0, 0, 0, overlapped.address());
			assertNotEquals(0, r0);
			final var r1 = (int) Kernel32.CloseHandle.invokeExact(handle);
		}
	}

	@SuppressWarnings("unused")
	@Test
	public void postQueuedCompletionStatus () throws Throwable
	{
		final var handle = (MemoryAddress) Kernel32.CreateIoCompletionPort.invokeExact(INVALID_HANDLE_VALUE, NULL, NULL, 0);
		assertNotEquals(NULL, handle);
		final var r0 = (int) Kernel32.PostQueuedCompletionStatus.invokeExact(handle, 1, ofLong(2), ofLong(3));
		assertNotEquals(0, r0);
		final var r1 = (int) Kernel32.CloseHandle.invokeExact(handle);
	}
	
	@SuppressWarnings("unused")
	@Test
	public void readFile () throws Throwable
	{
		final var path = Files.createTempFile(tmp, "readFile", ".tmp");
		try (var scope = ResourceScope.newConfinedScope()) {
			final var path_ntbs = CLinker.toCString(path.toString(), UTF_8, scope);
			final var handle = (MemoryAddress) Kernel32.CreateFileA.invokeExact(path_ntbs.address(), GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, 0, NULL);
			assertNotEquals(handle, INVALID_HANDLE_VALUE);
			final var buffer = MemorySegment.allocateNative(4096, scope);
			final var r0 = (int) Kernel32.ReadFile.invokeExact(handle, buffer.address(), (int) buffer.byteSize(), NULL, NULL);
			assertNotEquals(0, r0);
			final var r1 = (int) Kernel32.CloseHandle.invokeExact(handle);
		}
	}
}
