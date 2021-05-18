package br.dev.pedrolamarao.java.nio.windows;

public interface WindowsChannel
{
	void complete (long operation, boolean status, int result);
}
