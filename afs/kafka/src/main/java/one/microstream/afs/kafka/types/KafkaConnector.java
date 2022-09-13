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

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summarizingLong;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static one.microstream.X.checkArrayRange;
import static one.microstream.X.notNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.kafka.clients.admin.AdminClient;
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

import one.microstream.afs.blobstore.types.BlobStoreConnector;
import one.microstream.afs.blobstore.types.BlobStorePath;
import one.microstream.collections.BulkList;
import one.microstream.collections.EqHashTable;
import one.microstream.exceptions.IORuntimeException;
import one.microstream.io.ByteBufferInputStream;
import one.microstream.io.LimitedInputStream;

/**
 * Connector for <a href="https://kafka.apache.org/">Apache Kafka</a>.
 * <p>
 * First setup the <a href="https://kafka.apache.org/documentation/#api">connection properties</a>.
 * <pre>
 * Properties properties = ...
 * BlobStoreFileSystem fileSystem = BlobStoreFileSystem.New(
 * 	KafkaConnector.New(properties)
 * );
 * </pre>
 *
 * 
 *
 */
public interface KafkaConnector extends BlobStoreConnector
{
	/**
	 * Pseudo-constructor method which creates a new {@link KafkaConnector}.
	 *
	 * @param kafkaProperties the Kafka configuration
	 * @return a new {@link KafkaConnector}
	 */
	public static KafkaConnector New(
		final Properties kafkaProperties
	)
	{
		return new Default(
			notNull(kafkaProperties),
			false
		);
	}

	/**
	 * Pseudo-constructor method which creates a new {@link KafkaConnector} with cache.
	 *
	 * @param kafkaProperties the Kafka configuration
	 * @return a new {@link KafkaConnector}
	 */
	public static KafkaConnector Caching(
		final Properties kafkaProperties
	)
	{
		return new Default(
			notNull(kafkaProperties),
			true
		);
	}


	public static class Default
	extends    BlobStoreConnector.Abstract<Blob>
	implements KafkaConnector
	{
		static String topicName(
			final BlobStorePath path
		)
		{
			return Pattern.compile("[^a-zA-Z0-9\\._\\-]")
				.matcher(path.fullQualifiedName().replace(BlobStorePath.SEPARATOR_CHAR, '_'))
				.replaceAll("_")
			;
		}

		private final Properties                                         kafkaProperties;
		private final FileSystemIndex                                    fileSystemIndex;
		private final EqHashTable<String, TopicIndex>                    topicIndices   ;
		private final EqHashTable<String, KafkaConsumer<String, byte[]>> kafkaConsumers ;
		private final EqHashTable<String, KafkaProducer<String, byte[]>> kafkaProducers ;

		Default(
			final Properties kafkaProperties,
			final boolean    withCache
		)
		{
			super(
				Blob::topic,
				Blob::size,
				KafkaPathValidator.New(),
				withCache
			);
			this.kafkaProperties = kafkaProperties  ;
			this.fileSystemIndex = FileSystemIndex.New(kafkaProperties);
			this.topicIndices    = EqHashTable.New();
			this.kafkaConsumers  = EqHashTable.New();
			this.kafkaProducers  = EqHashTable.New();
		}

		private synchronized TopicIndex topicIndex(
			final BlobStorePath path
		)
		{
			return this.topicIndices.ensure(
				topicName(path),
				topic -> TopicIndex.New(this.kafkaProperties, topic)
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
			final Iterable<Blob> iterable = this.topicIndex(file).get();
			return iterable != null
				? StreamSupport.stream(iterable.spliterator(), false)
				: Stream.empty()
			;
		}

		@Override
		protected Stream<String> childKeys(
			final BlobStorePath directory
		)
		{
			final Pattern pattern = Pattern.compile(childKeysRegexWithContainer(directory));
			return this.fileSystemIndex.files()
				.filter(key -> pattern.matcher(key).matches())
			;
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
			final Blob          blob        ,
			final ByteBuffer    targetBuffer,
			final long          offset      ,
			final long          length
		)
		{
			final KafkaConsumer<String, byte[]> consumer = this.consumer(blob.topic());

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
			while((records = consumer.poll(Duration.ofSeconds(3))).isEmpty())
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
				try(AdminClient admin = AdminClient.create(this.kafkaProperties))
				{
					admin.deleteTopics(Arrays.asList(
						topicName,
						TopicIndex.Default.indexTopicName(topicName)
					))
					.all()
					.get();
				}

				synchronized(this)
				{
					this.fileSystemIndex.delete(file.fullQualifiedName());
					Optional.ofNullable(this.topicIndices  .removeFor(topicName)).ifPresent(TopicIndex   ::close);
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
			 * Writes remaining blobs into topic and deletes all before current offsets.
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
			 * Read data.
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

			/*
			 * Delete old data
			 */
			try(AdminClient admin = AdminClient.create(this.kafkaProperties))
			{
				admin.deleteRecords(deletionMap)
					.all()
					.get();
			}
			catch(final Exception e)
			{
				throw new RuntimeException(e);
			}
			synchronized(this)
			{
				final TopicIndex index = this.topicIndices.get(topicName);
				if(index != null)
				{
					index.delete(deletionMap);
				}
			}

			/*
			 * Write remaining
			 */
			this.internalWriteData(file, buffers);

			return true;
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
					final long currentBatchSize = Math.min(
						available,
						1_000_000
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
					}

					available -= currentBatchSize;
					offset    += currentBatchSize;
				}

				producer.flush();

				this.fileSystemIndex.put(file.fullQualifiedName());
				this.topicIndex(file).put(blobs);

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
		protected synchronized void internalClose()
		{
			this.fileSystemIndex.close();
			this.topicIndices.values().forEach(TopicIndex::close);
			this.topicIndices.clear();
			this.kafkaConsumers.values().forEach(KafkaConsumer::close);
			this.kafkaConsumers.clear();
			this.kafkaProducers.values().forEach(KafkaProducer::close);
			this.kafkaProducers.clear();
		}

	}

}
