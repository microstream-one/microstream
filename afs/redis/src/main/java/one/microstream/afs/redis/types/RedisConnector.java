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

import static one.microstream.X.checkArrayRange;
import static one.microstream.X.notNull;
import static one.microstream.chars.XChars.notEmpty;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import one.microstream.afs.blobstore.types.BlobStoreConnector;
import one.microstream.afs.blobstore.types.BlobStorePath;

/**
 * Connector for <a href="https://redis.io/">Redis</a> which utilizes the <a href="https://lettuce.io/">Lettuce client</a>.
 * <p>
 * First create a <a href="https://lettuce.io/">Lettuce Redis client</a>.
 * <pre>
 * RedisClient client = ...
 * BlobStoreFileSystem fileSystem = BlobStoreFileSystem.New(
 * 	RedisConnector.New(client)
 * );
 * </pre>
 *
 * 
 *
 */
public interface RedisConnector extends BlobStoreConnector
{
	/**
	 * Pseudo-constructor method which creates a new {@link RedisConnector}.
	 *
	 * @param redisUri url to connect to
	 * @return a new {@link RedisConnector}
	 */
	public static RedisConnector New(
		final String redisUri
	)
	{
		return New(
			RedisClient.create(
				notEmpty(redisUri)
			)
		);
	}
	
	/**
	 * Pseudo-constructor method which creates a new {@link RedisConnector} with cache.
	 *
	 * @param redisUri url to connect to
	 * @return a new {@link RedisConnector}
	 */
	public static RedisConnector Caching(
		final String redisUri
	)
	{
		return Caching(
			RedisClient.create(
				notEmpty(redisUri)
			)
		);
	}

	/**
	 * Pseudo-constructor method which creates a new {@link RedisConnector}.
	 *
	 * @param client Redis client connection
	 * @return a new {@link RedisConnector}
	 */
	public static RedisConnector New(
		final RedisClient client
	)
	{
		return new RedisConnector.Default(
			notNull(client),
			false
		);
	}
	
	/**
	 * Pseudo-constructor method which creates a new {@link RedisConnector} with cache.
	 *
	 * @param client Redis client connection
	 * @return a new {@link RedisConnector}
	 */
	public static RedisConnector Caching(
		final RedisClient client
	)
	{
		return new RedisConnector.Default(
			notNull(client),
			true
		);
	}


	public static class Default
	extends    BlobStoreConnector.Abstract<BlobMetadata>
	implements RedisConnector
	{
		private final RedisClient                                 client    ;
		private       StatefulRedisConnection<String, ByteBuffer> connection;
		private       RedisCommands          <String, ByteBuffer> commands  ;

		Default(
			final RedisClient client   ,
			final boolean     withCache
		)
		{
			super(
				BlobMetadata::key,
				BlobMetadata::size,
				withCache
			);
			this.client = client;
		}

		private RedisCommands<String, ByteBuffer> commands()
		{
			if(this.commands == null)
			{
				synchronized(this)
				{
					if(this.commands == null)
					{
						this.commands = (
							this.connection = this.client.connect(
								StringByteBufferCodec.New()
							)
						)
						.sync();

						this.commands.setTimeout(Duration.ofMinutes(1L));
					}
				}
			}

			return this.commands;
		}

		@Override
		protected Stream<BlobMetadata> blobs(
			final BlobStorePath file
		)
		{
			final RedisCommands<String, ByteBuffer> commands = this.commands();
			final String                            prefix   = toBlobKeyPrefixWithContainer(file);
			final Pattern                           pattern  = Pattern.compile(blobKeyRegex(prefix));
			return commands.keys(prefix.concat("*"))
				.stream()
				.filter(key -> pattern.matcher(key).matches())
				.map(key ->
					BlobMetadata.New(
						key,
						commands.strlen(key)
					)
				)
				.sorted(this.blobComparator())
			;
		}

		@Override
		protected Stream<String> childKeys(
			final BlobStorePath directory
		)
		{
			final RedisCommands<String, ByteBuffer> commands = this.commands();
			final Pattern                           pattern  = Pattern.compile(childKeysRegexWithContainer(directory));
			return commands.keys(toChildKeysPrefixWithContainer(directory).concat("*"))
				.stream()
				.filter(key -> pattern.matcher(key).matches())
			;
		}

		@Override
		protected void internalReadBlobData(
			final BlobStorePath   file        ,
			final BlobMetadata    blob        ,
			final ByteBuffer      targetBuffer,
			final long            offset      ,
			final long            length
		)
		{
			targetBuffer.put(
				this.commands().getrange(
					blob.key(),
					offset,
					offset + length - 1L
				)
			);
		}

		@Override
		protected boolean internalDeleteBlobs(
			final BlobStorePath                file ,
			final List<? extends BlobMetadata> blobs
		)
		{
			final String[] keys   = blobs.stream()
				.map(BlobMetadata::key)
				.toArray(String[]::new)
			;
			final Long     result = this.commands.del(keys);
			return result != null
				&& result.intValue() == blobs.size()
			;
		}

		@Override
		protected long internalWriteData(
			final BlobStorePath                  file         ,
			final Iterable<? extends ByteBuffer> sourceBuffers
		)
		{
			final long nextBlobNumber = this.nextBlobNumber(file);
			final long totalSize      = this.totalSize(sourceBuffers);

			final ByteBuffer buffer = ByteBuffer.allocateDirect(checkArrayRange(totalSize));
			sourceBuffers.forEach(sourceBuffer -> buffer.put(sourceBuffer));
			buffer.flip();

			final String result = this.commands().set(
				toBlobKeyWithContainer(file, nextBlobNumber),
				buffer
			);
			if(!"OK".equalsIgnoreCase(result))
			{
				throw new RuntimeException("Error writing data: " + result);
			}

			return totalSize;
		}

		@Override
		protected synchronized void internalClose()
		{
			if(this.connection != null)
			{
				this.connection.close();
				this.connection = null;
			}
			this.commands = null;
		}

	}

}
