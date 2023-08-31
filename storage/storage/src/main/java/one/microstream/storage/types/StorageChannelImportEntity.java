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

public interface StorageChannelImportEntity
{
	public int length();

	public StorageEntityType.Default type();

	public long objectId();
	
	public StorageChannelImportEntity next();
	
	
	
	public static class Default implements StorageChannelImportEntity
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final int                                length  ;
		final long                               objectId;
		final StorageEntityType.Default          type    ;
		      StorageChannelImportEntity.Default next    ;
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final int                       length  ,
			final long                      objectId,
			final StorageEntityType.Default type
		)
		{
			super();
			this.length   = length  ;
			this.objectId = objectId;
			this.type     = type    ;
		}
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final int length()
		{
			return this.length;
		}
		
		@Override
		public final StorageEntityType.Default type()
		{
			return this.type;
		}
		
		@Override
		public final long objectId()
		{
			return this.objectId;
		}
		
		@Override
		public final StorageChannelImportEntity.Default next()
		{
			return this.next;
		}
		
	}
	
}
