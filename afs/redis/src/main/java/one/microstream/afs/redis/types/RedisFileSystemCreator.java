package one.microstream.afs.redis.types;

/*-
 * #%L
 * microstream-afs-redis
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

import one.microstream.afs.blobstore.types.BlobStoreFileSystem;
import one.microstream.afs.types.AFileSystem;
import one.microstream.chars.XChars;
import one.microstream.configuration.types.Configuration;
import one.microstream.configuration.types.ConfigurationBasedCreator;

public class RedisFileSystemCreator extends ConfigurationBasedCreator.Abstract<AFileSystem>
{
	public RedisFileSystemCreator()
	{
		super(AFileSystem.class);
	}

	@Override
	public AFileSystem create(
		final Configuration configuration
	)
	{
		final String redisUri = configuration.get("redis.uri");
		if(XChars.isEmpty(redisUri))
		{
			return null;
		}

		final boolean        cache     = configuration.optBoolean("cache").orElse(true);
		final RedisConnector connector = cache
			? RedisConnector.Caching(redisUri)
			: RedisConnector.New(redisUri)
		;
		return BlobStoreFileSystem.New(connector);
	}

}
