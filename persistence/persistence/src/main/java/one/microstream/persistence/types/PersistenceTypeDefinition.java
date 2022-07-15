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

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;

import one.microstream.X;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.collections.types.XImmutableEnum;

public interface PersistenceTypeDefinition extends PersistenceTypeDescription, PersistenceTypeLink
{
	/**
	 * The biuniquely associated id value identifying a type description.
	 */
	@Override
	public long typeId();
	
	/**
	 * The name of the type as defined in the type dictionary. This name may never change for a given typeId,
	 * even if the runtime {@link #runtimeTypeName()} did to reflect a design-level type renaming.
	 */
	@Override
	public String typeName();
	
	@Override
	public Class<?> type();
	
	/**
	 * The name of the corresponding runtime type.
	 * If not implemented otherwise (e.g. to cache the name), this method simply calls {@link Class#getName()} of
	 * a non-null {@link #type()} reference.
	 * @return the name of the corresponding runtime type
	 */
	public default String runtimeTypeName()
	{
		return this.type() == null
			? null
			: this.type().getName()
		;
	}

	/* (30.06.2015 TM)TODO: PersistenceTypeDescription <?>Generics
	 * Must consider Generics Type information as well, at least as a simple normalized String for
	 * equality comparison.
	 * Otherwise, changing type parameter won't be recognized by the type validation and
	 * loading/building of entities will result in heap pollution (wrong instance for the type).
	 * Example:
	 * Lazy<Person> changed to Lazy<Employee>.
	 * Currently, this is just recognized as Lazy.
	 * 
	 * (05.04.2017 TM)NOTE: but does it really have to be stored here?
	 * Wouldn't it be enough to store it in the member description?
	 * E.g. Type "Lazy" PLUS type parameter "[full qualified] Person"
	 */
	
	@Override
	public XGettingEnum<? extends PersistenceTypeDefinitionMember> allMembers();
	
	/**
	 * Enum (unique elements with order), using {@link PersistenceTypeDescriptionMember#identityHashEqualator()}.
	 * Contains all persistent members (similar, but not identical to fields) in persistent order, which can
	 * differ from the declaration order.
	 * 
	 */
	@Override
	public XGettingEnum<? extends PersistenceTypeDefinitionMember> instanceMembers();
	
	public boolean hasPersistedReferences();

	public long membersPersistedLengthMinimum();
	
	public long membersPersistedLengthMaximum();
	
	/**
	 * Provides information if two instances of the handled type can have different length in persisted form.<p>
	 * Examples for variable length types:
	 * <ul>
	 * <li> arrays</li>
	 * <li>{@code java.lang.String}</li>
	 * <li>{@code java.util.ArrayList}</li>
	 * <li>{@code java.math.BigDecimal}</li>
	 * </ul><p>
	 * Examples for fixed length types:
	 * <ul>
	 * <li>primitive value wrapper types</li>
	 * <li>{@code java.lang.Object}</li>
	 * <li>{@code java.util.Date}</li>
	 * <li>typical entity types (without unshared inlined variable length component instances)</li>
	 * </ul>
	 * 
	 * @return if two instances of the handled type can have different length in persisted form
	 */
	public default boolean hasPersistedVariableLength()
	{
		return this.membersPersistedLengthMinimum() != this.membersPersistedLengthMaximum();
	}

	public boolean isPrimitiveType();
	
	/**
	 * Provides information if one particular instance can have variing binary length from one store to another.<p>
	 * Examples for variable length instances:
	 * <ul>
	 * <li> variable size collection instances</li>
	 * <li> variable size pesudo collection instances like {@code java.util.StringBuilder}</li>
	 * <li> instances of custom defined types similar to collections</li>
	 * </ul><p>
	 * Examples for fixed length instances:
	 * <ul>
	 * <li>arrays</li>
	 * <li>all immutable type instances (like {@code java.lang.String} )</li>
	 * <li>all fixed length types (see {@link #hasVaryingPersistedLengthInstances()}</li>
	 * </ul>
	 * 
	 * @return if one particular instance can have variing binary length from one store to another
	 */
	public boolean hasVaryingPersistedLengthInstances();
	
	public default String toRuntimeTypeIdentifier()
	{
		return PersistenceTypeDescription.buildTypeIdentifier(
			this.typeId(),
			X.coalesce(this.runtimeTypeName(), "[no runtime type]")
		);
	}
	
	

	public static boolean determineVariableLength(
		final XGettingSequence<? extends PersistenceTypeDescriptionMember> members
	)
	{
		for(final PersistenceTypeDescriptionMember member : members)
		{
			if(member.isVariableLength())
			{
				return true;
			}
		}
		return false;
	}

