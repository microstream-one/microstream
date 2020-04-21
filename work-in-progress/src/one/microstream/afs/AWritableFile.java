package one.microstream.afs;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

public interface AWritableFile extends AReadableFile
{
	// (21.04.2020 TM)FIXME: priv#49: overhaul with new concept
	
	public AWritableFile moveTo(AWritableDirectory destination);
	
	public long write(Iterable<? extends ByteBuffer> sources);
	
	public void delete();
	
	public <C extends Consumer<? super AWritableFile>> C waitOnDelete(C callback);
	
	
	public boolean isDeleted();
		
}
