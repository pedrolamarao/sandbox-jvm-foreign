package br.dev.pedrolamarao.java.nio.windows;

import java.nio.channels.CompletionHandler;
import java.util.concurrent.CompletableFuture;

final class CompletableFutureHandler <T> implements CompletionHandler<T, CompletableFuture<T>>
{
	@Override 
	public void completed (T result, CompletableFuture<T> future)
	{
		future.complete(result);
	}

	@Override 
	public void failed (Throwable cause, CompletableFuture<T> future)
	{
		future.completeExceptionally(cause);				
	}
}