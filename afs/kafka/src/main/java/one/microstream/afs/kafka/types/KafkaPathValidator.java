package one.microstream.afs.kafka.types;

/*-
 * #%L
 * microstream-afs-kafka
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

public interface KafkaPathValidator extends BlobStorePath.Validator
{

	public static KafkaPathValidator New()
	{
		return new KafkaPathValidator.Default();
	}


	public static class Default implements KafkaPathValidator
	{
		Default()
		{
			super();
		}

		/*
		 * https://stackoverflow.com/questions/37062904/what-are-apache-kafka-topic-name-limitations
		 */
		@Override
		public void validate(
			final BlobStorePath path
		)
		{
			final String name = path.fullQualifiedName().replace(BlobStorePath.SEPARATOR_CHAR, '_');
			if(name.length() > 249)
			{
				throw new IllegalArgumentException(
					"full qualified path name cannot be longer than 249 characters"
				);
			}
			if(!Pattern.matches(
				"[a-zA-Z0-9\\._\\-]*",
				name
			))
			{
				throw new IllegalArgumentException(
					"path can contain only letters, numbers, periods (.), underscores (_) and dashes (-)"
				);
			}
		}

	}

}
