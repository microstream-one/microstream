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

import one.microstream.chars.VarString;

public interface PersistenceTypeDictionaryExporter
{
	public void exportTypeDictionary(PersistenceTypeDictionary typeDictionary);

	
	public static PersistenceTypeDictionaryExporter New(
		final PersistenceTypeDictionaryStorer storer
	)
	{
		return New(
			PersistenceTypeDictionaryAssembler.New(),
			storer
		);
	}
	
	public static PersistenceTypeDictionaryExporter New(
		final PersistenceTypeDictionaryAssembler assembler,
		final PersistenceTypeDictionaryStorer    storer
	)
	{
		return new PersistenceTypeDictionaryExporter.Default(
			notNull(assembler),
			notNull(storer)
		);
	}


	public final class Default implements PersistenceTypeDictionaryExporter
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final PersistenceTypeDictionaryAssembler assembler;
		private final PersistenceTypeDictionaryStorer    storer   ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final PersistenceTypeDictionaryAssembler assembler,
			final PersistenceTypeDictionaryStorer    storer
		)
		{
			super();
			this.assembler = assembler;
			this.storer    = storer   ;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final void exportTypeDictionary(final PersistenceTypeDictionary typeDictionary)
		{
			final String typeDictionaryString = this.assembler.assemble(
				VarString.New(),
				typeDictionary
			).toString();
			
			this.storer.storeTypeDictionary(typeDictionaryString);
		}

	}

}
