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
import static one.microstream.math.XMath.positive;

import one.microstream.collections.types.XGettingSequence;

public interface PersistenceTypeDefinitionMemberFieldGenericComplex
extends PersistenceTypeDefinitionMemberFieldGenericVariableLength, PersistenceTypeDescriptionMemberFieldGenericComplex
{
	@Override
	public default PersistenceTypeDefinitionMemberFieldGenericComplex copyForName(final String name)
	{
		return this.copyForName(this.qualifier(), name);
	}
	
	@Override
	public PersistenceTypeDefinitionMemberFieldGenericComplex copyForName(String qualifier, String name);
	
	
	
	public static PersistenceTypeDefinitionMemberFieldGenericComplex New(
		final PersistenceTypeDescriptionMemberFieldGenericComplex description
	)
	{
		return New(
			description.qualifier()              ,
			description.name()                   ,
			description.members()                ,
			description.persistentMinimumLength(),
			description.persistentMaximumLength()
		);
	}
	
	public static PersistenceTypeDefinitionMemberFieldGenericComplex New(
		final String                                                        name                   ,
		final XGettingSequence<PersistenceTypeDescriptionMemberFieldGeneric> members                ,
		final long                                                          persistentMinimumLength,
		final long                                                          persistentMaximumLength
	)
	{
		return New(null, name, members, persistentMinimumLength, persistentMaximumLength);
	}
	
	public static PersistenceTypeDefinitionMemberFieldGenericComplex New(
		final String                                                        qualifier              ,
		final String                                                        name                   ,
		final XGettingSequence<PersistenceTypeDescriptionMemberFieldGeneric> members                ,
		final long                                                          persistentMinimumLength,
		final long                                                          persistentMaximumLength
	)
	{
		return new PersistenceTypeDefinitionMemberFieldGenericComplex.Default(
			 mayNull(qualifier)              ,
			 notNull(name)                   ,
			 notNull(members)                ,
			positive(persistentMinimumLength),
			positive(persistentMaximumLength)
		);
	}
	
	public class Default
	extends PersistenceTypeDescriptionMemberFieldGenericComplex.Default
	implements PersistenceTypeDefinitionMemberFieldGenericComplex
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default(
			final String                                                        qualifier              ,
			final String                                                        name                   ,
			final XGettingSequence<PersistenceTypeDescriptionMemberFieldGeneric> members                ,
			final long                                                          persistentMinimumLength,
			final long                                                          persistentMaximumLength
		)
		{
			super(
				qualifier              ,
				name                   ,
				members                ,
				persistentMinimumLength,
				persistentMaximumLength
			);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
				
		@Override
		public final Class<?> type()
		{
			return null;
		}

		@Override
		public PersistenceTypeDefinitionMemberFieldGenericComplex copyForName(
			final String qualifier,
			final String name
		)
		{
			return new PersistenceTypeDefinitionMemberFieldGenericComplex.Default(
				qualifier,
				name,
				this.members(),
				this.persistentMinimumLength(),
				this.persistentMaximumLength()
			);
		}

	}

}
