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
import one.microstream.chars.VarString;
import one.microstream.collections.HashEnum;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.util.cql.CQL;

/**
 * Data that describes the persistence-relevant aspects of a type, meaning its full type name and all its
 * persistable members (fields).
 * 
 * 
 *
 */
public interface PersistenceTypeDescription extends PersistenceTypeIdentity
{
	@Override
	public String typeName();
	
	public XGettingSequence<? extends PersistenceTypeDescriptionMember> allMembers();
	
	public XGettingSequence<? extends PersistenceTypeDescriptionMember> instanceMembers();
	
	public default XGettingSequence<? extends PersistenceTypeDescriptionMember> instanceReferenceMembers()
	{
		return CQL
			.<PersistenceTypeDescriptionMember>from(this.instanceMembers())
			.select(m -> m.isReference())
			.executeInto(HashEnum.<PersistenceTypeDescriptionMember>New())
		;
	}
	
	public default XGettingSequence<? extends PersistenceTypeDescriptionMember> instancePrimitiveMembers()
	{
		return CQL
			.<PersistenceTypeDescriptionMember>from(this.instanceMembers())
			.select(m -> m.isPrimitive())
			.executeInto(HashEnum.<PersistenceTypeDescriptionMember>New())
		;
	}
	
	/* (30.06.2015 TM)TODO: PersistenceTypeDescription Generics
	 * Must consider Generics Type information as well, at least as a simple normalized String for
	 * equality comparison.
	 * Otherwise, changing type parameter won't be recognized by the type validation and
	 * loading/building of entities will result in heap pollution (wrong instance for the type).
	 * Example:
	 * Lazy<Person> changed to Lazy<Employee>.
	 * Currently, this is just recognized as Lazy.
	 * 
	 * (13.09.2018 TM)NOTE: both here and in the member description
	 */
	
	
	public static char typeIdentifierSeparator()
	{
		return ':';
	}
	
	public static String buildTypeIdentifier(final long typeId, final String typeName)
	{
		// simple string concatenation syntax messes up the char adding.
		return VarString.New(100).add(typeId).add(typeIdentifierSeparator()).add(typeName).toString();
	}
	
	public static String buildTypeIdentifier(final PersistenceTypeDescription typeDescription)
	{
		return buildTypeIdentifier(typeDescription.typeId(), typeDescription.typeName());
	}
	
	public default String toTypeIdentifier()
	{
		return buildTypeIdentifier(this);
	}
	
	/**
	 * Equal content description, without TypeId comparison
	 * 
	 * @param td1 the first description
	 * @param td2 the second description
	 * @return if both descriptions are equal
	 */
	public static boolean equalDescription(
		final PersistenceTypeDescription td1,
		final PersistenceTypeDescription td2
	)
	{
		return td1 == td2 || td1 != null && td2 != null
			&& td1.typeName().equals(td1.typeName())
			&& PersistenceTypeDescriptionMember.equalDescriptions(td1.allMembers(), td2.allMembers())
		;
	}
	
	/**
	 * Equal structure, regardless of the member's definition type (reflective or custom-defined)
	 * 
	 * @param td1 the first description
	 * @param td2 the second description
	 * @return if both descriptions' structures are equal
	 */
	public static boolean equalStructure(
		final PersistenceTypeDescription td1,
		final PersistenceTypeDescription td2
	)
	{
		return td1 == td2 || td1 != null && td2 != null
			&& td1.typeName().equals(td1.typeName())
			&& PersistenceTypeDescriptionMember.equalStructures(td1.allMembers(), td2.allMembers())
		;
	}
	
	
	public static PersistenceTypeDescription Identity(final long typeId, final String typeName)
	{
		return new PersistenceTypeDescription.Identity(typeId, typeName);
	}
	
	public final class Identity implements PersistenceTypeDescription
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final long   typeId  ;
		private final String typeName;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Identity(final long typeId, final String typeName)
		{
			super();
			this.typeId   = typeId  ;
			this.typeName = typeName;
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
		public final XGettingSequence<? extends PersistenceTypeDescriptionMember> allMembers()
		{
			return X.empty();
		}

		@Override
		public final XGettingSequence<? extends PersistenceTypeDescriptionMember> instanceMembers()
		{
			return X.empty();
		}
		
	}
	
}
