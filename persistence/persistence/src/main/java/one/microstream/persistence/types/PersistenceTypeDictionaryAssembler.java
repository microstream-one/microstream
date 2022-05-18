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

import one.microstream.chars.ObjectStringAssembler;
import one.microstream.chars.VarString;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.persistence.internal.TypeDictionaryAppenderBuilder;

public interface PersistenceTypeDictionaryAssembler extends ObjectStringAssembler<PersistenceTypeDictionary>
{
	@Override
	public VarString assemble(VarString vc, PersistenceTypeDictionary typeDictionary);

	@Override
	public default String assemble(final PersistenceTypeDictionary typeDictionary)
	{
		return ObjectStringAssembler.super.assemble(typeDictionary);
	}
	

	public VarString assembleTypeDescription(VarString vc, PersistenceTypeDescription typeDescription);


	
	
	public static PersistenceTypeDictionaryAssembler New()
	{
		return new PersistenceTypeDictionaryAssembler.Default();
	}

	public class Default
	extends PersistenceTypeDictionary.Symbols
	implements PersistenceTypeDictionaryAssembler
	{
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////

		private static final int  MAX_LONG_LENGTH = 19 ;
		private static final char ID_PADDING_CHAR = '0';
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default()
		{
			super();
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		protected final VarString appendPaddedId(final VarString vc, final long id)
		{
			return vc.padLeft(Long.toString(id), MAX_LONG_LENGTH, ID_PADDING_CHAR);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public VarString assemble(final VarString vc, final PersistenceTypeDictionary typeDictionary)
		{
			for(final PersistenceTypeDefinition td : typeDictionary.allTypeDefinitions().values())
			{
				this.assembleTypeDescription(vc, td);
			}
			return vc;
		}

		private static TypeDictionaryAppenderBuilder appenderBuilder(final VarString vc)
		{
			return new TypeDictionaryAppenderBuilder(vc, 1);
		}

		private void appendTypeDictionaryMembers(
			final VarString vc,
			final XGettingSequence<? extends PersistenceTypeDescriptionMember> typeMembers
		)
		{
			if(typeMembers.isEmpty())
			{
				return;
			}
			
			final PersistenceTypeDescriptionMemberAppender appender = typeMembers.iterate(
				appenderBuilder(vc.lf())
			).yield();
			typeMembers.iterate(appender);
		}

		@Override
		public VarString assembleTypeDescription(
			final VarString                  vc             ,
			final PersistenceTypeDescription typeDescription
		)
		{
			this.appendTypeDefinitionStart  (vc, typeDescription);
			this.appendTypeDictionaryMembers(vc, typeDescription.allMembers());
			this.appendTypeDefinitionEnd    (vc, typeDescription);
			return vc;
		}

		protected void appendTypeDefinitionStart(
			final VarString                  vc             ,
			final PersistenceTypeDescription typeDescription
		)
		{
			this.appendPaddedId(vc, typeDescription.typeId())
				.blank().add(typeDescription.typeName())
				.append(TYPE_START)
			;
		}

		protected void appendTypeDefinitionEnd(
			final VarString                  vc             ,
			final PersistenceTypeDescription typeDescription
		)
		{
			vc.append(TYPE_END).lf();
		}

	}

}
