package one.microstream.storage.types;

/*-
 * #%L
 * microstream-storage
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
import static one.microstream.math.XMath.notNegative;

import one.microstream.afs.types.AFile;


public interface StorageDataInventoryFile extends StorageDataFile
{
	@Override
	public AFile file();
	
	@Override
	public int channelIndex();
	
	@Override
	public long number();
	
	
	
	@FunctionalInterface
	public interface Creator extends StorageDataFile.Creator<StorageDataInventoryFile>
	{
		@Override
		public StorageDataInventoryFile createDataFile(AFile file, int channelIndex, long number);
	}
	
	
	public static StorageDataInventoryFile New(
		final AFile file        ,
		final int   channelIndex,
		final long  number
	)
	{
		return new StorageDataInventoryFile.Default(
			    notNull(file),
			notNegative(channelIndex),
			notNegative(number)
		);
	}
	
	public class Default extends StorageDataFile.Abstract implements StorageDataInventoryFile
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default(final AFile file, final int channelIndex, final long number)
		{
			super(file, channelIndex, number);
		}
				
	}
}
