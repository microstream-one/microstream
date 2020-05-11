package one.microstream.afs;

import java.nio.ByteBuffer;

import one.microstream.io.BufferProvider;

public interface IoHandler
{
	// ONLY the IO-Aspect, not the AFS-management-level aspect
	public boolean open(AReadableFile file);

	// ONLY the IO-Aspect, not the AFS-management-level aspect
	public boolean isOpen(AReadableFile file);
	
	// ONLY the IO-Aspect, not the AFS-management-level aspect
	public boolean close(AReadableFile file);

	// ONLY the IO-Aspect, not the AFS-management-level aspect
	public boolean isClosed(AReadableFile file);
	

	public boolean ensure(AReadableFile file);

	// (10.05.2020 TM)FIXME: priv#49: enum for triple-state return value? Use for dual-value above as well?
	public boolean ensure(AWritableFile file);
	
	
	
	public ByteBuffer readBytes(AReadableFile sourceFile);
	
	public ByteBuffer readBytes(AReadableFile sourceFile, long position);
	
	public ByteBuffer readBytes(AReadableFile sourceFile, long position, long length);
	
	
	public long readBytes(AReadableFile sourceFile, ByteBuffer targetBuffer);
	
	public long readBytes(AReadableFile sourceFile, ByteBuffer targetBuffer, long position);
	
	public long readBytes(AReadableFile sourceFile, ByteBuffer targetBuffer, long position, long length);
	
	
	public long readBytes(AReadableFile sourceFile, BufferProvider bufferProvider);
	
	public long readBytes(AReadableFile sourceFile, BufferProvider bufferProvider, long position);
	
	public long readBytes(AReadableFile sourceFile, BufferProvider bufferProvider, long position, long length);
	
	
	
	public long writeBytes(AWritableFile targetFile, Iterable<? extends ByteBuffer> sourceBuffers);
	
	
	public long copyTo(AReadableFile sourceFile, AWritableFile target);
	
	public long copyTo(AReadableFile sourceFile, AWritableFile target, long sourcePosition);
	
	public long copyTo(AReadableFile sourceFile, AWritableFile target, long sourcePosition, long length);
	
	

	public void moveFile(AWritableFile sourceFile, AWritableFile targetFile);
	
	public void deleteFile(AWritableFile file);
	
	public long length(AFile file);
	

}
