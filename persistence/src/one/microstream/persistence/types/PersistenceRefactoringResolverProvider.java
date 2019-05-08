package one.microstream.persistence.types;

import static one.microstream.X.notNull;

import one.microstream.collections.types.XGettingEnum;

public interface PersistenceRefactoringResolverProvider extends PersistenceTypeResolverProvider
{
	@Override
	public PersistenceRefactoringResolver provideResolver();
	
	
	
	public static PersistenceRefactoringResolverProvider New(
		final PersistenceRefactoringMappingProvider refactoringMappingProvider
	)
	{
		return new PersistenceRefactoringResolverProvider.Default(
			notNull(refactoringMappingProvider),
			PersistenceRefactoringTypeIdentifierBuilder.createDefaultRefactoringLegacyTypeIdentifierBuilders(),
			PersistenceRefactoringMemberIdentifierBuilder.createDefaultRefactoringLegacyMemberIdentifierBuilders(),
			PersistenceRefactoringMemberIdentifierBuilder.createDefaultRefactoringCurrentMemberIdentifierBuilders()
		);
	}
	
	public static PersistenceRefactoringResolverProvider New(
		final PersistenceRefactoringMappingProvider                                 refactoringMappingProvider    ,
		final XGettingEnum<? extends PersistenceRefactoringTypeIdentifierBuilder>   sourceTypeIdentifierBuilders  ,
		final XGettingEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> sourceMemberIdentifierBuilders,
		final XGettingEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> targetMemberIdentifierBuilders
	)
	{
		return new PersistenceRefactoringResolverProvider.Default(
			notNull(refactoringMappingProvider)    ,
			notNull(sourceTypeIdentifierBuilders)  ,
			notNull(sourceMemberIdentifierBuilders),
			notNull(targetMemberIdentifierBuilders)
		);
	}
	
	public static PersistenceRefactoringResolverProvider Caching(
		final PersistenceRefactoringMappingProvider refactoringMappingProvider
	)
	{
		return new PersistenceRefactoringResolverProvider.Caching(
			notNull(refactoringMappingProvider),
			PersistenceRefactoringTypeIdentifierBuilder.createDefaultRefactoringLegacyTypeIdentifierBuilders(),
			PersistenceRefactoringMemberIdentifierBuilder.createDefaultRefactoringLegacyMemberIdentifierBuilders(),
			PersistenceRefactoringMemberIdentifierBuilder.createDefaultRefactoringCurrentMemberIdentifierBuilders()
		);
	}
	
	public static PersistenceRefactoringResolverProvider Caching(
		final PersistenceRefactoringMappingProvider                                 refactoringMappingProvider    ,
		final XGettingEnum<? extends PersistenceRefactoringTypeIdentifierBuilder>   sourceTypeIdentifierBuilders  ,
		final XGettingEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> sourceMemberIdentifierBuilders,
		final XGettingEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> targetMemberIdentifierBuilders
	)
	{
		return new PersistenceRefactoringResolverProvider.Caching(
			notNull(refactoringMappingProvider)    ,
			notNull(sourceTypeIdentifierBuilders)  ,
			notNull(sourceMemberIdentifierBuilders),
			notNull(targetMemberIdentifierBuilders)
		);
	}
	
	public class Default implements PersistenceRefactoringResolverProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final PersistenceRefactoringMappingProvider                                 refactoringMappingProvider    ;
		final XGettingEnum<? extends PersistenceRefactoringTypeIdentifierBuilder>   sourceTypeIdentifierBuilders  ;
		final XGettingEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> sourceMemberIdentifierBuilders;
		final XGettingEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> targetMemberIdentifierBuilders;
		
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Default(
			final PersistenceRefactoringMappingProvider                                 refactoringMappingProvider    ,
			final XGettingEnum<? extends PersistenceRefactoringTypeIdentifierBuilder>   sourceTypeIdentifierBuilders  ,
			final XGettingEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> sourceMemberIdentifierBuilders,
			final XGettingEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> targetMemberIdentifierBuilders
		)
		{
			super();
			this.refactoringMappingProvider     = refactoringMappingProvider    ;
			this.sourceTypeIdentifierBuilders   = sourceTypeIdentifierBuilders  ;
			this.sourceMemberIdentifierBuilders = sourceMemberIdentifierBuilders;
			this.targetMemberIdentifierBuilders = targetMemberIdentifierBuilders;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public PersistenceRefactoringResolver provideResolver()
		{
			// nifty: immure at creation time, not before.
			return new PersistenceRefactoringResolver.Default(
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

		transient PersistenceRefactoringResolver cachedResolver;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Caching(
			final PersistenceRefactoringMappingProvider                                 refactoringMappingProvider    ,
			final XGettingEnum<? extends PersistenceRefactoringTypeIdentifierBuilder>   sourceTypeIdentifierBuilders  ,
			final XGettingEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> sourceMemberIdentifierBuilders,
			final XGettingEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> targetMemberIdentifierBuilders
		)
		{
			super(
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
		public synchronized PersistenceRefactoringResolver provideResolver()
		{
			if(this.cachedResolver == null)
			{
				this.cachedResolver = super.provideResolver();
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
			
			this.provideResolver();
			
			return true;
		}

		@Override
		public synchronized void clear()
		{
			this.cachedResolver = null;
		}
	}
	
}
