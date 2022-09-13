package one.microstream.afs.oraclecloud.objectstorage.types;

/*-
 * #%L
 * microstream-afs-oraclecloud-objectstorage
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

public interface OracleCloudObjectStoragePathValidator extends BlobStorePath.Validator
{

	public static OracleCloudObjectStoragePathValidator New()
	{
		return new OracleCloudObjectStoragePathValidator.Default();
	}


	public static class Default implements OracleCloudObjectStoragePathValidator
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
			this.validateBucketName(path.container());

		}

		/*
		 * No documentation found for bucket naming limitations.
		 * This is just the check taken from the console web interface.
		 */
		void validateBucketName(
			final String bucketName
		)
		{
			if(!Pattern.matches(
				"[a-zA-Z0-9_\\-]*",
				bucketName
			))
			{
				throw new IllegalArgumentException(
					"bucket name can contain only letters, numbers, underscores (_) and dashes (-)"
				);
			}
		}

	}

}
