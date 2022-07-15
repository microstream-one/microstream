package one.microstream.storage.restclient.jersey.types;

/*-
 * #%L
 * microstream-storage-restclient-jersey
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

public interface Routes
{
	public String dictionary();
	
	public String root();
	
	public String object();
	
	public String filesStatistics();
	
	
	public static Routes Default()
	{
		return new Default(
			"dictionary",
			"root",
			"object",
			"maintenance/filesStatistics"
		);
	}	
	
	public static Routes New(
		final String dictionary, 
		final String root, 
		final String object, 
		final String filesStatistics
	)
	{
		return new Default(
			dictionary, 
			root, 
			object, 
			filesStatistics
		);	
	}
	
	
	public static class Default implements Routes
	{
		private final String dictionary;		
		private final String root;		
		private final String object;		
		private final String filesStatistics;
		
		Default(
			final String dictionary, 
			final String root, 
			final String object, 
			final String filesStatistics
		)
		{
			super();
			this.dictionary      = dictionary;
			this.root            = root;
			this.object          = object;
			this.filesStatistics = filesStatistics;
		}

		@Override
		public String dictionary()
		{
			return this.dictionary;
		}

		@Override
		public String root()
		{
			return this.root;
		}

		@Override
		public String object()
		{
			return this.object;
		}

		@Override
		public String filesStatistics()
		{
			return this.filesStatistics;
		}
		
	}
	
}
