package one.microstream.afs.aws.dynamodb;

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
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.BatchWriteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.RangeKeyCondition;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.TableWriteItems;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.Delete;
import com.amazonaws.services.dynamodbv2.model.DescribeLimitsRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeLimitsResult;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.Select;
import com.amazonaws.services.dynamodbv2.model.TransactWriteItem;
import com.amazonaws.services.dynamodbv2.model.TransactWriteItemsRequest;
import com.amazonaws.services.dynamodbv2.model.Update;

import one.microstream.afs.blobstore.BlobStoreConnector;
import one.microstream.afs.blobstore.BlobStorePath;
import one.microstream.exceptions.IORuntimeException;
import one.microstream.io.ByteBufferInputStream;
import one.microstream.io.LimitedInputStream;


public interface DynamoDbConnector extends BlobStoreConnector
{
	public static DynamoDbConnector New(
		final AmazonDynamoDB dynamoDb
	)
	{
		return new DynamoDbConnector.Default(
			notNull(dynamoDb)
		);
	}


	public static class Default
	extends    BlobStoreConnector.Abstract<Item>
	implements DynamoDbConnector
	{
		private final static String FIELD_KEY  = "key" ;
		private final static String FIELD_SEQ  = "seq" ;
		private final static String FIELD_SIZE = "size";
		private final static String FIELD_DATA = "data";

		// https://docs.aws.amazon.com/de_de/amazondynamodb/latest/developerguide/Limits.html
		private final static long MAX_BLOB_SIZE     =   400_000;
		private final static long MAX_REQUEST_SIZE  = 4_000_000;
		private final static long MAX_REQUEST_ITEMS =        25;

		private final AmazonDynamoDB     client  ;
		private final DynamoDB           dynamoDB;
		private final Map<String, Table> tables  ;

		Default(
			final AmazonDynamoDB client
		)
		{
			super();
			this.client   = client              ;
			this.dynamoDB = new DynamoDB(client);
			this.tables   = new HashMap<>();
		}

		private Table table(
			final BlobStorePath file
		)
		{
			return this.tables.computeIfAbsent(
				file.container() ,
				this::createTable
			);
		}

		private Table createTable(
			final String name)
		{
			try
			{
				this.client.describeTable(name);
				return this.dynamoDB.getTable(name);
			}
			catch(final ResourceNotFoundException e)
			{
				final DescribeLimitsResult limits = this.client.describeLimits(
					new DescribeLimitsRequest()
				);
				final CreateTableRequest   request = new CreateTableRequest()
					.withTableName(name)
					.withKeySchema(
						new KeySchemaElement(FIELD_KEY, KeyType.HASH ),
						new KeySchemaElement(FIELD_SEQ, KeyType.RANGE)
					)
					.withAttributeDefinitions(
						new AttributeDefinition(FIELD_KEY , ScalarAttributeType.S),
						new AttributeDefinition(FIELD_SEQ , ScalarAttributeType.N)
					)
					.withProvisionedThroughput(new ProvisionedThroughput(
						limits.getTableMaxReadCapacityUnits(),
						limits.getTableMaxWriteCapacityUnits()
					))
				;
				return this.dynamoDB.createTable(request);
			}
		}

		private Stream<Item> blobs(
			final BlobStorePath file    ,
			final boolean       withData
		)
		{
			final QuerySpec querySpec = new QuerySpec()
				.withHashKey(FIELD_KEY, file.fullQualifiedName())
			;
			if(!withData)
			{
				querySpec.withAttributesToGet(FIELD_KEY, FIELD_SEQ, FIELD_SIZE);
			}

			final ItemCollection<QueryOutcome> itemCollection = this.table(file).query(querySpec);
			try
			{
				if(itemCollection.iterator().hasNext())
				{
					return StreamSupport.stream(
						itemCollection.spliterator(),
						false
					)
					.sorted(this.blobComparator())
					;
				}
			}
			catch(final ResourceNotFoundException e)
			{
				// no items found
			}

			return Stream.empty();
		}

		private Map<String, AttributeValue> primaryKey(final BlobStorePath file)
		{
			final Map<String, AttributeValue> pk = new HashMap<>();
	        pk.put(FIELD_KEY, new AttributeValue(file.fullQualifiedName()));
	        return pk;
		}

		@Override
		protected String key(
			final Item blob
		)
		{
			return blob.getString(FIELD_KEY);
		}

		@Override
		protected long size(
			final Item blob
		)
		{
			return blob.getLong(FIELD_SIZE);
		}

		@Override
		protected long getBlobNr(final Item blob)
		{
			return blob.getLong(FIELD_SEQ);
		}

		@Override
		protected Stream<Item> blobs(
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
			final int count = this.table(file)
				.query(new QuerySpec()
					.withSelect(Select.COUNT)
					.withHashKey(FIELD_KEY, file.fullQualifiedName())
					.withRangeKeyCondition(new RangeKeyCondition(FIELD_SEQ).eq(0L))
				)
				.getAccumulatedItemCount();
			return count > 0;
		}

		@Override
		protected void readBlobData(
			final BlobStorePath file        ,
			final Item          blob        ,
			final ByteBuffer    targetBuffer,
			final long          offset      ,
			final long          length
		)
		{
			/*
			 *  Fetch blob again with data.
			 *  Per default they are loaded without it.
			 */
			final Item fullBlob = this.table(file).getItem(
				FIELD_KEY, file.fullQualifiedName(),
				FIELD_SEQ, this.getBlobNr(blob)
			);
			targetBuffer.put(
				fullBlob.getBinary(FIELD_DATA),
				checkArrayRange(offset),
				checkArrayRange(length)
			);
		}

		@Override
		protected boolean internalDeleteFile(
			final BlobStorePath file
		)
		{
			boolean                       deleted = false;
			final List<TransactWriteItem> deletes = new ArrayList<>();
			for(final Item item : this.blobs(file).collect(toList()))
			{
				final Map<String, AttributeValue> key = new HashMap<>();
		        key.put(FIELD_KEY, new AttributeValue(item.getString(FIELD_KEY)));
		        key.put(FIELD_SEQ, new AttributeValue().withN(Long.toString(item.getLong(FIELD_SEQ))));

		        deletes.add(new TransactWriteItem().withDelete(
		        	new Delete()
						.withTableName(file.container())
						.withKey(key)
				));
		        if(deletes.size() >= MAX_REQUEST_ITEMS)
		        {
		        	this.client.transactWriteItems(
						new TransactWriteItemsRequest().withTransactItems(deletes)
					);
		        	deletes.clear();;
		        	deleted = true;
		        }
			}

			if(!deletes.isEmpty())
			{
				this.client.transactWriteItems(
					new TransactWriteItemsRequest().withTransactItems(deletes)
				);
	        	deleted = true;
			}

			return deleted;
		}

		@Override
		protected long internalWriteData(
			final BlobStorePath                  file         ,
			final Iterable<? extends ByteBuffer> sourceBuffers
		)
		{
			final BatchWrite            batchWrite         = new BatchWrite(this.dynamoDB, file.container());
			      long                  nextBlobNr         = this.nextBlobNr(file);
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
					currentBatchSize)
				)
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

