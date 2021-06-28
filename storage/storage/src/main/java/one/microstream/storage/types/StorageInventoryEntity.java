package one.microstream.storage.types;

/*-
 * #%L
 * microstream-storage
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

public interface StorageInventoryEntity
{
	public long position();

	public long length();

	public long typeId();

	public long objectId();

	public StorageInventoryEntity next();



	public final class Default implements StorageInventoryEntity
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final long position, length, typeId, objectId;
		private       StorageInventoryEntity.Default next;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Default(final long position, final long length, final long typeId, final long objectId)
		{
			super();
			this.position = position;
			this.length   = length  ;
			this.typeId   = typeId  ;
			this.objectId = objectId;
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		final void setNext(final StorageInventoryEntity.Default next)
		{
			this.next = next;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final long position()
		{
			return this.position;
		}

		@Override
		public final long length()
		{
			return this.length;
		}

		@Override
		public final long typeId()
		{
			return this.typeId;
		}

		@Override
		public final long objectId()
		{
			return this.objectId;
		}

		@Override
		public final StorageInventoryEntity.Default next()
		{
			return this.next;
		}

		@Override
		public final String toString()
		{
			return "Init Entity " + this.length + ", " + this.typeId + ", " + this.objectId + " @ " + this.position;
		}

	}

}
