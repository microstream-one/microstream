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

import java.nio.ByteBuffer;

import one.microstream.X;
import one.microstream.memory.XMemory;


public interface StorageImportSourceByteBuffer extends StorageImportSource
{
	public static class Default
	extends    StorageImportSource.Abstract
	implements StorageImportSourceByteBuffer
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final int        channelIndex;
		private final ByteBuffer buffer      ;
	    
		              
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		              
	    Default(
			final int                               channelIndex,
			final ByteBuffer                        buffer      ,
			final StorageChannelImportBatch.Default headBatch
		)
		{
	    	super(headBatch);
			this.channelIndex = channelIndex;
			this.buffer       = buffer      ;
		}
	    
	    
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
	    
	    @Override
	    public long copyTo(final StorageFile target, final long sourcePosition, final long length)
	    {
	    	return target.writeBytes(
	    		X.ArrayView(XMemory.slice(this.buffer, sourcePosition, length))
	    	);
	    }
	    
	    @Override
	    public boolean close()
	    {
	    	// no-op
	    	return true;
	    }

		@Override
		public String toString()
		{
			return Integer.toString(this.channelIndex) + " "
				+ (this.buffer == null ? "<Dummy>"  : this.buffer + " " + this.headBatch)
			;
		}
		
	}

}
