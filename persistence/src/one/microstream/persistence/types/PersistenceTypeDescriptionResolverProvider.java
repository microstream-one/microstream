package one.microstream.persistence.types;

import static one.microstream.X.notNull;

import one.microstream.collections.types.XGettingEnum;

public interface PersistenceTypeDescriptionResolverProvider
{
	public PersistenceTypeDescriptionResolver provideTypeDescriptionResolver();
	
	
	
	public static PersistenceTypeDescriptionResolverProvider New(
		final PersistenceTypeResolver               typeResolver              ,
		final PersistenceRefactoringMappingProvider refactoringMappingProvider
	)
	{
		return new PersistenceTypeDescriptionResolverProvider.Default(
			notNull(typeResolver),
			notNull(refactoringMappingProvider),
			PersistenceRefactoringTypeIdentifierBuilder.createDefaultRefactoringLegacyTypeIdentifierBuilders(),
			PersistenceRefactoringMemberIdentifierBuilder.createDefaultRefactoringLegacyMemberIdentifierBuilders(),
			PersistenceRefactoringMemberIdentifierBuilder.createDefaultRefactoringCurrentMemberIdentifierBuilders()
		);
	}
	
	public static PersistenceTypeDescriptionResolverProvider New(
		final PersistenceTypeResolver                                               typeResolver                  ,
		final PersistenceRefactoringMappingProvider                                 refactoringMappingProvider    ,
		final XGettingEnum<? extends PersistenceRefactoringTypeIdentifierBuilder>   sourceTypeIdentifierBuilders  ,
		final XGettingEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> sourceMemberIdentifierBuilders,
		final XGettingEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> targetMemberIdentifierBuilders
	)
	{
		return new PersistenceTypeDescriptionResolverProvider.Default(
			notNull(typeResolver)                  ,
			notNull(refactoringMappingProvider)    ,
			notNull(sourceTypeIdentifierBuilders)  ,
			notNull(sourceMemberIdentifierBuilders),
			notNull(targetMemberIdentifierBuilders)
		);
	}
	
	public static PersistenceTypeDescriptionResolverProvider Caching(
		final PersistenceTypeResolver               typeResolver              ,
		final PersistenceRefactoringMappingProvider refactoringMappingProvider
	)
	{
		return new PersistenceTypeDescriptionResolverProvider.Caching(
			notNull(typeResolver),
			notNull(refactoringMappingProvider),
			PersistenceRefactoringTypeIdentifierBuilder.createDefaultRefactoringLegacyTypeIdentifierBuilders(),
			PersistenceRefactoringMemberIdentifierBuilder.createDefaultRefactoringLegacyMemberIdentifierBuilders(),
			PersistenceRefactoringMemberIdentifierBuilder.createDefaultRefactoringCurrentMemberIdentifierBuilders()
		);
	}
	
	public static PersistenceTypeDescriptionResolverProvider Caching(
		final PersistenceTypeResolver                                               typeResolver                  ,
		final PersistenceRefactoringMappingProvider                                 refactoringMappingProvider    ,
		final XGettingEnum<? extends PersistenceRefactoringTypeIdentifierBuilder>   sourceTypeIdentifierBuilders  ,
		final XGettingEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> sourceMemberIdentifierBuilders,
		final XGettingEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> targetMemberIdentifierBuilders
	)
	{
		return new PersistenceTypeDescriptionResolverProvider.Caching(
			notNull(typeResolver)                  ,
			notNull(refactoringMappingProvider)    ,
			notNull(sourceTypeIdentifierBuilders)  ,
			notNull(sourceMemberIdentifierBuilders),
			notNull(targetMemberIdentifierBuilders)
		);
	}
	
	public class Default implements PersistenceTypeDescriptionResolverProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final PersistenceTypeResolver                                               typeResolver                  ;
		final PersistenceRefactoringMappingProvider                                 refactoringMappingProvider    ;
		final XGettingEnum<? extends PersistenceRefactoringTypeIdentifierBuilder>   sourceTypeIdentifierBuilders  ;
		final XGettingEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> sourceMemberIdentifierBuilders;
		final XGettingEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> targetMemberIdentifierBuilders;
		
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Default(
			final PersistenceTypeResolver                                               typeResolver                  ,
			final PersistenceRefactoringMappingProvider                                 refactoringMappingProvider    ,
			final XGettingEnum<? extends PersistenceRefactoringTypeIdentifierBuilder>   sourceTypeIdentifierBuilders  ,
			final XGettingEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> sourceMemberIdentifierBuilders,
			final XGettingEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> targetMemberIdentifierBuilders
		)
		{
			super();
			this.typeResolver                   = typeResolver                  ;
			this.refactoringMappingProvider     = refactoringMappingProvider    ;
			this.sourceTypeIdentifierBuilders   = sourceTypeIdentifierBuilders  ;
			this.sourceMemberIdentifierBuilders = sourceMemberIdentifierBuilders;
			this.targetMemberIdentifierBuilders = targetMemberIdentifierBuilders;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public PersistenceTypeDescriptionResolver provideTypeDescriptionResolver()
		{
			// nifty: immure at creation time, not before.
			return new PersistenceTypeDescriptionResolver.Default(
				this.typeResolver                                          ,
				this.refactoringMappingProvider.provideRefactoringMapping(),
				this.sourceTypeIdentifierBuilders  .immure(),
				this.sourceMemberIdentifierBuilders.immure(),
				this.targetMemberIdentifierBuilders.immure()
			);
		}
		
	}
	
	public class Caching extends Default implements one.microstream.typing.Caching
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		transient PersistenceTypeDescriptionResolver cachedResolver;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Caching(
			final PersistenceTypeResolver                                               typeResolver                  ,
			final PersistenceRefactoringMappingProvider                                 refactoringMappingProvider    ,
			final XGettingEnum<? extends PersistenceRefactoringTypeIdentifierBuilder>   sourceTypeIdentifierBuilders  ,
			final XGettingEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> sourceMemberIdentifierBuilders,
			final XGettingEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> targetMemberIdentifierBuilders
		)
		{
			super(
				typeResolver                  ,
				refactoringMappingProvider    ,
				sourceTypeIdentifierBuilders  ,
				sourceMemberIdentifierBuilders,
				targetMemberIdentifierBuilders
			);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public synchronized PersistenceTypeDescriptionResolver provideTypeDescriptionResolver()
		{
			if(this.cachedResolver == null)
			{
				this.cachedResolver = super.provideTypeDescriptionResolver();
			}
			
			return this.cachedResolver;
		}

		@Override
		public synchronized boolean hasFilledCache()
		{
			return this.cachedResolver != null;
		}

		@Override
		public synchronized boolean ensureFilledCache()
		{
			if(this.hasFilledCache())
			{
				return false;
			}
			
			this.provideTypeDescriptionResolver();
			
			return true;
		}

		@Override
		public synchronized void clear()
		{
			this.cachedResolver = null;
		}
	}
	
}
