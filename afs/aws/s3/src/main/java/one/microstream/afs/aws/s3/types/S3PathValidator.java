package one.microstream.afs.aws.s3.types;

/*-
 * #%L
 * microstream-afs-aws-s3
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

public interface S3PathValidator extends BlobStorePath.Validator
{

	public static S3PathValidator New()
	{
		return new S3PathValidator.Default();
	}


	public static class Default implements S3PathValidator
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
		 * https://docs.aws.amazon.com/awscloudtrail/latest/userguide/cloudtrail-s3-bucket-naming-requirements.html
		 */
		void validateBucketName(
			final String bucketName
		)
		{
			final int length = bucketName.length();
			if(length < 3
			|| length > 63
			)
			{
				throw new IllegalArgumentException(
					"bucket name must be between 3 and 63 characters long"
				);
			}
			if(!Pattern.matches(
				"[a-z0-9\\.\\-]*",
				bucketName
			))
			{
				throw new IllegalArgumentException(
					"bucket name can contain only lowercase letters, numbers, periods (.) and dashes (-)"
				);
			}
			if(!Pattern.matches(
				"[a-z0-9]",
				bucketName.substring(0, 1)
			))
			{
				throw new IllegalArgumentException(
					"bucket name must begin with a lowercase letters or a number"
				);
			}
			if(bucketName.endsWith("-"))
			{
				throw new IllegalArgumentException(
					"bucket name must not end with a dash (-)"
				);
			}
			if(bucketName.contains(".."))
			{
				throw new IllegalArgumentException(
					"bucket name cannot have consecutive periods (..)"
				);
			}
			if(bucketName.contains(".-")
			|| bucketName.contains("-."))
			{
				throw new IllegalArgumentException(
					"bucket name cannot have dashes adjacent to periods (.- or -.)"
				);
			}
			if(Pattern.matches(
				"^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$",
				bucketName
			))
			{
				throw new IllegalArgumentException(
					"bucket name must not be in an IP address style"
				);
			}
			if(bucketName.startsWith("xn--"))
			{
				throw new IllegalArgumentException(
					"bucket names must not start with 'xn--'"
				);
			}
		}

	}

}
