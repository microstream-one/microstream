package one.microstream.persistence.binary.types;

/*-
 * #%L
 * microstream-persistence-binary
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

import one.microstream.typing.TypeMapping;


/**
 * Since the value translator lookup might potentially get rather giant in the future, it is wrapped in a trivial
 * on-demand provider to ensure it is really only created (and held in memory forever) if necessary.
 *
 */
public interface BinaryValueTranslatorMappingProvider extends BinaryValueTranslatorLookupProvider
{
	@Override
	public TypeMapping<BinaryValueSetter> mapping(boolean switchByteOrder);
	
	
	
	public static BinaryValueTranslatorMappingProvider New()
	{
		return new BinaryValueTranslatorMappingProvider.Default();
	}
	
	public final class Default implements BinaryValueTranslatorMappingProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private transient TypeMapping<BinaryValueSetter> typeMapping;

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public synchronized TypeMapping<BinaryValueSetter> mapping(final boolean switchByteOrder)
		{
			if(this.typeMapping == null)
			{
				this.typeMapping = BinaryValueTranslators.createDefaultValueTranslators(switchByteOrder);
			}

			return this.typeMapping;
		}
		
	}
	
}
