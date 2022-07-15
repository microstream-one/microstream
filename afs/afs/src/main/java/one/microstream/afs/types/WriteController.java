package one.microstream.afs.types;

/*-
 * #%L
 * microstream-afs
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

import one.microstream.afs.exceptions.AfsExceptionReadOnly;

@FunctionalInterface
public interface WriteController
{
	public default void validateIsWritable()
	{
		if(this.isWritable())
		{
			return;
		}
		
		throw new AfsExceptionReadOnly("Writing is not enabled.");
	}
	
	public boolean isWritable();
	
	
	
	public static WriteController Enabled()
	{
		// Singleton is (usually) an anti pattern.
		return new WriteController.Enabled();
	}
	
	public final class Enabled implements WriteController
	{
		Enabled()
		{
			super();
		}
		
		@Override
		public final void validateIsWritable()
		{
			// no-op
		}

		@Override
		public final boolean isWritable()
		{
			return true;
		}
		
	}
	
	public static WriteController Disabled()
	{
		// Singleton is (usually) an anti pattern.
		return new WriteController.Disabled();
	}
	
	public final class Disabled implements WriteController
	{
		Disabled()
		{
			super();
		}

		@Override
		public final boolean isWritable()
		{
			return false;
		}
		
	}
	
}