					final Item item = new Item()
						.withPrimaryKey(FIELD_KEY, file.fullQualifiedName())
						.withKeyComponent(FIELD_SEQ, nextBlobNr++)
						.withNumber(FIELD_SIZE, currentBatchSize)
						.withBinary(FIELD_DATA, batch)
					;
					batchWrite.add(item);
				}
				catch(final IOException e)
				{
					throw new IORuntimeException(e);
				}

				available -= currentBatchSize;
			}

			batchWrite.finish();

			return totalSize;
		}

		@Override
		protected long internalCopyFile(
			final BlobStorePath sourceFile,
			final BlobStorePath targetFile
		)
		{
			final BatchWrite batchWrite = new BatchWrite(this.dynamoDB, targetFile.container());
			      long       amount     = 0L;
			for(final Item blob : this.blobs(sourceFile, true).collect(toList()))
			{
				final long size = blob.getLong(FIELD_SIZE);
				final Item item = new Item()
					.withPrimaryKey(FIELD_KEY, targetFile.fullQualifiedName())
					.withKeyComponent(FIELD_SEQ, this.getBlobNr(blob))
					.withNumber(FIELD_SIZE, size)
					.withBinary(FIELD_DATA, blob.getBinary(FIELD_DATA))
				;
				batchWrite.add(item);

				amount += size;
			}

			batchWrite.finish();

			return amount;
		}

		@Override
		protected void internalMoveFile(
			final BlobStorePath sourceFile,
			final BlobStorePath targetFile
		)
		{
			if(sourceFile.container().equals(targetFile.container()))
			{
				final Update update = new Update()
					.withTableName(sourceFile.container())
					.withKey(this.primaryKey(sourceFile))
					.withUpdateExpression("set #" + FIELD_KEY +" = :" + FIELD_KEY)
					.addExpressionAttributeNamesEntry("#" + FIELD_KEY, FIELD_KEY)
					.addExpressionAttributeValuesEntry(":" + FIELD_KEY, new AttributeValue(targetFile.fullQualifiedName()));

				final TransactWriteItemsRequest request = new TransactWriteItemsRequest()
					.withTransactItems(new TransactWriteItem().withUpdate(update))
				;
				this.client.transactWriteItems(request);
			}
			else
			{
				super.internalMoveFile(sourceFile, targetFile);
			}
		}


		private static class BatchWrite
		{
			final DynamoDB        dynamoDb  ;
			final String          tableName ;
			      TableWriteItems writeItems;

			BatchWrite(
				final DynamoDB dynamoDb ,
				final String   tableName
			)
			{
				super();
				this.dynamoDb  = dynamoDb ;
				this.tableName = tableName;
			}


			void add(final Item item)
			{
				if(this.writeItems == null)
				{
					this.writeItems = new TableWriteItems(this.tableName);
				}

				this.writeItems.addItemToPut(item);

				final int itemCount = this.writeItems.getItemsToPut().size();
				if(itemCount >= MAX_REQUEST_ITEMS
				|| itemCount * MAX_BLOB_SIZE >= MAX_REQUEST_SIZE
				)
				{
					this.write();
					this.writeItems = null;
				}
			}

			void finish()
			{
				if(this.writeItems != null)
				{
					this.write();
					this.writeItems = null;
				}
			}

			private void write()
			{
				BatchWriteItemOutcome outcome = this.dynamoDb.batchWriteItem(this.writeItems);
				while(outcome.getUnprocessedItems().size() > 0)
				{
					outcome = this.dynamoDb.batchWriteItemUnprocessed(
						outcome.getUnprocessedItems()
					);
				}
			}

		}

	}

}
