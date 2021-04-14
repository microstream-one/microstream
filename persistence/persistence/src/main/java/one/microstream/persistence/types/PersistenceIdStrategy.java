package one.microstream.persistence.types;

import static one.microstream.X.notNull;

import one.microstream.afs.types.ADirectory;
import one.microstream.afs.types.AFile;
import one.microstream.persistence.internal.FileObjectIdStrategy;
import one.microstream.persistence.internal.FileTypeIdStrategy;

public interface PersistenceIdStrategy extends PersistenceObjectIdStrategy, PersistenceTypeIdStrategy
{
	public PersistenceObjectIdStrategy objectIdStragegy();
	
	public PersistenceTypeIdStrategy typeIdStragegy();
	
	@Override
	public default String strategyTypeNameObjectId()
	{
		return this.objectIdStragegy().strategyTypeNameObjectId();
	}
	
	@Override
	default String strategyTypeNameTypeId()
	{
		return this.typeIdStragegy().strategyTypeNameTypeId();
	}
	
	@Override
	public default PersistenceObjectIdProvider createObjectIdProvider()
	{
		return this.objectIdStragegy().createObjectIdProvider();
	}
	
	@Override
	public default PersistenceTypeIdProvider createTypeIdProvider()
	{
		return this.typeIdStragegy().createTypeIdProvider();
	}
	
	
	
	public static PersistenceIdStrategy NewInDirectory(final ADirectory directory)
	{
		return NewFromFiles(
			directory                             ,
			FileObjectIdStrategy.defaultFilename(),
			FileTypeIdStrategy.defaultFilename()
		);
	}
	
	public static PersistenceIdStrategy NewFromFiles(
		final ADirectory directory       ,
		final String     objectIdFilename,
		final String     typeIdFilename
	)
	{
		return NewFromFiles(
			directory.ensureFile(objectIdFilename),
			directory.ensureFile(typeIdFilename)
		);
	}
	
	public static PersistenceIdStrategy NewFromFiles(final AFile objectIdFile, final AFile typeIdFile)
	{
		return new PersistenceIdStrategy.Default(
			FileObjectIdStrategy.New(objectIdFile),
			FileTypeIdStrategy.New(typeIdFile)
		);
	}
	
	public static PersistenceIdStrategy New(
		final PersistenceObjectIdStrategy objectIdStrategy,
		final PersistenceTypeIdStrategy   typeIdStrategy
	)
	{
		return new PersistenceIdStrategy.Default(
			notNull(objectIdStrategy),
			notNull(typeIdStrategy)
		);
	}
	
	public class Default implements PersistenceIdStrategy
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final PersistenceObjectIdStrategy objectIdStrategy;
		private final PersistenceTypeIdStrategy   typeIdStrategy  ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final PersistenceObjectIdStrategy objectIdStrategy,
			final PersistenceTypeIdStrategy   typeIdStrategy
		)
		{
			super();
			this.objectIdStrategy = objectIdStrategy;
			this.typeIdStrategy   = typeIdStrategy  ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public PersistenceObjectIdStrategy objectIdStragegy()
		{
			return this.objectIdStrategy;
		}
		
		@Override
		public PersistenceTypeIdStrategy typeIdStragegy()
		{
			return this.typeIdStrategy;
		}
		
	}
	
}
