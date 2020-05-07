package one.microstream.afs;

import java.nio.ByteBuffer;

public interface IoHandler
{
	public void readBytes(AReadableFile sourceFile, ByteBuffer targetBuffer);
	
	public void writeBytes(AWritableFile targetFile, Iterable<? extends ByteBuffer> sourceBuffers);
	

	public void moveFile(AWritableFile sourceFile, AWritableFile targetFile);
	
	public void deleteFile(AWritableFile file);
}
