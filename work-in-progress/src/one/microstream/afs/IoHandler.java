package one.microstream.afs;

import java.nio.ByteBuffer;

import one.microstream.io.BufferProvider;

public interface IoHandler
{
	// ONLY the IO-Aspect, not the AFS-management-level aspect
	public boolean openReading(AReadableFile file);

	// ONLY the IO-Aspect, not the AFS-management-level aspect
	public boolean isOpenReading(AReadableFile file);
	
	// (11.05.2020 TM)FIXME: priv#49: may a readableFile closing logic close reading AND writing of a WritableFile?
	
	// ONLY the IO-Aspect, not the AFS-management-level aspect
	public boolean close(AReadableFile file);

	// ONLY the IO-Aspect, not the AFS-management-level aspect
	public boolean isClosed(AReadableFile file);
	

	public boolean ensure(AReadableFile file);

	public ActionReport ensure(AWritableFile file);
	

	public long length(AFile file);
	
	
	public ByteBuffer readBytes(AReadableFile sourceFile);
	
	public ByteBuffer readBytes(AReadableFile sourceFile, long position);
	
	public ByteBuffer readBytes(AReadableFile sourceFile, long position, long length);
	
	
	public long readBytes(AReadableFile sourceFile, ByteBuffer targetBuffer);
	
	public long readBytes(AReadableFile sourceFile, ByteBuffer targetBuffer, long position);
	
	public long readBytes(AReadableFile sourceFile, ByteBuffer targetBuffer, long position, long length);
	
	
	public long readBytes(AReadableFile sourceFile, BufferProvider bufferProvider);
	
	public long readBytes(AReadableFile sourceFile, BufferProvider bufferProvider, long position);
	
	public long readBytes(AReadableFile sourceFile, BufferProvider bufferProvider, long position, long length);
		
	
	public long copyTo(AReadableFile sourceFile, AWritableFile target);
	
	public long copyTo(AReadableFile sourceFile, AWritableFile target, long sourcePosition);
	
	public long copyTo(AReadableFile sourceFile, AWritableFile target, long sourcePosition, long length);
		
	
	
	public boolean openWriting(AWritableFile file);
	
	public boolean isOpenWriting(AWritableFile file);
	
	// ONLY the writing IO-Aspect, not the AFS-management-level aspect. Reading aspect remains open.
	public boolean closeWriting(AWritableFile file);
	
	public boolean isClosedWriting(AWritableFile file);

	// implicitely #closeWriting PLUS the AFS-management-level WRITING aspect. BOTH reading aspects remain!
	public boolean releaseWriting(AWritableFile file);
		
	public long writeBytes(AWritableFile targetFile, Iterable<? extends ByteBuffer> sourceBuffers);

	public void moveFile(AWritableFile sourceFile, AWritableFile targetFile);
	
	public void deleteFile(AWritableFile file);
	
}
