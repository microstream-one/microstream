package one.microstream.afs.sql.types;

/*-
 * #%L
 * MicroStream Abstract File System - SQL
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
 * Configure the separator character used by the microstream SQL AFS implementation.
 * Classes that implement this interface must provide the character as sting and as char.
 * The supplied character is used to separate path components and file names when mapping
 * directory structures to SQL-Table names.
 * Therefore the supplied character must be allowed to be part of table names of the used
 * SQL database the AFS is working with.
 * <p>
 * The configuration has to be done before any SQLFileSystem instance is created!
 * <p>
 * The configuration is done static via the {@link SqlPath} class.
 * <p>
 * E.g. to configure "_" as separator:
 * <blockquote><pre>
 * SqlPath.set(SqlPathSeparatorProvider.New("_", '_'));
 * </pre></blockquote>
 */
public interface SqlPathSeparatorProvider
{
	final static String DIRECTORY_TABLE_NAME_SEPARATOR_DEFAULT      = "$";
	final static char   DIRECTORY_TABLE_NAME_SEPARATOR_DEFAULT_CHAR = '$';
	
	public char getSqlPathSeparatorChar();
	
	public String getSqlPathSeparator();
	
	/**
	 * Create a Default instance that provides the default
	 * separator '$'.
	 * 
	 * @return an instance of SqlPathSeparatorProvider.Default.
	 */
	public static Default New()
	{
		return new Default(
			DIRECTORY_TABLE_NAME_SEPARATOR_DEFAULT,
			DIRECTORY_TABLE_NAME_SEPARATOR_DEFAULT_CHAR
		);
	}
	
	/**
	 * Create a Default instance that provides the configured
	 * separator.
	 * The both parameters must configure the same character as String and as char.
	 * 
	 * @param pathSeparator the separator as String
	 * @param pathSeparatorChar the separator as char
	 * @return an instance of SqlPathSeparatorProvider.Default.
	 */
	public static Default New(final String pathSeparator, final char pathSeparatorChar)
	{
		return new Default(
			pathSeparator,
			pathSeparatorChar
		);
	}
		
	public static class Default implements SqlPathSeparatorProvider
	{
		String pathSeparator;
		char   pathSeparatorChar;
		
		Default(final String pathSeparator, final char pathSeparatorChar)
		{
			this.pathSeparator     = pathSeparator;
			this.pathSeparatorChar = pathSeparatorChar;
		}
				
		@Override
		public char getSqlPathSeparatorChar()
		{
			return this.pathSeparatorChar;
		}

		@Override
		public String getSqlPathSeparator()
		{
			return this.pathSeparator;
		}

		
	}
}
