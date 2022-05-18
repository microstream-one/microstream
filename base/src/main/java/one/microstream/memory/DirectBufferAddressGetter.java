package one.microstream.memory;

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

import java.nio.ByteBuffer;

/**
 * Similar to {@link DirectBufferDeallocator} but to obtain the DirectBuffer's address value.
 * 
 * 
 */
public interface DirectBufferAddressGetter
{
	public long getDirectBufferAddress(ByteBuffer directBuffer);
		
	
	
//	public final class Java8Makeshift implements DirectBufferAddressGetter
//	{
//		public Java8Makeshift()
//		{
//			super();
//		}
//
//		@Override
//		public long getDirectBufferAddress(final ByteBuffer directBuffer)
//		{
//			return ((sun.nio.ch.DirectBuffer)directBuffer).address();
//		}
//
//	}
	
}
