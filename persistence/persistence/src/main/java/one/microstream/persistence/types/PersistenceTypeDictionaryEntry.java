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

import one.microstream.chars.VarString;
import one.microstream.collections.types.XGettingSequence;

/**
 * Data that ties a {@link PersistenceTypeDescription} to a biunique type id, aka a {@link PersistenceTypeIdentity}.
 * 
 * 
 *
 */
public interface PersistenceTypeDictionaryEntry extends PersistenceTypeDescription
{
	@Override
	public long   typeId();

	@Override
	public String typeName();

	@Override
	public XGettingSequence<? extends PersistenceTypeDescriptionMember> instanceMembers();

	
	
	public abstract class Abstract implements PersistenceTypeDictionaryEntry
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Abstract()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public String toString()
		{
			return PersistenceTypeDictionaryAssembler.New()
				.assembleTypeDescription(VarString.New(), this)
				.toString()
			;
		}

	}
	
}
