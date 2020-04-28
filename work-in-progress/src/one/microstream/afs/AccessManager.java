package one.microstream.afs;

import java.util.function.Consumer;

public interface AccessManager
{
	public AReadableFile createDirectory(AMutableDirectory parent, String identifier);
	
	public AReadableFile createFile(AMutableDirectory parent, String identifier);
	
	public AReadableFile createFile(AMutableDirectory parent, String name, String type);
	
	public AReadableFile createFile(AMutableDirectory parent, String identifier, String name, String type);
	
	public AReadableFile useReading(AFile file, Object owner);
	
	public AWritableFile useWriting(AFile file, Object owner);
	
	public AMutableDirectory useMutating(ADirectory directory, Object owner);
	
	public default Object defaultOwner()
	{
		return Thread.currentThread();
	}

	public default <C extends Consumer<? super AMutableDirectory>> C executeMutating(
		final ADirectory directory,
		final C          logic
	)
	{
		return this.executeMutating(directory, this.defaultOwner(), logic);
	}

	public default <C extends Consumer<? super AWritableFile>> C executeWriting(
		final AFile file ,
		final C     logic
	)
	{
		return this.executeWriting(file, this.defaultOwner(), logic);
	}
	
	public default <C extends Consumer<? super AMutableDirectory>> C executeMutating(
		final ADirectory directory,
		final Object     owner    ,
		final C          logic
	)
	{
		synchronized(directory)
		{
			// (28.04.2020 TM)FIXME: priv#49: keep reading aspect!
			
			final AMutableDirectory mDirectory = this.useMutating(directory, owner);
			
			try
			{
				logic.accept(mDirectory);
			}
			catch(final Throwable t)
			{
				// (28.04.2020 TM)FIXME: priv#49: release
				throw t;
			}
			
			return logic;
		}
	}
	
	public default <C extends Consumer<? super AWritableFile>> C executeWriting(
		final AFile  file ,
		final Object owner,
		final C      logic
	)
	{
		synchronized(file)
		{
			// (28.04.2020 TM)FIXME: priv#49: keep reading aspect!
			
			final AWritableFile mFile = this.useWriting(file, owner);
			
			try
			{
				logic.accept(mFile);
			}
			catch(final Throwable t)
			{
				// (28.04.2020 TM)FIXME: priv#49: unregister owner?
				throw t;
			}
			
			return logic;
		}
	}
	
}