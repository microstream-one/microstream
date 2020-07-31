package one.microstream.afs.oracle.nosql;

import static java.util.stream.Collectors.toList;
import static one.microstream.X.checkArrayRange;
import static one.microstream.X.notNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import one.microstream.afs.blobstore.BlobStoreConnector;
import one.microstream.afs.blobstore.BlobStorePath;
import one.microstream.chars.VarString;
import one.microstream.exceptions.IORuntimeException;
import one.microstream.io.ByteBufferInputStream;
import one.microstream.io.LimitedInputStream;
import oracle.kv.KVStore;
import oracle.kv.Key;
import oracle.kv.impl.api.KVStoreImpl;
import oracle.kv.table.PrimaryKey;
import oracle.kv.table.ReturnRow.Choice;
import oracle.kv.table.Row;
import oracle.kv.table.Table;
import oracle.kv.table.TableAPI;
import oracle.kv.table.TableOpExecutionException;
import oracle.kv.table.TableOperation;

/**
 * Connector for the <a href="https://www.oracle.com/database/technologies/related/nosql.html">Oracle NoSQL database</a>.
 * <p>
 * First create a connection to a <a href="https://docs.oracle.com/database/nosql-12.2.4.4/GettingStartedGuide/kvapi.html">key value store</a>.
 * <pre>
 * KVStore kvstore = ...
 * BlobStoreFileSystem fileSystem = BlobStoreFileSystem.New(
 * 	OracleNoSqlConnector.New(kvstore)
 * );
 * </pre>
 *
 * @author FH
 *
 */
public interface OracleNoSqlConnector extends BlobStoreConnector
{
	/**
	 * Pseude-constructor method which creates a new {@link OracleNoSqlConnector}.
	 *
	 * @param kvstore connection to a key value store
	 * @return a new {@link OracleNoSqlConnector}
	 */
	public static OracleNoSqlConnector New(
		final KVStore kvstore
	)
	{
		return new Default(
			notNull(kvstore),
			false
		);
	}
	
	/**
	 * Pseude-constructor method which creates a new {@link OracleNoSqlConnector} with cache.
	 *
	 * @param kvstore connection to a key value store
	 * @return a new {@link OracleNoSqlConnector}
	 */
	public static OracleNoSqlConnector Caching(
		final KVStore kvstore
	)
	{
		return new Default(
			notNull(kvstore),
			true
		);
	}


