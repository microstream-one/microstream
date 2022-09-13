package one.microstream.afs.hazelcast.types;

/*-
 * #%L
 * microstream-afs-hazelcast
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

import static one.microstream.chars.XChars.notEmpty;
import static one.microstream.math.XMath.notNegative;

public interface BlobMetadata
{
	public String key();

	public long size();


	public static BlobMetadata New(
		final String key ,
		final long   size
	)
	{
		return new BlobMetadata.Default(
			notEmpty   (key ),
			notNegative(size)
		);
	}


	public static class Default implements BlobMetadata
	{
		private final String key ;
		private final long   size;

		Default(
			final String key ,
			final long   size
		)
		{
			super();
			this.key  = key ;
			this.size = size;
		}

		@Override
		public String key()
		{
			return this.key;
		}

		@Override
		public long size()
		{
			return this.size;
		}

	}

}
