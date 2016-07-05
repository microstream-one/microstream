package net.jadoth.storage.types;


final class TypeInFile
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
