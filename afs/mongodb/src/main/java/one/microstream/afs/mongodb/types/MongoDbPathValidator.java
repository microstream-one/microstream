package one.microstream.afs.mongodb.types;

/*-
 * #%L
 * microstream-afs-mongodb
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

import one.microstream.afs.blobstore.types.BlobStorePath;

public interface MongoDbPathValidator extends BlobStorePath.Validator
{

	public static MongoDbPathValidator New()
	{
		return new MongoDbPathValidator.Default();
	}


	public static class Default implements MongoDbPathValidator
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
			this.validateCollectionName(path.container());

		}

		/*
		 * https://docs.mongodb.com/manual/reference/limits/#naming-restrictions
		 */
		void validateCollectionName(
			final String collectionName
		)
		{
			if(collectionName.contains("$"))
			{
				throw new IllegalArgumentException(
					"collection name cannot contain the dollar sign"
				);
			}
			if(collectionName.startsWith("system."))
			{
				throw new IllegalArgumentException(
					"collection name cannot begin with 'system.'"
				);
			}
		}

	}

}
