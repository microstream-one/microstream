package one.microstream.afs.kafka;

import static one.microstream.X.notNull;
import static one.microstream.chars.XChars.notEmpty;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.UUID;

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

public interface Index extends AutoCloseable
{
	public Iterable<Blob> get();

	public Index put(Iterable<Blob> blobs);

	@Override
	public void close();


    public static Index New(
		final Properties kafkaProperties,
		final String     key
	)
    {
    	return new Index.Default(
    		notNull (kafkaProperties),
    		notEmpty(key)
    	);
    }


	public static class Default implements Index
	{
		static String topicName(final String key)
		{
			return "__" + key + "_index";
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
		private final String                   key            ;
		private       BulkList<Blob>           blobs          ;
		private       Producer<String, byte[]> producer       ;

		Default(
			final Properties kafkaProperties,
			final String     key
		)
		{
			super();
			this.kafkaProperties = kafkaProperties;
			this.key             = key            ;
		}

		private String topicName()
		{
			return topicName(this.key);
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
			properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG                , "index" + UUID.randomUUID().toString());
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

				long lastReadOffset = 0L;
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
							this.key,
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
		public Index put(
			final Iterable<Blob> metadata
		)
		{
			final BulkList<Blob> blobs = this.ensureBlobs();
			synchronized(blobs)
			{
				final String                   topic    = this.topicName();
				final Producer<String, byte[]> producer = this.ensureProducer();
				metadata.forEach(blob ->
				{
					blobs.add(blob);

					final byte[] bytes = new byte[28];
					putInt (bytes,  0, blob.partition());
					putLong(bytes,  4, blob.offset   ());
					putLong(bytes, 12, blob.start    ());
					putLong(bytes, 20, blob.end      ());

					try
					{
						producer.send(new ProducerRecord<>(topic, bytes)).get();
					}
					catch(final Exception e)
					{
						throw new RuntimeException(e);
					}
				});
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