	public static boolean determineIsPrimitive(
		final XGettingSequence<? extends PersistenceTypeDescriptionMember> allMembers
	)
	{
		return allMembers.size() == 1 && allMembers.get().isPrimitiveDefinition();
	}
	
	
	public static PersistenceTypeDefinition New(
		final long                                                    typeId         ,
		final String                                                  typeName       ,
		final String                                                  runtimeTypeName,
		final Class<?>                                                type           ,
		final XGettingEnum<? extends PersistenceTypeDefinitionMember> allMembers     ,
		final XGettingEnum<? extends PersistenceTypeDefinitionMember> instanceMembers
	)
	{
		// as defined by interface contract.
		if(allMembers.equality() != PersistenceTypeDescriptionMember.identityHashEqualator()
		|| instanceMembers.equality() != PersistenceTypeDescriptionMember.identityHashEqualator()
		)
		{
			throw new IllegalArgumentException();
		}
		
		// no-op for already immutable collection type (e.g. PersistenceTypeDescriptionMember#validateAndImmure)
		// type may be null for the sole case of an explicitly mapped to be deleted type.
		final XImmutableEnum<? extends PersistenceTypeDefinitionMember> immutAllMembers = allMembers.immure();
		final XImmutableEnum<? extends PersistenceTypeDefinitionMember> immutInsMembers = instanceMembers.immure();
		return new PersistenceTypeDefinition.Default(
			                                                         typeId          ,
			                                                 notNull(typeName)       ,
			                                                 mayNull(runtimeTypeName),
			                                                 mayNull(type)           ,
			                                                         immutAllMembers ,
			                                                         immutInsMembers ,
			PersistenceTypeDescriptionMember.determineHasReferences (immutInsMembers),
			PersistenceTypeDefinition       .determineIsPrimitive   (immutAllMembers),
			PersistenceTypeDefinition       .determineVariableLength(immutInsMembers)
		);
	}

	

	public final class Default implements PersistenceTypeDefinition
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final long                                                      typeId          ;
		final String                                                    typeName        ;
		final String                                                    runtimeTypeName ;
		final Class<?>                                                  runtimeType     ;
		final XImmutableEnum<? extends PersistenceTypeDefinitionMember> allMembers      ;
		final XImmutableEnum<? extends PersistenceTypeDefinitionMember> instanceMembers ;
		final long                                                      membersMinLength;
		final long                                                      membersMaxLength;
		final boolean                                                   hasReferences   ;
		final boolean                                                   isPrimitive     ;
		final boolean                                                   variableLength  ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final long                                                      typeId         ,
			final String                                                    typeName       ,
			final String                                                    runtimeTypeName,
			final Class<?>                                                  runtimeType    ,
			final XImmutableEnum<? extends PersistenceTypeDefinitionMember> allMembers     ,
			final XImmutableEnum<? extends PersistenceTypeDefinitionMember> instanceMembers,
			final boolean                                                   hasReferences  ,
			final boolean                                                   isPrimitive    ,
			final boolean                                                   variableLength
		)
		{
			super();
			this.typeId           = typeId         ;
			this.typeName         = typeName       ;
			this.runtimeTypeName  = runtimeTypeName;
			this.runtimeType      = runtimeType    ;
			this.allMembers       = allMembers     ;
			this.instanceMembers  = instanceMembers;
			this.hasReferences    = hasReferences  ;
			this.isPrimitive      = isPrimitive    ;
			this.variableLength   = variableLength ;
			this.membersMinLength = PersistenceTypeDescriptionMember.calculatePersistentMinimumLength(0, instanceMembers);
			this.membersMaxLength = PersistenceTypeDescriptionMember.calculatePersistentMaximumLength(0, instanceMembers);
		}

		

		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final long typeId()
		{
			return this.typeId;
		}
		
		@Override
		public final String typeName()
		{
			return this.typeName;
		}
		
		@Override
		public final String runtimeTypeName()
		{
			return this.runtimeTypeName;
		}
		
		@Override
		public final Class<?> type()
		{
			return this.runtimeType;
		}
		
		@Override
		public final XImmutableEnum<? extends PersistenceTypeDefinitionMember> allMembers()
		{
			return this.allMembers;
		}
		
		@Override
		public final XImmutableEnum<? extends PersistenceTypeDefinitionMember> instanceMembers()
		{
			return this.instanceMembers;
		}

		@Override
		public final boolean hasPersistedReferences()
		{
			return this.hasReferences;
		}

		@Override
		public final boolean isPrimitiveType()
		{
			return this.isPrimitive;
		}

		@Override
		public final boolean hasPersistedVariableLength()
		{
			return this.variableLength;
		}
		
		@Override
		public final boolean hasVaryingPersistedLengthInstances()
		{
			return this.variableLength;
		}
		
		@Override
		public final String toString()
		{
			return this.toRuntimeTypeIdentifier();
		}

		@Override
		public final long membersPersistedLengthMinimum()
		{
			return this.membersMinLength;
		}

		@Override
		public final long membersPersistedLengthMaximum()
		{
			return this.membersMaxLength;
		}
		

	}
	
}
