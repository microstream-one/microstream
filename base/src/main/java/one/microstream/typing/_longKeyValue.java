package one.microstream.typing;

/*-
 * #%L
 * microstream-base
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

public interface _longKeyValue
{
	public long key();
	public long value();



	public class Default implements _longKeyValue
	{
		private final long key;
		private final long value;

		public Default(final long key, final long value)
		{
			super();
			this.key = key;
			this.value = value;
		}

		@Override
		public long key()
		{
			return this.key;
		}

		@Override
		public long value()
		{
			return this.value;
		}

		@Override
		public String toString()
		{
			return "(" + this.key + " -> " + this.value + ")";
		}

	}

}
