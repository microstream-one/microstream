package one.microstream.afs.googlecloud.firestore.types;

/*-
 * #%L
 * microstream-afs-googlecloud-firestore
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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.api.gax.core.NoCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;

import one.microstream.afs.blobstore.types.BlobStoreFileSystem;
import one.microstream.afs.types.AFileSystem;
import one.microstream.chars.XChars;
import one.microstream.configuration.exceptions.ConfigurationException;
import one.microstream.configuration.types.Configuration;
import one.microstream.configuration.types.ConfigurationBasedCreator;

public class GoogleCloudFirestoreFileSystemCreator extends ConfigurationBasedCreator.Abstract<AFileSystem>
{
	private final static String CLASSPATH_PREFIX = "classpath:";
		
	public GoogleCloudFirestoreFileSystemCreator()
	{
		super(AFileSystem.class);
	}
	
	@Override
	public AFileSystem create(
		final Configuration configuration
	)
	{
		final Configuration firestoreConfiguration = configuration.child("googlecloud.firestore");
		if(firestoreConfiguration == null)
		{
			return null;
		}
		
		final FirestoreOptions.Builder optionsBuilder = FirestoreOptions.getDefaultInstance().toBuilder();
				
		firestoreConfiguration.opt("client-lib-token").ifPresent(
			value -> optionsBuilder.setClientLibToken(value)
		);
		
		firestoreConfiguration.opt("database-id").ifPresent(
			value -> optionsBuilder.setDatabaseId(value)
		);
		
		firestoreConfiguration.opt("emulator-host").ifPresent(
			value -> optionsBuilder.setEmulatorHost(value)
		);
		
		firestoreConfiguration.opt("host").ifPresent(
			value -> optionsBuilder.setHost(value)
		);
		
		firestoreConfiguration.opt("project-id").ifPresent(
			value -> optionsBuilder.setProjectId(value)
		);
		
		firestoreConfiguration.opt("quota-project-id").ifPresent(
			value -> optionsBuilder.setQuotaProjectId(value)
		);
				
		this.createCredentials(
			firestoreConfiguration,
			optionsBuilder
		);
		
		final Firestore                     firestore = optionsBuilder.build().getService();
		final boolean                       cache     = configuration.optBoolean("cache").orElse(true);
		final GoogleCloudFirestoreConnector connector = cache
			? GoogleCloudFirestoreConnector.Caching(firestore)
			: GoogleCloudFirestoreConnector.New(firestore)
		;
		return BlobStoreFileSystem.New(connector);
	}

	private void createCredentials(
		final Configuration            configuration,
		final FirestoreOptions.Builder optionsBuilder
	)
	{
		configuration.opt("credentials.type").ifPresent(credentialsProvider ->
		{
			switch(credentialsProvider)
			{
				case "none":
				{
					optionsBuilder.setCredentialsProvider(NoCredentialsProvider.create());
				}
				break;

				case "input-stream":
				{
					final String inputStreamPath = configuration.get("credentials.input-stream");
					if(XChars.isEmpty(inputStreamPath))
					{
						throw new ConfigurationException(
							configuration,
							"googlecloud.firestore.credentials.input-stream must be defined when " +
							"googlecloud.firestore.credentials.type=input-stream"
						);
					}
					
					try
					{
						optionsBuilder.setCredentials(
							GoogleCredentials.fromStream(
								this.createInputStream(
									configuration,
									inputStreamPath
								)
							)
						);
					}
					catch(final IOException e)
					{
						throw new ConfigurationException(configuration, e);
					}
				}
				break;
				
				case "default":
				default:
				{
					try
					{
						optionsBuilder.setCredentials(GoogleCredentials.getApplicationDefault());
					}
					catch(final IOException e)
					{
						throw new ConfigurationException(configuration, e);
					}
				}
				break;
			}
		});
	}
	
	private InputStream createInputStream(
		final Configuration configuration,
		final String        path
	)
	throws IOException
	{
		if(path.toLowerCase().startsWith(CLASSPATH_PREFIX))
		{
			return this.getClass().getResourceAsStream(path.substring(CLASSPATH_PREFIX.length()));
		}
	
		try
		{
			final URL url = new URL(path);
			return url.openStream();
		}
		catch(final MalformedURLException e)
		{
			return new FileInputStream(path);
		}
	}
	
}
