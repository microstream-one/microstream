package one.microstream.afs.mongodb.types;

import static java.util.stream.Collectors.toList;
import static one.microstream.X.checkArrayRange;
import static one.microstream.X.notNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Binary;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Updates;

import one.microstream.afs.blobstore.types.BlobStoreConnector;
import one.microstream.afs.blobstore.types.BlobStorePath;
import one.microstream.exceptions.IORuntimeException;
import one.microstream.io.ByteBufferInputStream;
import one.microstream.io.LimitedInputStream;

/**
 * Connector for <a href="https://www.mongodb.com/">MongoDB</a> database and GridFS.
 * <p>
 * First create a connection to a <a href="http://mongodb.github.io/mongo-java-driver/3.12/driver/getting-started/quick-start/">MongoDB database</a>.
 * <pre>
 * MongoDatabase database = ...
 * BlobStoreFileSystem fileSystem = BlobStoreFileSystem.New(
 * 	MongoDbConnector.New(database)
 * );
 * </pre>
 * or
 * <pre>
 * BlobStoreFileSystem fileSystem = BlobStoreFileSystem.New(
 * 	MongoDbConnector.GridFs(database)
 * );
 * </pre>
 *
 * 
 *
 */
public interface MongoDbConnector extends BlobStoreConnector
{
	/**
	 * Pseudo-constructor method which creates a new {@link MongoDbConnector}.
	 *
	 * @param database connection to the MongoDB database
	 * @return a new {@link MongoDbConnector}
	 */
	public static MongoDbConnector New(
		final MongoDatabase database
	)
	{
		return new MongoDbConnector.Default(
			notNull(database),
			false
		);
	}

	/**
	 * Pseudo-constructor method which creates a new {@link MongoDbConnector} with cache.
	 *
	 * @param database connection to the MongoDB database
	 * @return a new {@link MongoDbConnector}
	 */
	public static MongoDbConnector Caching(
		final MongoDatabase database
	)
	{
		return new MongoDbConnector.Default(
			notNull(database),
			true
		);
	}

	/**
	 * Pseudo-constructor method which creates a new {@link MongoDbConnector} for GridFS.
	 *
	 * @param database connection to the MongoDB database
	 * @return a new {@link MongoDbConnector}
	 */
	public static MongoDbConnector GridFs(
		final MongoDatabase database
	)
	{
		return new MongoDbConnector.GridFs(
			notNull(database),
			false
		);
	}

	/**
	 * Pseudo-constructor method which creates a new {@link MongoDbConnector} for GridFS with cache.
	 *
	 * @param database connection to the MongoDB database
	 * @return a new {@link MongoDbConnector}
	 */
	public static MongoDbConnector GridFsCaching(
		final MongoDatabase database
	)
	{
		return new MongoDbConnector.GridFs(
			notNull(database),
			true
		);
	}


