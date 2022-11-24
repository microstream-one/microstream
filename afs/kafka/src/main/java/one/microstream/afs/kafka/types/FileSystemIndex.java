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

import static one.microstream.X.notNull;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

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
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

public interface FileSystemIndex extends AutoCloseable
{
	public Stream<String> files();

	public FileSystemIndex put(String file);

	public FileSystemIndex delete(String file);

	@Override
	public void close();


	public static FileSystemIndex New(
		final Properties kafkaProperties
	)
	{
		return new Default(
			notNull(kafkaProperties)
		);
	}


	public static class Default implements FileSystemIndex
	{
		private final Properties               kafkaProperties;
		private       Set<String>              files          ;
		private       Producer<String, String> producer       ;

		Default(
			final Properties kafkaProperties
		)
		{
			super();
			this.kafkaProperties = kafkaProperties;
		}

		private String topicName()
		{
			return "__filesystem_index";
		}

		private Set<String> ensureFiles()
		{
			if(this.files == null)
			{
				synchronized(this)
				{
					if(this.files == null)
					{
						this.files = this.createFiles();
					}
				}
			}

			return this.files;
		}

		private Set<String> createFiles()
		{
			final Set<String> files = new HashSet<>();

			final Properties properties = new Properties();
			properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG  , StringDeserializer.class.getName());
			properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
			properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG                , "filesystemindex" + UUID.randomUUID().toString());
			properties.remove(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG );
			properties.remove(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG);
			properties.putAll(this.kafkaProperties);

			try(final KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties))
			{
				final TopicPartition topicPartition = new TopicPartition(this.topicName(), 0);
				consumer.assign(Arrays.asList(topicPartition));
				consumer.seekToEnd(Arrays.asList(topicPartition));
				final long highestOffset = consumer.position(topicPartition) - 1;
				consumer.seekToBeginning(Arrays.asList(topicPartition));

				long lastReadOffset = -1L;
				while(lastReadOffset < highestOffset)
				{
					final ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
					for(final ConsumerRecord<String, String> record : records)
					{
						files.add(record.value());
						lastReadOffset = record.offset();
					}
				}
			}

			return files;
		}

		private Producer<String, String> ensureProducer()
		{
			if(this.producer == null)
			{
				final Properties properties = new Properties();
				properties.putAll(this.kafkaProperties);
				properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG  , StringSerializer.class.getName());
				properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
				this.producer = new KafkaProducer<>(properties);
			}

			return this.producer;
		}

		private void internalProduce(
			final Producer<String, String> producer,
			final String                   file
		)
		{
			try
			{
				producer.send(new ProducerRecord<>(this.topicName(), file)).get();
			}
			catch(final Exception e)
			{
				throw new RuntimeException(e);
			}
		}

		@Override
		public Stream<String> files()
		{
			final Set<String> files = this.ensureFiles();
			synchronized(files)
			{
				return new HashSet<>(files).stream();
			}
		}

		@Override
		public FileSystemIndex put(
			final String file
		)
		{
			final Set<String> files = this.ensureFiles();
			synchronized(files)
			{
				if(files.add(file))
				{
					final Producer<String, String> producer = this.ensureProducer();
					this.internalProduce(producer, file);
					producer.flush();
				}
			}

			return this;
		}

		@Override
		public FileSystemIndex delete(
			final String file
		)
		{
			final Set<String> files = this.ensureFiles();
			synchronized(files)
			{
				if(files.remove(file))
				{
					final Map<TopicPartition, RecordsToDelete> recordsToDelete = new HashMap<>();
					recordsToDelete.put(
						new TopicPartition(this.topicName(), 0),
						RecordsToDelete.beforeOffset(0xFFFFFFFFFFFFFFFFL)
					);
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

					final Producer<String, String> producer = this.ensureProducer();
					files.forEach(f -> this.internalProduce(producer, f));
					producer.flush();
				}
			}

			return this;
		}

		@Override
		public synchronized void close()
		{
			if(this.files != null)
			{
				this.files.clear();
				this.files = null;
			}

			if(this.producer != null)
			{
				this.producer.close();
				this.producer = null;
			}
		}

	}

}
