package one.microstream.afs;

import java.util.function.Function;

public interface AccessManager
{
	public AReadableFile createDirectory(AMutableDirectory parent, String identifier);
	
	public AReadableFile createFile(AMutableDirectory parent, String identifier);
	
	public AReadableFile createFile(AMutableDirectory parent, String name, String type);
	
	public AReadableFile createFile(AMutableDirectory parent, String identifier, String name, String type);
	
	


	public boolean isUsed(ADirectory directory);
	
	public boolean isMutating(ADirectory directory);

	public boolean isReading(AFile file);
	
	public boolean isWriting(AFile file);
	
	
	public boolean isUsed(ADirectory directory, Object owner);
	
	public boolean isMutating(ADirectory directory, Object owner);

	public boolean isReading(AFile file, Object owner);
	
	public boolean isWriting(AFile file, Object owner);
	
	// (29.04.2020 TM)TODO: priv#49: executeIfNot~ methods? Or coverable by execute~ methods below?
	
	
	public AReadableFile useReading(AFile file, Object owner);
	
	public AWritableFile useWriting(AFile file, Object owner);
	
	public AUsedDirectory use(ADirectory directory, Object owner);
	
	public AMutableDirectory useMutating(ADirectory directory, Object owner);
	
	public default Object defaultOwner()
	{
		return Thread.currentThread();
	}

	public default <R> R executeMutating(
		final ADirectory                             directory,
		final Function<? super AMutableDirectory, R> logic
	)
	{
		return this.executeMutating(directory, this.defaultOwner(), logic);
	}

	public default <R> R executeWriting(
		final AFile                              file ,
		final Function<? super AWritableFile, R> logic
	)
	{
		return this.executeWriting(file, this.defaultOwner(), logic);
	}
	
	public default <R> R executeMutating(
		final ADirectory                             directory,
		final Object                                 owner    ,
		final Function<? super AMutableDirectory, R> logic
	)
	{
		synchronized(directory)
		{
			final boolean isUsed = this.isUsed(directory, owner);
			
			final AMutableDirectory mDirectory = this.useMutating(directory, owner);
			
			try
			{
				return logic.apply(mDirectory);
			}
			finally
			{
				if(isUsed)
				{
					mDirectory.releaseMutating();
				}
				else
				{
					mDirectory.release();
				}
			}
		}
	}
	
	public default <R> R executeWriting(
		final AFile                              file ,
		final Object                             owner,
		final Function<? super AWritableFile, R> logic
	)
	{
		synchronized(file)
		{
			final boolean isReading = this.isReading(file, owner);
			
			final AWritableFile mFile = this.useWriting(file, owner);
			
			try
			{
				return logic.apply(mFile);
			}
			finally
			{
				if(isReading)
				{
					mFile.releaseWriting();
				}
				else
				{
					mFile.release();
				}
			}
		}
	}
	
}