	public static class Default
	extends    BlobStoreConnector.Abstract<Document>
	implements MongoDbConnector
	{
		private final static String FIELD_KEY  = "key" ;
		private final static String FIELD_SIZE = "size";
		private final static String FIELD_DATA = "data";
		private final static String INDEX_NAME = "key-index";

		// https://docs.mongodb.com/manual/reference/limits/
		// 16 MB payload maximum minus 16 KB for the rest
		private final static long   MAX_BLOB_SIZE = 16_777_216L - 16_384L;

		private final MongoDatabase                          database   ;
		private final Map<String, MongoCollection<Document>> collections;

		Default(
			final MongoDatabase database ,
			final boolean       withCache
		)
		{
			super(
				blob -> blob.getString(FIELD_KEY ),
				blob -> blob.getLong  (FIELD_SIZE),
				MongoDbPathValidator.New(),
				withCache
			);
			this.database    = database       ;
			this.collections = new HashMap<>();
		}

		private MongoCollection<Document> collection(
			final BlobStorePath path
		)
		{
			synchronized(this.collections)
			{
				return this.collections.computeIfAbsent(
					path.container(),
					this::createCollection
				);
			}
		}

		private MongoCollection<Document> createCollection(
			final String name
		)
		{
			final MongoCollection<Document> collection = this.database.getCollection(name);
			final boolean                   hasIndex   = StreamSupport.stream(
				collection.listIndexes().spliterator(),
				false
			)
			.filter(d -> {
				return INDEX_NAME.equals(d.getString("name"));
			})
			.findAny()
			.isPresent();

			if(!hasIndex)
			{
				collection.createIndex(
					Indexes.hashed(FIELD_KEY),
					new IndexOptions().name(INDEX_NAME)
				);
			}

			return collection;
		}

		private Bson filterFor(
			final BlobStorePath file
		)
		{
			return Filters.regex(
				FIELD_KEY,
				Pattern.compile(
					blobKeyRegex(toBlobKeyPrefix(file))
				)
			);
		}

		private Bson filterForChildren(
			final BlobStorePath directory
		)
		{
			return Filters.regex(
				FIELD_KEY,
				Pattern.compile(
					childKeysRegex(directory)
				)
			);
		}

		private Bson filterFor(
			final List<? extends Document> blobs
		)
		{
			return Filters.in(
				FIELD_KEY,
				blobs.stream()
					.map(blob -> blob.getString(FIELD_KEY))
					.collect(toList())
			);
		}

		private Bson filterFor(
			final Document blob
		)
		{
			return Filters.eq(
				FIELD_KEY,
				blob.getString(FIELD_KEY)
			);
		}

		private Stream<Document> blobs(
			final BlobStorePath file    ,
			final boolean       withData
		)
		{
			final FindIterable<Document> iterable = this.collection(file)
				.find(this.filterFor(file))
			;
			if(!withData)
			{
				iterable.projection(Projections.include(FIELD_KEY, FIELD_SIZE));
			}
			return StreamSupport.stream(
				iterable.spliterator(),
				false
			)
			.sorted(this.blobComparator())
			;
		}

		@Override
		protected Stream<String> childKeys(
			final BlobStorePath directory
		)
		{
			final FindIterable<Document> iterable = this.collection(directory)
				.find(this.filterForChildren(directory))
				.projection(Projections.include(FIELD_KEY));
			return StreamSupport.stream(
				iterable.spliterator(),
				false
			)
			.map(document -> document.getString(FIELD_KEY))
			;
		}

		@Override
		protected Stream<Document> blobs(
			final BlobStorePath file
		)
		{
			return this.blobs(file, false);
		}

		@Override
		protected boolean internalFileExists(
			final BlobStorePath file
		)
		{
			final long count = this.collection(file)
				.countDocuments(this.filterFor(file));
			return count > 0L;
		}

		@Override
		protected void internalReadBlobData(
			final BlobStorePath file        ,
			final Document      blob        ,
			final ByteBuffer    targetBuffer,
			final long          offset      ,
			final long          length
		)
		{
			/*
			 *  Fetch blob again with data.
			 *  Per default they are loaded without it.
			 */
			final Document fullBlob = this.collection(file)
				.find(this.filterFor(blob))
				.first();
			final Binary binary = fullBlob.get(FIELD_DATA, Binary.class);
			targetBuffer.put(
				binary.getData(),
				checkArrayRange(offset),
				checkArrayRange(length)
			);
		}

		@Override
		protected boolean internalDeleteFile(
			final BlobStorePath file
		)
		{
			final MongoCollection<Document> collection   = this.collection(file);
			final Bson                      filter       = this.filterFor(file);
			final long                      count        = collection.countDocuments(filter);
			if(count == 0L)
			{
				return false;
			}

			final long                      deletedCount = collection
				.deleteMany(filter)
				.getDeletedCount()
			;
			return deletedCount == count;
		}

		@Override
		protected boolean internalDeleteBlobs(
			final BlobStorePath            file ,
			final List<? extends Document> blobs
		)
		{
			final long deletedCount = this.collection(file)
				.deleteMany(this.filterFor(blobs))
				.getDeletedCount()
			;
			return deletedCount == blobs.size();
		}

		@Override
		protected long internalWriteData(
			final BlobStorePath                  file         ,
			final Iterable<? extends ByteBuffer> sourceBuffers
		)
		{
			final List<Document>        documents          = new ArrayList<>();
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

				try(LimitedInputStream limitedInputStream = LimitedInputStream.New(
					new BufferedInputStream(buffersInputStream),
					currentBatchSize))
				{
					final byte[] batch = new byte[checkArrayRange(currentBatchSize)];
						  int    remaining = batch.length;
				          int    read;
					while(remaining > 0 &&
						(read = limitedInputStream.read(
							batch,
							0,
							Math.min(batch.length, remaining))
						) != -1
					)
					{
						remaining -= read;
					}
					final Document document = new Document();
					document.put(FIELD_KEY, toBlobKey(file, nextBlobNumber++));
					document.put(FIELD_SIZE, currentBatchSize);
					document.put(FIELD_DATA, new Binary(batch));
					documents.add(document);
				}
				catch(final IOException e)
				{
					throw new IORuntimeException(e);
				}

				available -= currentBatchSize;
			}

			if(!this.collection(file).insertMany(documents).wasAcknowledged())
			{
				throw new RuntimeException("Write to " + file.fullQualifiedName() + " was not acknowledged.");
			}

			return totalSize;
		}

