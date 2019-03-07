package one.microstream.storage.types;

import one.microstream.persistence.types.Unpersistable;


final class TypeInFile implements Unpersistable
{
	final StorageEntityType.Implementation type    ;
	final StorageDataFile.Implementation   file    ;
	      TypeInFile                       hashNext;

	TypeInFile(
		final StorageEntityType.Implementation type    ,
		final StorageDataFile.Implementation   file    ,
		final TypeInFile                       hashNext
	)
	{
		super();
		this.type     = type    ;
		this.file     = file    ;
		this.hashNext = hashNext;
	}

}
