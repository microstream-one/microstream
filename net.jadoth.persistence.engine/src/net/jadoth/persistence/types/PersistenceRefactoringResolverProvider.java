package net.jadoth.persistence.types;

import static net.jadoth.X.notNull;

import net.jadoth.X;
import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.collections.types.XGettingTable;

public interface PersistenceRefactoringResolverProvider
{
	public PersistenceRefactoringResolver provideRefactoringMapping();
	
	public static PersistenceRefactoringResolverProvider NewEmpty()
	{
		return new PersistenceRefactoringResolverProvider.Implementation(
			X.emptyTable()
		);
	}
	
	public static PersistenceRefactoringResolverProvider New(final XGettingTable<String, String> entries)
	{
		return new PersistenceRefactoringResolverProvider.Implementation(
			notNull(entries)
		);
	}
	
	public final class Implementation implements PersistenceRefactoringResolverProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final XGettingTable<String, String>                                         entries                       ;
		final XGettingEnum<? extends PersistenceRefactoringTypeIdentifierBuilder>   sourceTypeIdentifierBuilders  ;
		final XGettingEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> sourceMemberIdentifierBuilders;
		final XGettingEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> targetMemberIdentifierBuilders;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(
			final XGettingTable<String, String>                                         entries                       ,
			final XGettingEnum<? extends PersistenceRefactoringTypeIdentifierBuilder>   sourceTypeIdentifierBuilders  ,
			final XGettingEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> sourceMemberIdentifierBuilders,
			final XGettingEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> targetMemberIdentifierBuilders
		)
		{
			super();
			this.entries                        = entries                       ;
			this.sourceTypeIdentifierBuilders   = sourceTypeIdentifierBuilders  ;
			this.sourceMemberIdentifierBuilders = sourceMemberIdentifierBuilders;
			this.targetMemberIdentifierBuilders = targetMemberIdentifierBuilders;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public PersistenceRefactoringResolver provideRefactoringMapping()
		{
			// nifty: immure at creation time, not before.
			return new PersistenceRefactoringResolver.Implementation(
				this.entries                       .immure(),
				this.sourceTypeIdentifierBuilders  .immure(),
				this.sourceMemberIdentifierBuilders.immure(),
				this.targetMemberIdentifierBuilders.immure()
			);
		}
		
	}
	
}
