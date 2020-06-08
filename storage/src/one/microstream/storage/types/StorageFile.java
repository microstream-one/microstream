package one.microstream.storage.types;

import one.microstream.afs.AFile;
import one.microstream.chars.VarString;
import one.microstream.chars.XChars;

public interface StorageFile
{
	public AFile file();
	
	
	
	public static VarString assembleNameAndSize(final VarString vs, final StorageFile file)
	{
		return vs.add(file.file().identifier() + "[" + file.file().size() + "]");
	}
	
	public abstract class Abstract implements StorageFile
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final AFile file;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Abstract(final AFile file)
		{
			super();
			this.file = file;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public AFile file()
		{
			return this.file;
		}
		
		@Override
		public String toString()
		{
			return XChars.systemString(this) + " (" + this.file + ")";
		}
		
	}
	
}
