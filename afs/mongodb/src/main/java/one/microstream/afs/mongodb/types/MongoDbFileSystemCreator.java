package one.microstream.afs.mongodb.types;

/*-
 * #%L
 * microstream-afs-mongodb
 * %%
 * Copyright (C) 2019 - 2021 MicroStream Software
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

import org.bson.UuidRepresentation;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ReadConcern;
import com.mongodb.ReadConcernLevel;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import one.microstream.afs.blobstore.types.BlobStoreFileSystem;
import one.microstream.afs.types.AFileSystem;
import one.microstream.chars.XChars;
import one.microstream.configuration.exceptions.ConfigurationException;
import one.microstream.configuration.types.Configuration;
import one.microstream.configuration.types.ConfigurationBasedCreator;

public class MongoDbFileSystemCreator extends ConfigurationBasedCreator.Abstract<AFileSystem>
{
	public MongoDbFileSystemCreator()
	{
		super(AFileSystem.class);
	}
	
	@Override
	public AFileSystem create(
		final Configuration configuration
	)
	{
		final Configuration mongoConfiguration = configuration.child("mongodb");
		if(mongoConfiguration == null)
		{
			return null;
		}
		
		final String databaseName = mongoConfiguration.get("database");
		if(XChars.isEmpty(databaseName))
		{
			throw new ConfigurationException(mongoConfiguration, "MongoDB database must be defined");
		}
		
		final MongoClientSettings.Builder clientBuilder = MongoClientSettings.builder();
		
		mongoConfiguration.opt("application-name").ifPresent(
			value -> clientBuilder.applicationName(value)
		);

		mongoConfiguration.opt("connection-string").ifPresent(
			value -> clientBuilder.applyConnectionString(new ConnectionString(value))
		);
		
		mongoConfiguration.opt("read-concern").ifPresent(
			value -> clientBuilder.readConcern(new ReadConcern(ReadConcernLevel.fromString(value)))
		);
		
		mongoConfiguration.opt("read-preference").ifPresent(
			value -> clientBuilder.readPreference(ReadPreference.valueOf(value))
		);
		
		mongoConfiguration.opt("write-concern").ifPresent(
			value -> clientBuilder.writeConcern(WriteConcern.valueOf(value))
		);
		
		mongoConfiguration.optBoolean("retry-reads").ifPresent(
			value -> clientBuilder.retryReads(value)
		);
		
		mongoConfiguration.optBoolean("retry-writes").ifPresent(
			value -> clientBuilder.retryWrites(value)
		);
		
		mongoConfiguration.opt("uuid-representation").ifPresent(
			value -> clientBuilder.uuidRepresentation(UuidRepresentation.valueOf(value))
		);
		
		final Configuration credentialsConfig = mongoConfiguration.child("credentials");
		if(credentialsConfig != null)
		{
			clientBuilder.credential(
				this.createCredential(
					databaseName     ,
					credentialsConfig
				)
			);
		}
		
		final MongoClient      client    = MongoClients.create(clientBuilder.build());
		final MongoDatabase    database  = client.getDatabase(databaseName);
		final boolean          cache     = configuration.optBoolean("cache").orElse(true);
		final MongoDbConnector connector = cache
			? MongoDbConnector.Caching(database)
			: MongoDbConnector.New(database)
		;
		return BlobStoreFileSystem.New(connector);
	}

	private MongoCredential createCredential(
		final String        databaseName ,
		final Configuration configuration
	)
	{
		final MongoCredential credential;
		
		switch(configuration.opt("auth-mechanism").orElse("unspecified").toLowerCase())
		{
			case "gssapi":
			{
				credential = MongoCredential.createGSSAPICredential(
					configuration.get("username")
				);
			}
			break;
			
			case "plain":
			{
				credential = MongoCredential.createPlainCredential(
					configuration.get("username"),
					configuration.opt("source").orElse(databaseName),
					configuration.get("password").toCharArray()
				);
			}
			break;
			
			case "mongodb-x509":
			{
				credential = MongoCredential.createMongoX509Credential(
					configuration.get("username")
				);
			}
			break;
			
			case "mongo-cr":
			default:
			{
				credential = MongoCredential.createCredential(
					configuration.get("username"),
					databaseName,
					configuration.get("password").toCharArray()
				);
			}
			break;
		}
		
		final Configuration authPropsConfig = configuration.child("auth-mechanism-properties");
		if(authPropsConfig != null)
		{
			authPropsConfig.coalescedMap().entrySet().forEach(
				e -> credential.withMechanismProperty(e.getKey(), e.getValue())
			);
		}
		
		return credential;
	}
	
}
