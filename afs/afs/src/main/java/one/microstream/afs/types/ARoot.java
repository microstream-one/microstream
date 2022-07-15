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

import static one.microstream.X.coalesce;
import static one.microstream.X.notNull;

public interface ARoot extends ADirectory
{
	/**
	 * E.g.
	 * https://
	 * file://
	 * 
	 * @return the protocol
	 */
	public String protocol();
	
	
	
	@FunctionalInterface
	public interface Creator
	{
		public ARoot createRootDirectory(AFileSystem fileSystem, String protocol, String identifier);
		
		public default ARoot createRootDirectory(final AFileSystem fileSystem, final String identifier)
		{
			return this.createRootDirectory(
				fileSystem,
				coalesce(this.protocol(), fileSystem.defaultProtocol()),
				identifier
			);
		}
		
		public default String protocol()
		{
			return null;
		}
	}
	
	
	/**
	 * Creates a new root directory
	 * Note: {@code identifier} can be {@literal ""} since local file paths might start with a "/".
	 * @param fileSystem the root's file system
	 * @param protocol the used protocol
	 * @param identifier the identifier of the root directory
	 * @return the newly created root directory
	 * 
	 */
	public static ARoot New(
		final AFileSystem fileSystem,
		final String      protocol  ,
		final String      identifier
	)
	{
		return new ARoot.Default(
			notNull(fileSystem),
			notNull(protocol)  ,
			notNull(identifier) // may be ""
		);
	}
	
	public final class Default extends ADirectory.Abstract implements ARoot
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final AFileSystem fileSystem;
		private final String      protocol  ;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Default(
			final AFileSystem fileSystem,
			final String      protocol  ,
			final String      identifier
		)
		{
			super(identifier);
			this.protocol   = protocol  ;
			this.fileSystem = fileSystem;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final AFileSystem fileSystem()
		{
			return this.fileSystem;
		}
		
		@Override
		public final String protocol()
		{
			return this.protocol;
		}
		
		@Override
		public final ADirectory parent()
		{
			return null;
		}
		
	}
	
}
