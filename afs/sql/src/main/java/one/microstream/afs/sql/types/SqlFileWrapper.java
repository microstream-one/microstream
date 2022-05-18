package one.microstream.afs.sql.types;

/*-
 * #%L
 * microstream-afs-sql
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

import one.microstream.afs.exceptions.AfsExceptionRetiredFile;
import one.microstream.afs.types.AFile;
import one.microstream.chars.XChars;

public interface SqlFileWrapper extends AFile.Wrapper, SqlItemWrapper
{
	public boolean retire();

	public boolean isRetired();

	public boolean isHandleOpen();

	public boolean checkHandleOpen();

	public boolean openHandle();

	public boolean closeHandle();

	public SqlFileWrapper ensureOpenHandle();


	public abstract class Abstract<U> extends AFile.Wrapper.Abstract<U> implements SqlFileWrapper
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private SqlPath path              ;
		private boolean handleOpen = false;



        ///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

        protected Abstract(
			final AFile   actual ,
			final U       user   ,
			final SqlPath path
        )
		{
			super(actual, user);
			this.path = notNull(path);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public SqlPath path()
		{
			synchronized(this.mutex())
			{
				this.validateIsNotRetired();
				return this.path;
			}
		}

		@Override
		public boolean retire()
		{
			synchronized(this.mutex())
			{
				if(this.path == null)
				{
					return false;
				}
				
				this.path = null;
				
				return true;
			}
		}

		@Override
		public boolean isRetired()
		{
			synchronized(this.mutex())
			{
				return this.path == null;
			}
		}

		public void validateIsNotRetired()
		{
			if(!this.isRetired())
			{
				return;
			}

			throw new AfsExceptionRetiredFile(
				"File is retired: " + XChars.systemString(this) + "(\"" + this.toPathString() + "\"."
			);
		}

		@Override
		public boolean isHandleOpen()
		{
			synchronized(this.mutex())
			{
				return this.handleOpen;
			}
		}

		@Override
		public boolean checkHandleOpen()
		{
			synchronized(this.mutex())
			{
				this.validateIsNotRetired();
				return this.isHandleOpen();
			}
		}

		@Override
		public boolean openHandle()
		{
			synchronized(this.mutex())
			{
				if(this.checkHandleOpen())
				{
					return false;
				}
	
				this.handleOpen = true;
				return true;
			}
		}

		@Override
		public boolean closeHandle()
		{
			synchronized(this.mutex())
			{
				if(!this.isHandleOpen())
				{
					return false;
				}
	
				this.handleOpen = false;
	
				return true;
			}
		}

		@Override
		public SqlFileWrapper ensureOpenHandle()
		{
			synchronized(this.mutex())
			{
				this.validateIsNotRetired();
				this.openHandle();
	
				return this;
			}
		}

	}

}
