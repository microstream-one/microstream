
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

/**
 * 
 * 
 */
public interface MemoryStatistics
{
	/**
	 * Returns the maximum amount of memory in bytes that can be
	 * used for memory management.  This method returns <code>-1</code>
	 * if the maximum memory size is undefined.
	 *
	 * <p> This amount of memory is not guaranteed to be available
	 * for memory management if it is greater than the amount of
	 * committed memory.  The Java virtual machine may fail to allocate
	 * memory even if the amount of used memory does not exceed this
	 * maximum size.
	 *
	 * @return the maximum amount of memory in bytes;
	 * <code>-1</code> if undefined.
	 */
	public long max();
	
	/**
	 * Returns the amount of memory in bytes that is committed for
	 * the Java virtual machine to use.  This amount of memory is
	 * guaranteed for the Java virtual machine to use.
	 *
	 * @return the amount of committed memory in bytes.
	 *
	 */
	public long committed();
	
	/**
	 * Returns the amount of used memory in bytes.
	 *
	 * @return the amount of used memory in bytes.
	 *
	 */
	public long used();
		
	
	public static MemoryStatistics New(
		final long   max      ,
		final long   committed,
		final long   used
	)
	{
		return new Default(max, committed, used);
	}
	
	public static class Default implements MemoryStatistics
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final long   max      ;
		private final long   committed;
		private final long   used     ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final long   max      ,
			final long   committed,
			final long   used
		)
		{
			super();
			
			this.max       = max      ;
			this.committed = committed;
			this.used      = used     ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public long max()
		{
			return this.max;
		}
		
		@Override
		public long committed()
		{
			return this.committed;
		}
		
		@Override
		public long used()
		{
			return this.used;
		}
		
	}
	
}
