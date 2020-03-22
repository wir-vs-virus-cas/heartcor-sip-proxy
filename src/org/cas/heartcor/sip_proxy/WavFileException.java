package org.cas.heartcor.sip_proxy;

//needed for the WavFile class

public class WavFileException extends Throwable
{
	public WavFileException()
	{
		super();
	}

	public WavFileException(String message)
	{
		super(message);
	}

	public WavFileException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public WavFileException(Throwable cause)
	{
		super(cause);
	}
}
