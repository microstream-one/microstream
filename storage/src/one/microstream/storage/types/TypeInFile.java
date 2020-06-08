package one.microstream.storage.types;

import one.microstream.persistence.types.Unpersistable;


final class TypeInFile implements Unpersistable
{
	final StorageEntityType.Default type    ;
	final ZStorageDataFile.Default   file    ;
	      TypeInFile                hashNext;

	TypeInFile(
		final StorageEntityType.Default type    ,
		final ZStorageDataFile.Default   file    ,
		final TypeInFile                hashNext
	)
	{
		super();
		this.type     = type    ;
		this.file     = file    ;
		this.hashNext = hashNext;
	}

}
