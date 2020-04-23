package one.microstream.afs;

import java.nio.ByteBuffer;

public interface ActionListener
{
	public void onBeforeFileDelete(AFile fileToDelete);
	
	public void onAfterFileDelete(AFile deletedFile, long deletionTime);
	
	
	public void onBeforeFileMove(AFile fileToMove, AWritableDirectory targetDirectory);
	
	public void onAfterFileMove(AFile movedFile, AWritableDirectory sourceDirectory, long deletionTime);
	
	
	public void onBeforeFileWrite(AWritableFile targetFile, Iterable<? extends ByteBuffer> sources);

	public void onAfterFileWrite(AWritableFile targetFile, Iterable<? extends ByteBuffer> sources, long writeTime);
}