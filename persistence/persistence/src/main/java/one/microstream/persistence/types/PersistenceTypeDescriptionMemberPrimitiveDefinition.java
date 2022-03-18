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
import static one.microstream.math.XMath.positive;

import java.util.Objects;

import one.microstream.persistence.exceptions.PersistenceException;

public interface PersistenceTypeDescriptionMemberPrimitiveDefinition extends PersistenceTypeDescriptionMember
{
	public String primitiveDefinition();
	
	@Override
	public default boolean isInstanceMember()
	{
		return false;
	}
	
	@Override
	public default String identifier()
	{
		return this.primitiveDefinition();
	}

	
	@Override
	public default boolean equalsDescription(final PersistenceTypeDescriptionMember member)
	{
		return member instanceof PersistenceTypeDescriptionMemberPrimitiveDefinition
			&& equalDescription(this, (PersistenceTypeDescriptionMemberPrimitiveDefinition)member)
		;
	}
	
	public static boolean equalDescription(
		final PersistenceTypeDescriptionMemberPrimitiveDefinition m1,
		final PersistenceTypeDescriptionMemberPrimitiveDefinition m2
	)
	{
		return m1.primitiveDefinition().equals(m2.primitiveDefinition());
	}
	
	@Override
	public default PersistenceTypeDefinitionMemberPrimitiveDefinition createDefinitionMember(
		final PersistenceTypeDefinitionMemberCreator creator
	)
	{
		return creator.createDefinitionMember(this);
	}

	
	public static PersistenceTypeDescriptionMemberPrimitiveDefinition New(
		final String primitiveDefinition,
		final long   persistentLength
	)
	{
		return new PersistenceTypeDefinitionMemberPrimitiveDefinition.Default(
			 notNull(primitiveDefinition),
			positive(persistentLength)
		);
	}

	public class Default
	implements PersistenceTypeDescriptionMemberPrimitiveDefinition
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final String primitiveDefinition;
		private final long   persistentLength   ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default(
			final String primitiveDefinition,
			final long   persistentLength
		)
		{
			super();
			this.primitiveDefinition =  notNull(primitiveDefinition);
			this.persistentLength    = positive(persistentLength)   ;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public String primitiveDefinition()
		{
			return this.primitiveDefinition;
		}

		@Override
		public void assembleTypeDescription(final PersistenceTypeDescriptionMemberAppender assembler)
		{
			assembler.appendTypeMemberDescription(this);
		}
		
		@Override
		public long persistentMinimumLength()
		{
			return this.persistentLength;
		}
		
		@Override
		public long persistentMaximumLength()
		{
			return this.persistentLength;
		}

		@Override
		public boolean isValidPersistentLength(final long persistentLength)
		{
			return persistentLength == this.persistentLength;
		}
		
		@Override
		public boolean equalsStructure(final PersistenceTypeDescriptionMember other)
		{
			// the check for equal (namely null) typename and name is still valid here.
			return PersistenceTypeDescriptionMemberPrimitiveDefinition.super.equalsStructure(other)
				&& other instanceof PersistenceTypeDescriptionMemberPrimitiveDefinition
				&& Objects.equals(
					this.primitiveDefinition(),
					((PersistenceTypeDescriptionMemberPrimitiveDefinition)other).primitiveDefinition()
				)
			;
		}

		@Override
		public String typeName()
		{
			return null;
		}
		
		@Override
		public String qualifier()
		{
			return null;
		}
		
		@Override
		public String name()
		{
			return null;
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
			return true;
		}
		
		@Override
		public final boolean isEnumConstant()
		{
			return false;
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
				+ " != " + this.persistentLength + "."
			);
		}
		
		@Override
		public final boolean isInstanceMember()
		{
			return false;
		}

	}

}
