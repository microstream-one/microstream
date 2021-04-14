package one.microstream.storage.types;

import one.microstream.storage.exceptions.StorageException;
import one.microstream.storage.types.StorageTransactionsAnalysis.Logic;

public enum StorageTransactionsEntryType
{
	FILE_CREATION  ("CREATION"  , Logic.TYPE_FILE_CREATION  , Logic.LENGTH_FILE_CREATION  ),
	DATA_STORE     ("STORE"     , Logic.TYPE_STORE          , Logic.LENGTH_STORE          ),
	DATA_TRANSFER  ("TRANSFER"  , Logic.TYPE_TRANSFER       , Logic.LENGTH_TRANSFER       ),
	FILE_TRUNCATION("TRUNCATION", Logic.TYPE_FILE_TRUNCATION, Logic.LENGTH_FILE_TRUNCATION),
	FILE_DELETION  ("DELETION"  , Logic.TYPE_FILE_DELETION  , Logic.LENGTH_FILE_DELETION  );
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final String typeName;
	private final byte   code    ;
	private final int    length  ;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	private StorageTransactionsEntryType(final String typeName, final byte code, final int length)
	{
		this.typeName = typeName;
		this.code     = code    ;
		this.length   = length  ;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	public byte code()
	{
		return this.code;
	}
	
	public String typeName()
	{
		return this.typeName;
	}
	
	public int length()
	{
		return this.length;
	}
	
	@Override
	public String toString()
	{
		return this.typeName + "(" + this.code + "," + this.length + ")";
	}
	
	public static StorageTransactionsEntryType fromCode(final byte code)
	{
		switch(code)
		{
			case Logic.TYPE_FILE_CREATION  : return StorageTransactionsEntryType.FILE_CREATION  ;
			case Logic.TYPE_STORE          : return StorageTransactionsEntryType.DATA_STORE     ;
			case Logic.TYPE_TRANSFER       : return StorageTransactionsEntryType.DATA_TRANSFER  ;
			case Logic.TYPE_FILE_TRUNCATION: return StorageTransactionsEntryType.FILE_TRUNCATION;
			case Logic.TYPE_FILE_DELETION  : return StorageTransactionsEntryType.FILE_DELETION  ;
			default:
			{
				throw new StorageException("Unknown transactions entry type: " + code);
			}
		}
	}
	
}