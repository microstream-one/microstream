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

import java.util.Properties;

import one.microstream.afs.blobstore.types.BlobStoreFileSystem;
import one.microstream.afs.types.AFileSystem;
import one.microstream.configuration.types.Configuration;
import one.microstream.configuration.types.ConfigurationBasedCreator;

public class KafkaFileSystemCreator extends ConfigurationBasedCreator.Abstract<AFileSystem>
{
	public KafkaFileSystemCreator()
	{
		super(AFileSystem.class);
	}
	
	@Override
	public AFileSystem create(
		final Configuration configuration
	)
	{
		final Configuration kafkaConfiguration = configuration.child("kafka-properties");
		if(kafkaConfiguration == null)
		{
			return null;
		}
		
		final Properties     kafkaProperties = new Properties();
		kafkaProperties.putAll(kafkaConfiguration.coalescedMap());
		final boolean        cache           = configuration.optBoolean("cache").orElse(true);
		final KafkaConnector connector       = cache
			? KafkaConnector.Caching(kafkaProperties)
			: KafkaConnector.New(kafkaProperties)
		;
		return BlobStoreFileSystem.New(connector);
	}
	
}
