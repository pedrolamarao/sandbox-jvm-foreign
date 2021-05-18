package br.dev.pedrolamarao.java.nio.windows.internal;

@SuppressWarnings("preview")
public final record OperationState(boolean complete, int result, int data, int flags) 
{
}