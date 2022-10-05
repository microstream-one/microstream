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

import static java.util.stream.Collectors.toMap;
import static one.microstream.X.notNull;
import static one.microstream.chars.XChars.notEmpty;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.RecordsToDelete;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import one.microstream.collections.BulkList;

public interface TopicIndex extends AutoCloseable
{
	public Iterable<Blob> get();

	public TopicIndex put(Iterable<Blob> blobs);

	public TopicIndex delete(Map<TopicPartition, RecordsToDelete> recordsToDelete);

	@Override
	public void close();


    public static TopicIndex New(
		final Properties kafkaProperties,
		final String     topic
	)
    {
    	return new TopicIndex.Default(
    		notNull (kafkaProperties),
    		notEmpty(topic)
    	);
    }


	public static class Default implements TopicIndex
	{
		static String indexTopicName(final String topic)
		{
			return "__" + topic + "_index";
		}

		static int getInt(
			final byte[] bytes ,
			final int    offset
		)
		{
	        return ( bytes[offset + 3] & 0xFF       ) +
	               ((bytes[offset + 2] & 0xFF) <<  8) +
	               ((bytes[offset + 1] & 0xFF) << 16) +
	               ( bytes[offset    ]         << 24)
	        ;
	    }

		static long getLong(
			final byte[] bytes,
			final int offset
		)
		{
	        return ( bytes[offset + 7] & 0xFFL       ) +
	               ((bytes[offset + 6] & 0xFFL) <<  8) +
	               ((bytes[offset + 5] & 0xFFL) << 16) +
	               ((bytes[offset + 4] & 0xFFL) << 24) +
	               ((bytes[offset + 3] & 0xFFL) << 32) +
	               ((bytes[offset + 2] & 0xFFL) << 40) +
	               ((bytes[offset + 1] & 0xFFL) << 48) +
	               ((long)bytes[offset]         << 56)
	        ;
	    }

		static void putInt(
			final byte[] bytes,
			final int offset,
			final int value
		)
		{
	        bytes[offset + 3] = (byte)  value        ;
	        bytes[offset + 2] = (byte) (value >>>  8);
	        bytes[offset + 1] = (byte) (value >>> 16);
	        bytes[offset    ] = (byte) (value >>> 24);
	    }

	    static void putLong(
	    	final byte[] bytes,
	    	final int offset,
	    	final long value
	    )
	    {
	        bytes[offset + 7] = (byte)  value        ;
	        bytes[offset + 6] = (byte) (value >>>  8);
	        bytes[offset + 5] = (byte) (value >>> 16);
	        bytes[offset + 4] = (byte) (value >>> 24);
	        bytes[offset + 3] = (byte) (value >>> 32);
	        bytes[offset + 2] = (byte) (value >>> 40);
	        bytes[offset + 1] = (byte) (value >>> 48);
	        bytes[offset    ] = (byte) (value >>> 56);
	    }


		private final Properties               kafkaProperties;
		private final String                   topic          ;
		private       BulkList<Blob>           blobs          ;
		private       Producer<String, byte[]> producer       ;

		Default(
			final Properties kafkaProperties,
			final String     topic
		)
		{
			super();
			this.kafkaProperties = kafkaProperties;
			this.topic           = topic          ;
		}

		private String topicName()
		{
			return indexTopicName(this.topic);
		}

		private BulkList<Blob> ensureBlobs()
		{
			if(this.blobs == null)
			{
				synchronized(this)
				{
					if(this.blobs == null)
					{
						this.blobs = this.createBlobs();
					}
				}
			}

			return this.blobs;
		}

		private BulkList<Blob> createBlobs()
		{
			final BulkList<Blob> blobs = BulkList.New();

			final Properties properties = new Properties();
			properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG  , StringDeserializer   .class.getName());
			properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
			properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG                , "topicindex" + UUID.randomUUID().toString());
			properties.remove(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG );
			properties.remove(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG);
			properties.putAll(this.kafkaProperties);