	public static class Default
	extends    BlobStoreConnector.Abstract<BlobMetadata>
	implements OracleNoSqlConnector
	{
		private final static String KEY           = "key";
		private final static String SEQ           = "seq";
		private final static String SIZE          = "size";

		private final static long   MAX_BLOB_SIZE = 134_217_728L; // 128 MB


		private final KVStore            kvstore;
		private final Map<String, Table> tables;

		Default(
			final KVStore kvstore  ,
			final boolean withCache
		)
		{
			super(
				BlobMetadata::key,
				BlobMetadata::size,
				OracleNoSqlPathValidator.New(),
				withCache
			);
			this.kvstore = kvstore;
			this.tables  = new HashMap<>();
		}

		private Table table(
			final BlobStorePath path
		)
		{
			return this.tables.computeIfAbsent(
				path.container(),
				this::createTable
			);
		}

		private Table createTable(
			final String name
		)
		{
			Table table = this.kvstore.getTableAPI().getTable(name);
			if(table == null)
			{
				final String statement = VarString.New()
					.add("create table ").add(name).add(" (")
					.add(KEY).add(" string, ")
					.add(SEQ).add(" long, ")
					.add(SIZE).add(" long, ")
					.add("primary key (shard(").add(KEY).add("), ").add(SEQ).add("))")
					.toString();

				try
				{
					this.kvstore.execute(statement).get();
				}
				catch(InterruptedException | ExecutionException e)
				{
					// TODO proper exception
					throw new RuntimeException(e);
				}

				table = this.kvstore.getTableAPI().getTable(name);
			}

			return table;
		}

		private Key key(
			final BlobStorePath file,
			final long          seq
		)
		{
			final String suffix = this.kvstore instanceof KVStoreImpl
				? ((KVStoreImpl)this.kvstore).getDefaultLOBSuffix()
				: ".lob"
			;
			return Key.createKey(
				Arrays.asList(file.pathElements()),
				Arrays.asList(seq + suffix)
			);
		}

		private InputStream blobInputStream(
			final BlobStorePath file,
			final long          seq
		)
		{
			return this.kvstore.getLOB(
				this.key(file, seq),
				null,
				0L,
				null
			)
			.getInputStream();
		}

		@Override
		protected long blobNumber(
			final BlobMetadata blob
		)
		{
			return blob.seq();
		}

		@Override
		protected Stream<BlobMetadata> blobs(
			final BlobStorePath file
		)
		{
			final Table      table = this.table(file);
			final PrimaryKey pk    = table.createPrimaryKey();
			pk.put(KEY, file.fullQualifiedName());
			return this.kvstore.getTableAPI()
				.multiGet(pk, null, null)
				.stream()
				.map(row -> BlobMetadata.New(
					row.get(KEY ).asString().get(),
					row.get(SEQ ).asLong().get(),
					row.get(SIZE).asLong().get()
				))
				.sorted(this.blobComparator())
			;
		}

		/*
		 * TODO check if it can be done more efficient with queries instead of iterating over all keys
		 */
		@Override
		protected Stream<String> childKeys(
			final BlobStorePath directory
		)
		{
			final Set<String> keys    = new LinkedHashSet<>();
			final Table       table   = this.table(directory);
			final PrimaryKey  pk      = table.createPrimaryKey();
			final Pattern     pattern = Pattern.compile(childKeysRegexWithContainer(directory));
			this.kvstore.getTableAPI()
				.tableIterator(pk, null, null)
				.forEachRemaining(row ->
				{
					final String key = row.get(KEY).asString().get();
					if(pattern.matcher(key).matches())
					{
						keys.add(key);
					}
				});
			;
			return keys.stream();
		}

		@Override
		protected String fileNameOfKey(
			final String key
		)
		{
			return key.substring(
				key.lastIndexOf(BlobStorePath.SEPARATOR_CHAR) + 1
			);
		}

		@Override
		protected void internalReadBlobData(
			final BlobStorePath file        ,
			final BlobMetadata  metadata    ,
			final ByteBuffer    targetBuffer,
			final long          offset      ,
			final long          length
		)
		{
			try(final InputStream inputStream = this.blobInputStream(file, metadata.seq()))
			{
				if(offset > 0L)
				{
					inputStream.skip(offset);
				}

				final byte[] buffer    = new byte[1024 * 10];
				      long   remaining = length;
				      int    read;
				while(remaining > 0 &&
					(read = inputStream.read(
						buffer,
						0,
						Math.min(buffer.length, checkArrayRange(remaining)))
					) != -1
				)
				{
					targetBuffer.put(buffer, 0, read);
					remaining -= read;
				}
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}

		@Override
		protected boolean internalDeleteFile(
			final BlobStorePath file
		)
		{
			final TableAPI tapi  = this.kvstore.getTableAPI();
			final Table    table = tapi.getTable(file.container());
			if(table == null)
			{
				return false;
			}

			final List<BlobMetadata> blobs = this.blobs(file).collect(toList());
			if(blobs.isEmpty())
			{
				return false;
			}

			for(final BlobMetadata metadata : blobs)
			{
				final Key key = this.key(file, metadata.seq());
				if(!this.kvstore.deleteLOB(key, null, 0L, null))
				{
					return false;
				}
			}

			final PrimaryKey pk = table.createPrimaryKey();
			pk.put(KEY, file.fullQualifiedName());
			return tapi.multiDelete(pk, null, null) > 0;
		}

		@Override
		protected boolean internalDeleteBlobs(
			final BlobStorePath                file ,
			final List<? extends BlobMetadata> blobs
		)
		{
			final TableAPI tapi  = this.kvstore.getTableAPI();
			final Table    table = tapi.getTable(file.container());
			if(table == null)
			{
				return false;
			}

			for(final BlobMetadata metadata : blobs)
			{
				final Key key = this.key(file, metadata.seq());
				if(!this.kvstore.deleteLOB(key, null, 0L, null))
				{
					return false;
				}

				final PrimaryKey pk = table.createPrimaryKey();
				pk.put(KEY, file.fullQualifiedName());
				pk.put(SEQ, metadata.seq());
				if(!tapi.delete(pk, null, null))
				{
					return false;
				}
			}

			return true;
		}

		@Override
		protected long internalWriteData(
			final BlobStorePath                  file         ,
			final Iterable<? extends ByteBuffer> sourceBuffers
		)
		{
			final Table                 table              = this.table(file);
			final List<TableOperation>  tableOperations    = new ArrayList<>();
			      long                  nextBlobNumber     = this.nextBlobNumber(file);
			final long                  totalSize          = this.totalSize(sourceBuffers);
			final ByteBufferInputStream buffersInputStream = ByteBufferInputStream.New(sourceBuffers);
			      long                  available          = totalSize;
			while(available > 0)
			{
				final long currentBatchSize = Math.min(
					available,
					MAX_BLOB_SIZE
				);

				try(final LimitedInputStream limitedInputStream = LimitedInputStream.New(
					new BufferedInputStream(buffersInputStream),
					currentBatchSize
				))
				{
					final Row row = table.createRow();
					row.put(KEY , file.fullQualifiedName());
					row.put(SEQ , nextBlobNumber);
					row.put(SIZE, currentBatchSize);
					tableOperations.add(this.kvstore.getTableAPI().getTableOperationFactory().createPut(
						row,
						Choice.NONE,
						true
					));

					this.kvstore.putLOB(
						this.key(file, nextBlobNumber++),
						limitedInputStream,
						null,
						0L,
						null
					);
				}
				catch(final IOException e)
				{
					throw new IORuntimeException(e);
				}

				available -= currentBatchSize;
			}

			try
			{
				this.kvstore.getTableAPI().execute(tableOperations, null);
			}
			catch(final TableOpExecutionException e)
			{
				// TODO proper exception
				throw new RuntimeException(e);
			}

			return totalSize;
		}

	}

}
