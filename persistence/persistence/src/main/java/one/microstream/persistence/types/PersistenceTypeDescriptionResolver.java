package one.microstream.persistence.types;

/*-
 * #%L
 * microstream-persistence
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import one.microstream.X;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.collections.types.XImmutableEnum;
import one.microstream.persistence.exceptions.PersistenceException;
import one.microstream.typing.KeyValue;

/**
 * A mapping that projects outdated identifiers (usually className#fieldName, but in case of root instances
 * also potentially arbitrary strings) to current identifiers.
 * 
 * 
 *
 */
public interface PersistenceTypeDescriptionResolver extends PersistenceTypeResolver
{
	/**
	 * Returns a key-value pair with the passed source identifier as the key and a mapped target identifier
	 * as the value. The value can be potentially null to indicate deletion.
	 * If the lookup did not yield any result, {@code null} is returned.
	 * 
	 * @param sourceIdentifier the source identifier
	 * @return a key-value pair with the passed source identifier as the key and a mapped target identifier
	 * as the value
	 */
	public KeyValue<String, String> lookup(String sourceIdentifier);
	
	public String resolveRuntimeTypeName(PersistenceTypeDescription typeDescription);
	
	public default String resolveRuntimeTypeName(final String descriptionTypeName)
	{
		final KeyValue<String, String> entry = this.lookup(descriptionTypeName);
		
		if(entry == null)
		{
			// no mapping entry, return the descriptionTypeName itself
			return descriptionTypeName;
		}

		// can be null for types explicitly marked as no more having a runtime type (unreachable / "deleted")
		return entry.value();
	}
	

	
	public default Class<?> resolveRuntimeType(final PersistenceTypeDescription typeDescription)
	{
		final String runtimeTypeName = this.resolveRuntimeTypeName(typeDescription);
		return this.resolveType(runtimeTypeName);
	}
	
	public default Class<?> tryResolveRuntimeType(final PersistenceTypeDescription typeDescription)
	{
		final String runtimeTypeName = this.resolveRuntimeTypeName(typeDescription);
		return this.tryResolveType(runtimeTypeName);
	}
	
	/**
	 * Returns a key-value pair with the passed source member as the key and a mapped target member
	 * as the value. The value can be potentially null to indicate deletion.
	 * If the lookup did not yield any result, {@code null} is returned.
	 * 
	 * @param sourceType the source type
	 * @param sourceMember the source member
	 * @param targetType the target type
	 * @return a key-value pair with the passed source member as the key and a mapped target member
	 * as the value
	 */
	public KeyValue<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> resolveMember(
		PersistenceTypeDefinition       sourceType  ,
		PersistenceTypeDefinitionMember sourceMember,
		PersistenceTypeDefinition       targetType
	);
	
	public boolean isNewCurrentTypeMember(
		PersistenceTypeDefinition       currentTypeDefinition,
		PersistenceTypeDefinitionMember currentTypeMember
	);
	

		
	public static PersistenceTypeDescriptionResolver New(
		final PersistenceTypeResolver                                               typeResolver                  ,
		final PersistenceRefactoringMapping                                         refactoringMapping            ,
		final XGettingEnum<? extends PersistenceRefactoringTypeIdentifierBuilder>   sourceTypeIdentifierBuilders  ,
		final XGettingEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> sourceMemberIdentifierBuilders,
		final XGettingEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> targetMemberIdentifierBuilders
	)
	{
		return new Default(
			typeResolver                           ,
			refactoringMapping                     ,
			sourceTypeIdentifierBuilders  .immure(),
			sourceMemberIdentifierBuilders.immure(),
			targetMemberIdentifierBuilders.immure()
		);
	}
	
	public final class Default implements PersistenceTypeDescriptionResolver
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final PersistenceTypeResolver                                                 typeResolver                  ;
		final PersistenceRefactoringMapping                                           refactoringMapping            ;
		final XImmutableEnum<? extends PersistenceRefactoringTypeIdentifierBuilder>   sourceTypeIdentifierBuilders  ;
		final XImmutableEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> sourceMemberIdentifierBuilders;
		final XImmutableEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> targetMemberIdentifierBuilders;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final PersistenceTypeResolver                                                 typeResolver                  ,
			final PersistenceRefactoringMapping                                           refactoringMapping            ,
			final XImmutableEnum<? extends PersistenceRefactoringTypeIdentifierBuilder>   sourceTypeIdentifierBuilders  ,
			final XImmutableEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> sourceMemberIdentifierBuilders,
			final XImmutableEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> targetMemberIdentifierBuilders
		)
		{
			super();
			this.typeResolver                   = typeResolver                  ;
			this.refactoringMapping             = refactoringMapping            ;
			this.sourceTypeIdentifierBuilders   = sourceTypeIdentifierBuilders  ;
			this.sourceMemberIdentifierBuilders = sourceMemberIdentifierBuilders;
			this.targetMemberIdentifierBuilders = targetMemberIdentifierBuilders;
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public ClassLoader getTypeResolvingClassLoader(final String typeName)
		{
			return this.typeResolver.getTypeResolvingClassLoader(typeName);
		}
				
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
				
				// can be null for types explicitly marked as no more having a runtime type (unreachable / "deleted")
				return entry.value();
			}
			
			// if no refactoring entry could be found, the original type name still applies.
			return typeDescription.typeName();
		}

		@Override
		public final KeyValue<String, String> lookup(final String sourceIdentifier)
		{
			return this.refactoringMapping.lookup(sourceIdentifier);
		}
		
		@Override
		public boolean isNewCurrentTypeMember(
			final PersistenceTypeDefinition       currentTypeDefinition,
			final PersistenceTypeDefinitionMember currentTypeMember
		)
		{
			for(final PersistenceRefactoringMemberIdentifierBuilder idBuilder : this.targetMemberIdentifierBuilders)
			{
				final String identifier = idBuilder.buildMemberIdentifier(currentTypeDefinition, currentTypeMember);
				if(this.refactoringMapping.isNewElement(identifier))
				{
					return true;
				}
			}

			return false;
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
			
			for(final PersistenceTypeDefinitionMember targetMember : targetType.allMembers())
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
			throw new PersistenceException(
				"Unresolvable type member refactoring mapping: "
				+ sourceType.toTypeIdentifier() + '#' + sourceMember.identifier()
				+ " -> \"" + targetMemberIdentifier + "\" in type "
				+ targetType.toRuntimeTypeIdentifier()
			);
		}
				
	}
	
}
