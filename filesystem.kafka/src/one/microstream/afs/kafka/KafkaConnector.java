package one.microstream.afs.kafka;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summarizingLong;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static one.microstream.X.checkArrayRange;
import static one.microstream.X.notNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.RecordsToDelete;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import one.microstream.afs.blobstore.BlobStoreConnector;
import one.microstream.afs.blobstore.BlobStorePath;
import one.microstream.collections.BulkList;
import one.microstream.collections.EqHashTable;
import one.microstream.exceptions.IORuntimeException;
import one.microstream.io.ByteBufferInputStream;


public interface KafkaConnector extends BlobStoreConnector
{

	public static KafkaConnector New(
		final Properties kafkaProperties
	)
	{
		return new Default(
			notNull(kafkaProperties)
		);
	}


	public static class Default
	extends    BlobStoreConnector.Abstract<Blob>
	implements KafkaConnector
	{
		private static String topicName(
			final BlobStorePath file
		)
		{
			return file.fullQualifiedName().replace(BlobStorePath.SEPARATOR_CHAR, '_');
		}


		private final Properties                                         kafkaProperties;
		private final EqHashTable<String, Index>                         indices        ;
		private final EqHashTable<String, KafkaConsumer<String, byte[]>> kafkaConsumers ;
		private final EqHashTable<String, KafkaProducer<String, byte[]>> kafkaProducers ;

		Default(
			final Properties kafkaProperties
		)
		{
			super(
				Blob::key,
				Blob::size
			);
			this.kafkaProperties = kafkaProperties  ;
			this.indices         = EqHashTable.New();
			this.kafkaConsumers  = EqHashTable.New();
			this.kafkaProducers  = EqHashTable.New();
		}

		private synchronized Index index(
			final BlobStorePath file
		)
		{
			return this.indices.ensure(
				topicName(file),
				n -> Index.New(this.kafkaProperties, n)
			);
		}

		private synchronized KafkaConsumer<String, byte[]> consumer(
			final String key
		)
		{
			return this.kafkaConsumers.ensure(
				key,
				this::createConsumer
			);
		}

		private KafkaConsumer<String, byte[]> createConsumer(
			final String key
		)
		{
			final Properties properties = new Properties();
			properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG  , StringDeserializer   .class.getName());
			properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
			properties.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG        , "1");
			properties.remove(ConsumerConfig.GROUP_ID_CONFIG          );
			properties.remove(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG );
			properties.remove(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG);
			properties.putAll(this.kafkaProperties);

