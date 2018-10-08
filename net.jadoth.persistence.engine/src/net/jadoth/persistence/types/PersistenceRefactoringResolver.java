package net.jadoth.persistence.types;

import net.jadoth.X;
import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.collections.types.XImmutableEnum;
import net.jadoth.typing.KeyValue;

/**
 * A mapping that projects outdated identifiers (usually className#fieldName, but in case of root instances
 * also potentially arbitrary strings) to current identifiers.
 * 
 * @author TM
 *
 */
public interface PersistenceRefactoringResolver extends PersistenceTypeResolver
{
	/**
	 * Returns a key-value pair with the passed source identifier as the key and a mapped target identifier
	 * as the value. The value can be potentially null to indicate deletion.
	 * If the lookup did not yield any result, <code>null</code> is returned.
	 * 
	 * @param sourceIdentifier
	 * @return
	 */
	public KeyValue<String, String> lookup(String sourceIdentifier);
	
	/**
	 * Returns a key-value pair with the passed source member as the key and a mapped target member
	 * as the value. The value can be potentially null to indicate deletion.
	 * If the lookup did not yield any result, <code>null</code> is returned.
	 * 
	 * @param sourceType
	 * @param sourceMember
	 * @param targetType
	 * @return
	 */
	public KeyValue<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> resolveMember(
		PersistenceTypeDefinition       sourceType  ,
		PersistenceTypeDefinitionMember sourceMember,
		PersistenceTypeDefinition       targetType
	);
	

		
	public static PersistenceRefactoringResolver New(
		final PersistenceRefactoringMapping                                         refactoringMapping            ,
		final XGettingEnum<? extends PersistenceRefactoringTypeIdentifierBuilder>   sourceTypeIdentifierBuilders  ,
		final XGettingEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> sourceMemberIdentifierBuilders,
		final XGettingEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> targetMemberIdentifierBuilders
	)
	{
		return new Implementation(
			refactoringMapping                     ,
			sourceTypeIdentifierBuilders  .immure(),
			sourceMemberIdentifierBuilders.immure(),
			targetMemberIdentifierBuilders.immure()
		);
	}
	
	public final class Implementation implements PersistenceRefactoringResolver
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final PersistenceRefactoringMapping                                           refactoringMapping            ;
		final XImmutableEnum<? extends PersistenceRefactoringTypeIdentifierBuilder>   sourceTypeIdentifierBuilders  ;
		final XImmutableEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> sourceMemberIdentifierBuilders;
		final XImmutableEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> targetMemberIdentifierBuilders;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(
			final PersistenceRefactoringMapping                                           refactoringMapping            ,
			final XImmutableEnum<? extends PersistenceRefactoringTypeIdentifierBuilder>   sourceTypeIdentifierBuilders  ,
			final XImmutableEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> sourceMemberIdentifierBuilders,
			final XImmutableEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> targetMemberIdentifierBuilders
		)
		{
			super();
			this.refactoringMapping             = refactoringMapping            ;
			this.sourceTypeIdentifierBuilders   = sourceTypeIdentifierBuilders  ;
			this.sourceMemberIdentifierBuilders = sourceMemberIdentifierBuilders;
			this.targetMemberIdentifierBuilders = targetMemberIdentifierBuilders;
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
				
		@Override
		public String resolveRuntimeTypeName(final PersistenceTypeDescription typeDescription)
		{
			// search for a mapping entry with identifier builders in descending order of priority.
			final PersistenceRefactoringMapping refactoringMapping = this.refactoringMapping;
			for(final PersistenceRefactoringTypeIdentifierBuilder idBuilder : this.sourceTypeIdentifierBuilders)
			{
				final String                   identifier = idBuilder.buildTypeIdentifier(typeDescription);
				final KeyValue<String, String> entry      = refactoringMapping.lookup(identifier);
				if(entry == null)
				{
					continue;
				}
				
				// value might be null to indicate deletion
				return entry.value();
			}
			
			// if no refacting entry could be found, the original type name still applies.
			return typeDescription.typeName();
		}

		@Override
		public final KeyValue<String, String> lookup(final String sourceIdentifier)
		{
			return this.refactoringMapping.lookup(sourceIdentifier);
		}

		@Override
		public KeyValue<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> resolveMember(
			final PersistenceTypeDefinition       sourceType  ,
			final PersistenceTypeDefinitionMember sourceMember,
			final PersistenceTypeDefinition       targetType
		)
		{
			// search for a mapping entry with identifier builders in descending order of priority.
			final PersistenceRefactoringMapping refactoringMapping = this.refactoringMapping;
			for(final PersistenceRefactoringMemberIdentifierBuilder idBuilder : this.sourceMemberIdentifierBuilders)
			{
				final String                   identifier = idBuilder.buildMemberIdentifier(sourceType, sourceMember);
				final KeyValue<String, String> entry      = refactoringMapping.lookup(identifier);
				if(entry == null)
				{
					continue;
				}
				
				return this.resolveTarget(sourceType, sourceMember, targetType, entry.value());
			}

			// no refacting entry could be found
			return null;
		}
		
		private KeyValue<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> resolveTarget(
			final PersistenceTypeDefinition       sourceType            ,
			final PersistenceTypeDefinitionMember sourceMember          ,
			final PersistenceTypeDefinition       targetType            ,
			final String                          targetMemberIdentifier
		)
		{
			if(targetMemberIdentifier == null)
			{
				// indicated deletion
				return X.KeyValue(sourceMember, null);
			}
			
			for(final PersistenceTypeDefinitionMember targetMember : targetType.members())
			{
				for(final PersistenceRefactoringMemberIdentifierBuilder idBuilder : this.targetMemberIdentifierBuilders)
				{
					final String identifier = idBuilder.buildMemberIdentifier(targetType, targetMember);
					if(identifier.equals(targetMemberIdentifier))
					{
						return X.KeyValue(sourceMember, targetMember);
					}
				}
			}
			
			// if a target member mapping was found but cannot be resolved, something is wrong.
			// (05.10.2018 TM)EXCP: proper exception
			throw new RuntimeException(
				"Unresolvable type member refactoring mapping: "
				+ sourceType.toTypeIdentifier() + '#' + sourceMember.uniqueName()
				+ " -> \"" + targetMemberIdentifier + "\" in type "
				+ targetType.toRuntimeTypeIdentifier()
			);
		}
				
	}
	
}
