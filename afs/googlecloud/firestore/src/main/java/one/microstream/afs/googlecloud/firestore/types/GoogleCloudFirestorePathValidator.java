package one.microstream.afs.googlecloud.firestore.types;

/*-
 * #%L
 * microstream-afs-googlecloud-firestore
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

import java.util.regex.Pattern;

import one.microstream.afs.blobstore.types.BlobStorePath;

public interface GoogleCloudFirestorePathValidator extends BlobStorePath.Validator
{

	public static GoogleCloudFirestorePathValidator New()
	{
		return new GoogleCloudFirestorePathValidator.Default();
	}


	public static class Default implements GoogleCloudFirestorePathValidator
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
		 * https://firebase.google.com/docs/firestore/quotas#collections_documents_and_fields
		 */
		void validateCollectionName(
			final String collectionName
		)
		{
			if(collectionName.indexOf('/') != -1)
			{
				throw new IllegalArgumentException(
					"collection name cannot contain a forward slash (/)"
				);
			}
			if(collectionName.equals(".")
			|| collectionName.equals(".."))
			{
				throw new IllegalArgumentException(
					"collection name cannot solely consist of a single period (.) or double periods (..)"
				);
			}
			if(Pattern.matches(
				"__.*__",
				collectionName
			))
			{
				throw new IllegalArgumentException(
					"collection name cannot match the regular expression __.*__"
				);
			}
		}

	}

}
