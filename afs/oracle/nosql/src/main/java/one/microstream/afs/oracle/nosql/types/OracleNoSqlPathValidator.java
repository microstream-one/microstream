package one.microstream.afs.oracle.nosql.types;

/*-
 * #%L
 * microstream-afs-oracle-nosql
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

import java.util.regex.Pattern;

import one.microstream.afs.blobstore.types.BlobStorePath;

public interface OracleNoSqlPathValidator extends BlobStorePath.Validator
{

	public static OracleNoSqlPathValidator New()
	{
		return new OracleNoSqlPathValidator.Default();
	}


	public static class Default implements OracleNoSqlPathValidator
	{
		Default()
		{
			super();
		}

		@Override
		public void validate(
			final BlobStorePath path
		)
		{
			this.validateTableName(path.container());

		}

		/*
		 * https://docs.oracle.com/en/database/other-databases/nosql-database/20.1/java-driver-table/name-constraints.html
		 */
		void validateTableName(
			final String tableName
		)
		{
			final int length = tableName.length();
			if(length > 32
			)
			{
				throw new IllegalArgumentException(
					"table name cannot be longer than 32 characters"
				);
			}
			if(!Pattern.matches(
				"[a-zA-Z0-9_\\.]*",
				tableName
			))
			{
				throw new IllegalArgumentException(
					"table name can contain letters, numbers, periods (.) and underscores (_)"
				);
			}
			if(!Pattern.matches(
				"[a-zA-Z]",
				tableName.substring(0, 1)
			))
			{
				throw new IllegalArgumentException(
					"table names must start with a letter"
				);
			}
		}

	}

}