			try(final KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(properties))
			{
				final TopicPartition topicPartition = new TopicPartition(this.topicName(), 0);
				consumer.assign(Arrays.asList(topicPartition));
				consumer.seekToEnd(Arrays.asList(topicPartition));
				final long highestOffset = consumer.position(topicPartition) - 1;
				consumer.seekToBeginning(Arrays.asList(topicPartition));

				long lastReadOffset = -1L;
				while(lastReadOffset < highestOffset)
				{
					final ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofSeconds(1));
					for(final ConsumerRecord<String, byte[]> record : records)
					{
						final byte[] bytes     = record.value();
						final int    partition = getInt (bytes,  0);
						final long   offset    = getLong(bytes,  4);
						final long   start     = getLong(bytes, 12);
						final long   end       = getLong(bytes, 20);
						blobs.add(new Blob.Default(
							this.topic,
							partition,
							offset,
							start,
							end
						));

						lastReadOffset = record.offset();
					}
				}
			}

			return blobs;
		}

		private Producer<String, byte[]> ensureProducer()
		{
			if(this.producer == null)
			{
				final Properties properties = new Properties();
				properties.putAll(this.kafkaProperties);
				properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG  , StringSerializer   .class.getName());
				properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
				this.producer = new KafkaProducer<>(properties);
			}

			return this.producer;
		}

		private void internalProduce(
			final Producer<String, byte[]> producer,
			final Blob                     blob
		)
		{
			final byte[] bytes = new byte[28];
			putInt (bytes,  0, blob.partition());
			putLong(bytes,  4, blob.offset   ());
			putLong(bytes, 12, blob.start    ());
			putLong(bytes, 20, blob.end      ());

			try
			{
				producer.send(new ProducerRecord<>(this.topicName(), bytes)).get();
			}
			catch(final Exception e)
			{
				throw new RuntimeException(e);
			}
		}

		@Override
		public Iterable<Blob> get()
		{
			final BulkList<Blob> blobs = this.ensureBlobs();
			synchronized(blobs)
			{
				return blobs.immure();
			}
		}

		@Override
		public TopicIndex put(
			final Iterable<Blob> metadata
		)
		{
			final BulkList<Blob> blobs = this.ensureBlobs();
			synchronized(blobs)
			{
				final Producer<String, byte[]> producer = this.ensureProducer();
				metadata.forEach(blob ->
				{
					blobs.add(blob);
					this.internalProduce(producer, blob);
				});
				producer.flush();
			}

			return this;
		}

		@Override
		public TopicIndex delete(
			final Map<TopicPartition, RecordsToDelete> recordsToDelete
		)
		{
			final BulkList<Blob> blobs = this.ensureBlobs();
			synchronized(blobs)
			{
				final Map<Integer, RecordsToDelete> partitionMap = recordsToDelete
					.entrySet()
					.stream()
					.collect(toMap(
						e -> e.getKey().partition(),
						e -> e.getValue())
					)
				;
				final long removeCount = blobs.removeBy(blob ->
				{
					final RecordsToDelete records = partitionMap.get(blob.partition());
					return records != null
						&& records.beforeOffset() > blob.offset()
					;
				});
				if(removeCount > 0L)
				{
					try(AdminClient admin = AdminClient.create(this.kafkaProperties))
					{
						admin.deleteRecords(recordsToDelete)
							.all()
							.get()
						;
					}
					catch(final Exception e)
					{
						throw new RuntimeException(e);
					}

					final Producer<String, byte[]> producer = this.ensureProducer();
					blobs.forEach(blob -> this.internalProduce(producer, blob));
					producer.flush();
				}
			}

			return this;
		}

		@Override
		public synchronized void close()
		{
			if(this.blobs != null)
			{
				this.blobs.clear();
				this.blobs = null;
			}

			if(this.producer != null)
			{
				this.producer.close();
				this.producer = null;
			}
		}

	}


}
