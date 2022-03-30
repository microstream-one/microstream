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

import static one.microstream.X.notNull;

import java.util.Objects;

import one.microstream.persistence.exceptions.PersistenceException;

public interface PersistenceTypeDescriptionMemberEnumConstant extends PersistenceTypeDescriptionMember
{
	@Override
	public default boolean isInstanceMember()
	{
		return false;
	}
	
	@Override
	public default boolean equalsStructure(final PersistenceTypeDescriptionMember other)
	{
		return other instanceof PersistenceTypeDescriptionMemberEnumConstant
			&& equalName(this, (PersistenceTypeDescriptionMemberEnumConstant)other)
		;
	}
	
	@Override
	public default boolean equalsDescription(final PersistenceTypeDescriptionMember member)
	{
		return this.equalsStructure(member);
	}
	
	public static boolean equalName(
		final PersistenceTypeDescriptionMemberEnumConstant m1,
		final PersistenceTypeDescriptionMemberEnumConstant m2
	)
	{
		// attribute checks must be null-safe equals because of primitive definitions
		return m1 == m2 || m2 != null
			&& Objects.equals(m1.name(), m2.name())
		;
	}
	
	@Override
	public default PersistenceTypeDefinitionMemberEnumConstant createDefinitionMember(
		final PersistenceTypeDefinitionMemberCreator creator
	)
	{
		return creator.createDefinitionMember(this);
	}

	
	public static PersistenceTypeDescriptionMemberEnumConstant New(
		final String enumPersistentName
	)
	{
		return new PersistenceTypeDescriptionMemberEnumConstant.Default(
			 notNull(enumPersistentName)
		);
	}

	public class Default
	implements PersistenceTypeDescriptionMemberEnumConstant
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final String enumName;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default(final String enumName)
		{
			super();
			this.enumName = notNull(enumName);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public String name()
		{
			return this.enumName;
		}

		@Override
		public void assembleTypeDescription(final PersistenceTypeDescriptionMemberAppender assembler)
		{
			assembler.appendTypeMemberDescription(this);
		}
		
		@Override
		public long persistentMinimumLength()
		{
			// does not enlarge an instance of the described type. It's only a constant field name validation vehicle.
			return 0L;
		}
		
		@Override
		public long persistentMaximumLength()
		{
			// does not enlarge an instance of the described type. It's only a constant field name validation vehicle.
			return 0L;
		}

		@Override
		public boolean isValidPersistentLength(final long persistentLength)
		{
			return persistentLength == this.persistentMinimumLength();
		}
		
		@Override
		public final boolean isInstanceMember()
		{
			return false;
		}

		@Override
		public String typeName()
		{
			return PersistenceTypeDictionary.Symbols.KEYWORD_ENUM;
		}
		
		@Override
		public String qualifier()
		{
			return null;
		}
		
		@Override
		public String identifier()
		{
			return this.name();
		}

		@Override
		public final boolean isReference()
		{
			return false;
		}

		@Override
		public final boolean isPrimitive()
		{
			return false;
		}

		@Override
		public final boolean isPrimitiveDefinition()
		{
			return false;
		}
		
		@Override
		public final boolean isEnumConstant()
		{
			return true;
		}

		@Override
		public final boolean hasReferences()
		{
			return false;
		}

		@Override
		public void validatePersistentLength(final long persistentLength)
		{
			if(this.isValidPersistentLength(persistentLength))
			{
				return;
			}
			throw new PersistenceException(
				"Invalid persistent length: " + persistentLength
				+ " != " + this.persistentMinimumLength() + "."
			);
		}
		

		
		@Override
		public String toString()
		{
			return "enum " + this.name();
		}

	}

}
