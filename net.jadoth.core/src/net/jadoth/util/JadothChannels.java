package net.jadoth.util;

import java.nio.channels.Channel;

public final class JadothChannels
{
	public static final void closeSilent(final Channel channel)
	{
		if(channel == null)
		{
			return;
		}
		try
		{
			channel.close();
		}
		catch(final Exception ex)
		{
			// silent
		}
		finally
		{
			try
			{
				channel.close();
			}
			catch(final Exception ex)
			{
				// silent
			}
		}
	}


	private JadothChannels()
	{
		// static only
		throw new UnsupportedOperationException();
	}
}
