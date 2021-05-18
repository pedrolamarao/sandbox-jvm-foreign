package br.dev.pedrolamarao.java.foreign.windows.sendfile;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public final class ProgramTest
{
	private AsynchronousServerSocketChannel port;

	@Test
	public void test () throws Exception
	{
		final InetSocketAddress address = (InetSocketAddress) port.getLocalAddress();
		final int status = new Program().run(new String[] { address.getHostString(), "" + address.getPort(), "build.gradle" });
		assertEquals(0, status);
	}
	
	@BeforeEach
	public void before () throws IOException
	{
		port = AsynchronousServerSocketChannel.open().bind(new InetSocketAddress(InetAddress.getLocalHost(), 0));
		new Thread(this::server).start();
	}
	
	public void server ()
	{
		try
		{
			final AsynchronousSocketChannel link = port.accept().get(1000, TimeUnit.MILLISECONDS);
			final ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
			while (true) {
				buffer.clear();
				final int read = link.read(buffer).get(1000, TimeUnit.MILLISECONDS);
				if (read == 0)
					break;
			}
			link.shutdownOutput();
			link.shutdownInput();
			link.close();
		} 
		catch (InterruptedException | ExecutionException | TimeoutException | IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void accept (AsynchronousServerSocketChannel port, ByteBuffer buffer)
	{
		port.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
			public void completed (AsynchronousSocketChannel link, Void context) {
				read(link, buffer);
			}
			public void failed (Throwable cause, Void context) { cause.printStackTrace(); }
		});
	}
	
	public void read (AsynchronousSocketChannel link, ByteBuffer buffer)
	{
		link.read(buffer.clear(), null, new CompletionHandler<Integer, Void>() {
			public void completed (Integer result, Void attachment) {
				try {
					if (result == 0)
						link.close();
					else
						read(link, buffer);
				}
				catch (IOException e) { }
			}
			public void failed (Throwable cause, Void attachment) { cause.printStackTrace(); }
		});
	}
	
	@AfterEach
	public void after () throws IOException
	{
		port.close();
	}
}
