package net.jadoth.swizzling.types;

import static net.jadoth.X.notNull;

import java.io.File;

import net.jadoth.swizzling.internal.FileObjectIdStrategy;
import net.jadoth.swizzling.internal.FileTypeIdStrategy;

public interface SwizzleIdStrategy extends SwizzleObjectIdStrategy, SwizzleTypeIdStrategy
{
	public SwizzleObjectIdStrategy objectIdStragegy();
	
	public SwizzleTypeIdStrategy typeIdStragegy();
	
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
	public default SwizzleObjectIdProvider createObjectIdProvider()
	{
		return this.objectIdStragegy().createObjectIdProvider();
	}
	
	@Override
	public default SwizzleTypeIdProvider createTypeIdProvider()
	{
		return this.typeIdStragegy().createTypeIdProvider();
	}
	
	
	
	public static SwizzleIdStrategy NewInDirectory(final File directory)
	{
		return NewFromFiles(
			directory                             ,
			FileObjectIdStrategy.defaultFilename(),
			FileObjectIdStrategy.defaultFilename()
		);
	}
	
	public static SwizzleIdStrategy NewFromFiles(
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
	
	public static SwizzleIdStrategy NewFromFiles(final File objectIdFile, final File typeIdFile)
	{
		return new SwizzleIdStrategy.Implementation(
			FileObjectIdStrategy.New(objectIdFile),
			FileTypeIdStrategy.New(typeIdFile)
		);
	}
	
	public static SwizzleIdStrategy New(
		final SwizzleObjectIdStrategy objectIdStrategy,
		final SwizzleTypeIdStrategy   typeIdStrategy
	)
	{
		return new SwizzleIdStrategy.Implementation(
			notNull(objectIdStrategy),
			notNull(typeIdStrategy)
		);
	}
	
	public class Implementation implements SwizzleIdStrategy
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final SwizzleObjectIdStrategy objectIdStrategy;
		private final SwizzleTypeIdStrategy   typeIdStrategy  ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(
			final SwizzleObjectIdStrategy objectIdStrategy,
			final SwizzleTypeIdStrategy   typeIdStrategy
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
		public SwizzleObjectIdStrategy objectIdStragegy()
		{
			return this.objectIdStrategy;
		}
		
		@Override
		public SwizzleTypeIdStrategy typeIdStragegy()
		{
			return this.typeIdStrategy;
		}
		
	}
	
}