		@Override
		protected void internalMoveFile(
			final BlobStorePath sourceFile,
			final BlobStorePath targetFile
		)
		{
			if(sourceFile.container().equals(targetFile.container()))
			{
				this.blobs(sourceFile).forEach(blob ->
				{
					final Bson update = Updates.set(
						FIELD_KEY,
						toBlobKey(sourceFile, this.blobNumber(blob))
					);
					this.collection(sourceFile).updateOne(
						this.filterFor(blob),
						update
					);
				});
			}
			else
			{
				super.internalMoveFile(sourceFile, targetFile);
			}
		}

		@Override
		protected void internalClose()
		{
			synchronized(this.collections)
			{
				this.collections.clear();
			}
		}

	}


	public static class GridFs
	extends    BlobStoreConnector.Abstract<GridFSFile>
	implements MongoDbConnector
	{
		private final MongoDatabase             database;
		private final Map<String, GridFSBucket> buckets ;

		GridFs(
			final MongoDatabase database ,
			final boolean       withCache
		)
		{
			super(
				GridFSFile::getFilename,
				GridFSFile::getLength,
				withCache
			);
			this.database = database       ;
			this.buckets  = new HashMap<>();
		}

		private GridFSBucket bucket(
			final BlobStorePath path
		)
		{
			synchronized(this.buckets)
			{
				return this.buckets.computeIfAbsent(
					path.container(),
					name -> GridFSBuckets.create(this.database, name)
				);
			}
		}

		@Override
		protected Stream<GridFSFile> blobs(
			final BlobStorePath file
		)
		{
			final String  prefix  = toBlobKeyPrefix(file);
			final Pattern pattern = Pattern.compile(blobKeyRegex(prefix));
			final Bson    filter  = Filters.regex("filename", pattern);
			return StreamSupport.stream(
				this.bucket(file).find(filter).spliterator(),
				false
			)
			.sorted(this.blobComparator())
			;
		}

		@Override
		protected Stream<String> childKeys(
			final BlobStorePath directory
		)
		{
			final Pattern pattern = Pattern.compile(childKeysRegex(directory));
			final Bson    filter  = Filters.regex("filename", pattern);
			return StreamSupport.stream(
				this.bucket(directory).find(filter).spliterator(),
				false
			)
			.map(GridFSFile::getFilename)
			;
		}

		@Override
		protected void internalReadBlobData(
			final BlobStorePath file        ,
			final GridFSFile    blob        ,
			final ByteBuffer    targetBuffer,
			final long          offset      ,
			final long          length
		)
		{
			try(final GridFSDownloadStream downloadStream =
				this.bucket(file).openDownloadStream(blob.getObjectId())
			)
			{
				if(offset > 0L)
				{
					downloadStream.skip(offset);
				}
				final byte[] buffer    = new byte[1024 * 10];
			          long   remaining = length;
			          int    read;
				while(remaining > 0 &&
					(read = downloadStream.read(
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
		}

		@Override
		protected boolean internalDeleteBlobs(
			final BlobStorePath              file ,
			final List<? extends GridFSFile> blobs
		)
		{
			final GridFSBucket bucket = this.bucket(file);
			blobs.forEach(
				blob -> bucket.delete(blob.getObjectId())
			);

			return true;
		}

		@Override
		protected long internalWriteData(
			final BlobStorePath                  file         ,
			final Iterable<? extends ByteBuffer> sourceBuffers
		)
		{
			final long nextBlobNumber = this.nextBlobNumber(file);
			final long totalSize      = this.totalSize(sourceBuffers);

			try(final BufferedInputStream inputStream = new BufferedInputStream(
				ByteBufferInputStream.New(sourceBuffers)
			))
			{
				this.bucket(file).uploadFromStream(
					toBlobKey(file, nextBlobNumber),
					inputStream
				);
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}

			return totalSize;
		}

		@Override
		protected void internalMoveFile(
			final BlobStorePath sourceFile,
			final BlobStorePath targetFile
		)
		{
			if(sourceFile.container().equals(targetFile.container()))
			{
				this.blobs(sourceFile).forEach(blob ->
				{
					this.bucket(sourceFile).rename(
						blob.getObjectId(),
						toBlobKey(
							targetFile,
							this.blobNumber(blob)
						)
					);
				});
			}
			else
			{
				super.internalMoveFile(sourceFile, targetFile);
			}
		}

		@Override
		protected void internalClose()
		{
			synchronized(this.buckets)
			{
				this.buckets.clear();
			}
		}

	}

}
