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

import one.microstream.persistence.exceptions.PersistenceException;

public interface PersistenceTypeDescriptionMemberField extends PersistenceTypeDescriptionMember
{
	@Override
	public String typeName();
	
	/**
	 * A type-internal qualifier to distinct different members with equal "primary" name. E.g. reflection-based
	 * type handling where fields names are only unique in combination with their declaring class.
	 * <p>
	 * May never be {@code null}.
	 * 
	 * @return the member's qualifier string to ensure a unique {@link #identifier()} in a group of member fields.
	 */
	@Override
	public String qualifier();

	/**
	 * The simple or "primary" name of the member. E.g. "lastName".
	 * <p>
	 * May never be {@code null}.
	 * 
	 * @return the member field's simple name.
	 */
	@Override
	public String name();
	

	@Override
	public default boolean isInstanceMember()
	{
		return true;
	}
			
	
	
	public abstract class Abstract
//	extends PersistenceTypeDescriptionMember.Abstract
	implements PersistenceTypeDescriptionMemberField
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final String   typeName               ;
		private final String   qualifier              ;
		private final String   name                   ;
		private final boolean  isReference            ;
		private final boolean  isPrimitive            ;
		private final boolean  hasReferences          ;
		private final long     persistentMinimumLength;
		private final long     persistentMaximumLength;
		private final String   qualifiedFieldName     ;


		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Abstract(
			final String   typeName               ,
			final String   qualifier              ,
			final String   name                   ,
			final boolean  isReference            ,
			final boolean  isPrimitive            ,
			final boolean  hasReferences          ,
			final long     persistentMinimumLength,
			final long     persistentMaximumLength
		)
		{
			super();
			this.typeName                = typeName               ;
			this.qualifier               = qualifier              ;
			this.name                    = name                   ;
			this.isReference             = isReference            ;
			this.isPrimitive             = isPrimitive            ;
			this.hasReferences           = hasReferences          ;
			this.persistentMinimumLength = persistentMinimumLength;
			this.persistentMaximumLength = persistentMaximumLength;
			this.qualifiedFieldName      = PersistenceTypeDictionary.fullQualifiedFieldName(qualifier, name);
		}




		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final String typeName()
		{
			return this.typeName;
		}
		
		@Override
		public final String qualifier()
		{
			return this.qualifier;
		}

		@Override
		public final String name()
		{
			return this.name;
		}
		
		@Override
		public final String identifier()
		{
			return this.qualifiedFieldName;
		}

		@Override
		public final boolean isReference()
		{
			return this.isReference;
		}

		@Override
		public final boolean isPrimitive()
		{
			return this.isPrimitive;
		}

		@Override
		public final boolean isPrimitiveDefinition()
		{
			return false;
		}
		
		@Override
		public final boolean isEnumConstant()
		{
			return false;
		}

		@Override
		public boolean hasReferences()
		{
			return this.hasReferences;
		}

		@Override
		public long persistentMinimumLength()
		{
			return this.persistentMinimumLength;
		}

		@Override
		public long persistentMaximumLength()
		{
			return this.persistentMaximumLength;
		}

		@Override
		public boolean isValidPersistentLength(final long persistentLength)
		{
			return persistentLength >= this.persistentMinimumLength && persistentLength <= this.persistentMaximumLength;
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
				+ " not in [" + this.persistentMinimumLength + ";" + this.persistentMaximumLength + "]"
			);
		}
		
		@Override
		public final boolean isInstanceMember()
		{
			return true;
		}
		
		@Override
		public String toString()
		{
			return this.typeName() + ' ' + this.identifier();
		}

	}

}
