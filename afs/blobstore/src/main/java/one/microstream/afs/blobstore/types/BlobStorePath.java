package one.microstream.afs.blobstore.types;

/*-
 * #%L
 * microstream-afs-blobstore
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

import static java.util.stream.Collectors.joining;

import java.util.Arrays;

import one.microstream.chars.XChars;
import one.microstream.collections.XArrays;

public interface BlobStorePath
{
	@FunctionalInterface
	public static interface Validator
	{
		public final static Validator NO_OP = path ->
		{
			// no-op
		};


		public void validate(BlobStorePath path);
	}


	public final static String SEPARATOR      = "/";
	public final static char   SEPARATOR_CHAR = '/';

	public String[] pathElements();

	public String container();

	public String identifier();

	public String fullQualifiedName();

	public BlobStorePath parentPath();


	public static String[] splitPath(
		final String fullQualifiedPath
	)
	{
		return XChars.splitSimple(fullQualifiedPath, SEPARATOR);
	}

	public static BlobStorePath New(
		final String... pathElements
	)
	{
		if(pathElements.length == 0)
		{
			throw new IllegalArgumentException("empty path");
		}
		for(final String element : pathElements)
		{
			if(element.isEmpty())
			{
				throw new IllegalArgumentException("empty path element");
			}
		}

		return new Default(pathElements);
	}


	public final static class Default implements BlobStorePath
	{
		private final String[] pathElements     ;
		private       String   fullQualifiedName;

		Default(
			final String[] pathElements
		)
		{
			super();
			this.pathElements = pathElements;
		}

		@Override
		public String[] pathElements()
		{
			return this.pathElements;
		}

		@Override
		public String container()
		{
			return this.pathElements[0];
		}

		@Override
		public String identifier()
		{
			return this.pathElements[this.pathElements.length - 1];
		}

		@Override
		public String fullQualifiedName()
		{
			if(this.fullQualifiedName == null)
			{
				this.fullQualifiedName = this.pathElements.length == 1
					? this.pathElements[0]
					: Arrays
						.stream(this.pathElements)
						.collect(joining(SEPARATOR))
				;
			}

			return this.fullQualifiedName;
		}

		@Override
		public BlobStorePath parentPath()
		{
			return this.pathElements.length > 1
				? new BlobStorePath.Default(
					XArrays.copyRange(this.pathElements, 0, this.pathElements.length - 1)
				)
				: null;
		}

	}

}
