package net.jadoth.persistence.types;

import static net.jadoth.X.notNull;

import java.io.File;

import net.jadoth.persistence.internal.FileObjectIdStrategy;
import net.jadoth.persistence.internal.FileTypeIdStrategy;

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
	
	
	
	public static PersistenceIdStrategy NewInDirectory(final File directory)
	{
		return NewFromFiles(
			directory                             ,
			FileObjectIdStrategy.defaultFilename(),
			FileObjectIdStrategy.defaultFilename()
		);
	}
	
	public static PersistenceIdStrategy NewFromFiles(
		final File   directory       ,
		final String objectIdFilename,
		final String typeIdFilename
	)
	{
		return NewFromFiles(
			new File(directory, objectIdFilename),
			new File(directory, typeIdFilename)
		);
	}
	
	public static PersistenceIdStrategy NewFromFiles(final File objectIdFile, final File typeIdFile)
	{
		return new PersistenceIdStrategy.Implementation(
			FileObjectIdStrategy.New(objectIdFile),
			FileTypeIdStrategy.New(typeIdFile)
		);
	}
	
	public static PersistenceIdStrategy New(
		final PersistenceObjectIdStrategy objectIdStrategy,
		final PersistenceTypeIdStrategy   typeIdStrategy
	)
	{
		return new PersistenceIdStrategy.Implementation(
			notNull(objectIdStrategy),
			notNull(typeIdStrategy)
		);
	}
	
	public class Implementation implements PersistenceIdStrategy
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final PersistenceObjectIdStrategy objectIdStrategy;
		private final PersistenceTypeIdStrategy   typeIdStrategy  ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(
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
