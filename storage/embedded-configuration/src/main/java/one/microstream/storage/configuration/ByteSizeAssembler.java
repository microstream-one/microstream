package one.microstream.storage.configuration;

/*-
 * #%L
 * microstream-storage-embedded-configuration
 * %%
 * Copyright (C) 2019 - 2021 MicroStream Software
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
import one.microstream.configuration.types.ByteUnit;

/**
 * 
 * @deprecated will be removed in a future release
 * @see one.microstream.storage.configuration
 */
@Deprecated
public interface ByteSizeAssembler extends ObjectStringAssembler<Long>
{
	@Override
	public VarString assemble(VarString vs, Long byteSize);

	@Override
	public default String assemble(final Long byteSize)
	{
		return ObjectStringAssembler.super.assemble(byteSize);
	}


	public static ByteSizeAssembler Default()
	{
		return new ByteSizeAssembler.Default();
	}


	public static class Default implements ByteSizeAssembler
	{
		Default()
		{
			super();
		}

		@Override
		public VarString assemble(final VarString vs, final Long byteSize)
		{
			final ByteUnit[] byteMultiples =
			{
				ByteUnit.KB,
				ByteUnit.MB,
				ByteUnit.GB,
				ByteUnit.TB,
				ByteUnit.PB,
				ByteUnit.EB,
				ByteUnit.ZB,
				ByteUnit.YB
			};

			for(final ByteUnit byteMultiple : byteMultiples)
			{
				final double value     = ByteUnit.convert(byteSize, ByteUnit.B).to(byteMultiple);
				final long   longValue = (long)value;
				if(longValue > 0 && longValue < 1000)
				{
					if(longValue == value)
					{
						vs.add(longValue);
					}
					else
					{
						vs.add(value);
					}
					return vs.add(byteMultiple.name().toLowerCase());
				}
			}

			return vs.add(byteSize.longValue());
		}

	}

}
