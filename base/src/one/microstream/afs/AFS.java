package one.microstream.afs;

import java.util.function.Function;

public class AFS
{

	public static <R> R execute(
		final AFile                      file ,
		final Function<AReadableFile, R> logic
	)
	{
		final AReadableFile rFile = file.useReading();
		try
		{
			return logic.apply(rFile);
		}
		finally
		{
			rFile.release();
		}
	}
	
	
	public static <R> R executeWriting(
		final AFile                              file ,
		final Function<? super AWritableFile, R> logic
	)
	{
		return executeWriting(file, file.defaultUser(), logic);
	}
	
	public static <R> R executeWriting(
		final AFile                              file ,
		final Object                             user ,
		final Function<? super AWritableFile, R> logic
	)
	{
		// no locking needed, here since the implementation of #useWriting has to cover that
		final AWritableFile writableFile = file.useWriting(user);
		try
		{
			return logic.apply(writableFile);
		}
		finally
		{
			writableFile.release();
		}
	}
	
	public static <R> R tryExecuteWriting(
		final AFile                              file ,
		final Function<? super AWritableFile, R> logic
	)
	{
		return tryExecuteWriting(file, file.defaultUser(), logic);
	}
	
	public static <R> R tryExecuteWritingDefaulting(
		final AFile                              file        ,
		final R                                  defaultValue,
		final Function<? super AWritableFile, R> logic
	)
	{
		return tryExecuteWritingDefaulting(file, file.defaultUser(), defaultValue, logic);
	}
	
	public static <R> R tryExecuteWriting(
		final AFile                              file ,
		final Object                             user ,
		final Function<? super AWritableFile, R> logic
	)
	{
		return tryExecuteWritingDefaulting(file, user, null, logic);
	}
	
	public static <R> R tryExecuteWritingDefaulting(
		final AFile                              file       ,
		final Object                             user       ,
		final R                                  defaultValue,
		final Function<? super AWritableFile, R> logic
	)
	{
		// no locking needed, here since the implementation of #useWriting has to cover that
		final AWritableFile writableFile = file.tryUseWriting(user);
		if(writableFile == null)
		{
			return defaultValue;
		}
		
		try
		{
			return logic.apply(writableFile);
		}
		finally
		{
			writableFile.release();
		}
	}
	
	// (06.06.2020 TM)FIXME: priv#49: need waitingUse~ and waitingExecute~ as well(?).
	
	
	public static void close(final AReadableFile file, final Throwable cause)
	{
		if(file == null)
		{
			return;
		}
		
		try
		{
			file.close();
		}
		catch(final Throwable t)
		{
			if(cause != null)
			{
				t.addSuppressed(cause);
			}
			throw t;
		}
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 * 
	 * @throws UnsupportedOperationException
	 */
	private AFS()
	{
		// static only
		throw new UnsupportedOperationException();
	}
}