			return new KafkaConsumer<>(properties);
		}

		private synchronized KafkaProducer<String, byte[]> producer(
			final String key
		)
		{
			return this.kafkaProducers.ensure(
				key,
				this::createProducer
			);
		}

		private KafkaProducer<String, byte[]> createProducer(
			final String key
		)
		{
			final Properties properties = new Properties();
			properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG  , StringSerializer   .class.getName());
			properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
			properties.putAll(this.kafkaProperties);

			return new KafkaProducer<>(properties);
		}

		@Override
		protected Stream<Blob> blobs(
			final BlobStorePath file
		)
		{
			final Iterable<Blob> iterable = this.index(file).get();
			return iterable != null
				? StreamSupport.stream(iterable.spliterator(), false)
				: Stream.empty()
			;
		}

		@Override
		protected void internalReadBlobData(
			final BlobStorePath file        ,
			final Blob          blob        ,
			final ByteBuffer    targetBuffer,
			final long          offset      ,
			final long          length
		)
		{
			final KafkaConsumer<String, byte[]> consumer = this.consumer(blob.key());

			final TopicPartition topicPartition = new TopicPartition(
				topicName(file),
				blob.partition()
			);
			consumer.assign(Arrays.asList(topicPartition));
			consumer.seek(
				topicPartition,
				blob.offset()
			);
			ConsumerRecords<String, byte[]> records;
			int count = 0;
			while((records = consumer.poll(Duration.ofSeconds(1))).isEmpty())
			{
				if(++count >= 3)
				{
					throw new RuntimeException("No data available for " + file.fullQualifiedName() +
						", offset=" + offset + ", length=" + length);
				}
			}

			final ConsumerRecord<String, byte[]> record = records.iterator().next();
			final byte[]                         value  = record.value();
			targetBuffer.put(
				value,
				checkArrayRange(offset),
				checkArrayRange(length)
			);
		}

		@Override
		protected boolean internalDeleteFile(
			final BlobStorePath file
		)
		{
			try
			{
				final String topicName = topicName(file);
				KafkaAdminClient.create(this.kafkaProperties)
					.deleteTopics(Arrays.asList(
						topicName,
						Index.Default.topicName(topicName)
					))
					.all()
					.get();

				synchronized(this)
				{
					Optional.ofNullable(this.indices       .removeFor(topicName)).ifPresent(Index        ::close);
					Optional.ofNullable(this.kafkaConsumers.removeFor(topicName)).ifPresent(KafkaConsumer::close);
					Optional.ofNullable(this.kafkaProducers.removeFor(topicName)).ifPresent(KafkaProducer::close);
				}

				return true;
			}
			catch(final Exception e)
			{
				throw new RuntimeException(e);
			}
		}

		@Override
		protected boolean internalDeleteBlobs(
			final BlobStorePath        file ,
			final List<? extends Blob> blobs
		)
		{
			/*
			 * Writes remaining blobs into topic and then deletes all before current offsets.
			 * Unfortunately there's no other way to get partial deletion done.
			 */

			/*
			 * First, remember current partition offsets.
			 */
			final String                               topicName   = topicName(file);
			final Map<TopicPartition, RecordsToDelete> deletionMap = blobs.stream()
				.collect(groupingBy(
					Blob::partition,
					summarizingLong(Blob::offset)
				))
				.entrySet()
				.stream()
				.collect(toMap(
					kv -> new TopicPartition(topicName, kv.getKey()),
					kv -> RecordsToDelete.beforeOffset(kv.getValue().getMax() + 1L)
				))
			;

			/*
			 * Write remaining blobs into topic.
			 */
			final List<ByteBuffer> buffers        = new ArrayList<>();
			final List<Blob>       remainingBlobs = this.blobs(file).collect(toList());
			remainingBlobs.removeAll(blobs);
			for(final Blob blob : remainingBlobs)
			{
				final ByteBuffer buffer = ByteBuffer.allocateDirect(checkArrayRange(blob.size()));
				this.internalReadBlobData(file, blob, buffer, 0L, blob.size());
				buffer.flip();
				buffers.add(buffer);
			}
			synchronized(this)
			{
				Optional.ofNullable(this.indices.removeFor(topicName)).ifPresent(Index::close);
			}
			this.internalWriteData(file, buffers);

			/*
			 * Delete old data
			 */
			try
			{
				KafkaAdminClient.create(this.kafkaProperties)
					.deleteRecords(deletionMap)
					.all()
					.get();

				return true;
			}
			catch(final Exception e)
			{
				throw new RuntimeException(e);
			}
		}

		@Override
		protected long internalWriteData(
			final BlobStorePath                  file         ,
			final Iterable<? extends ByteBuffer> sourceBuffers
		)
		{
			try
			{
				final KafkaProducer<String, byte[]> producer           = this.producer(file.fullQualifiedName());
				final String                        topic              = topicName(file);
				final long                          totalSize          = this.totalSize(sourceBuffers);
				final BulkList<Blob>                blobs              = BulkList.New();
				final ByteBufferInputStream         buffersInputStream = ByteBufferInputStream.New(sourceBuffers);
				      long                          available          = totalSize;
				      long                          offset             = this.fileSize(file);
				while(available > 0)
				{
					final long   currentBatchSize = Math.min(
						available,
						1_000_000
					);
					final byte[] batch            = new byte[checkArrayRange(currentBatchSize)];
					      int    read             = 0;
					do
					{
						read += buffersInputStream.read(batch, read, batch.length - read);
					}
					while(read < batch.length);

					final RecordMetadata metadata = producer.send(
						new ProducerRecord<>(topic, batch)
					)
					.get();

					blobs.add(Blob.New(
						topic,
						metadata.partition(),
						metadata.offset(),
						offset,
						offset + currentBatchSize - 1
					));

					available -= currentBatchSize;
					offset    += currentBatchSize;
				}

				this.index(file).put(blobs);

				return totalSize;
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
			catch(final Exception e)
			{
				throw new RuntimeException(e);
			}
		}

		@Override
		protected long internalCopyFile(
			final BlobStorePath sourceFile,
			final BlobStorePath targetFile
		)
		{
			final ByteBuffer buffer = this.readData(
				sourceFile,
				0L,
				this.fileSize(sourceFile)
			);
			return this.writeData(
				targetFile,
				Arrays.asList(buffer)
			);
		}

		@Override
		protected synchronized void internalClose()
		{
			this.indices.values().forEach(Index::close);
			this.indices.clear();
			this.kafkaConsumers.values().forEach(KafkaConsumer::close);
			this.kafkaConsumers.clear();
			this.kafkaProducers.values().forEach(KafkaProducer::close);
			this.kafkaProducers.clear();
		}

	}

}
