package one.microstream.afs.azure.storage.types;

/*-
 * #%L
 * microstream-afs-azure-storage
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

import com.azure.core.credential.BasicAuthenticationCredential;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;

import one.microstream.afs.blobstore.types.BlobStoreFileSystem;
import one.microstream.afs.types.AFileSystem;
import one.microstream.configuration.types.Configuration;
import one.microstream.configuration.types.ConfigurationBasedCreator;

public class AzureStorageFileSystemCreator extends ConfigurationBasedCreator.Abstract<AFileSystem>
{
	public AzureStorageFileSystemCreator()
	{
		super(AFileSystem.class);
	}

	@Override
	public AFileSystem create(
		final Configuration configuration
	)
	{
		final Configuration azureConfiguration = configuration.child("azure.storage");
		if(azureConfiguration == null)
		{
			return null;
		}

		final BlobServiceClientBuilder clientBuilder = new BlobServiceClientBuilder();

		azureConfiguration.opt("endpoint").ifPresent(
			value -> clientBuilder.endpoint(value)
		);

		azureConfiguration.opt("connection-string").ifPresent(
			value -> clientBuilder.connectionString(value)
		);

		azureConfiguration.opt("encryption-scope").ifPresent(
			value -> clientBuilder.encryptionScope(value)
		);

		azureConfiguration.opt("credentials.type").ifPresent(credentialsType ->
		{
			switch(credentialsType)
			{
				case "basic":
				{
					clientBuilder.credential(
						new BasicAuthenticationCredential(
							azureConfiguration.get("credentials.username"),
							azureConfiguration.get("credentials.password")
						)
					);
				}
				break;

				case "shared-key":
				{
					clientBuilder.credential(
						new StorageSharedKeyCredential(
							azureConfiguration.get("credentials.account-name"),
							azureConfiguration.get("credentials.account-key")
						)
					);
				}
				break;

				default:
					// no credentials provider is used if not explicitly set
			}
		});

		final Configuration furtherConfiguration = azureConfiguration.child("configuration");
		if(furtherConfiguration != null)
		{
			final com.azure.core.util.Configuration config = new com.azure.core.util.Configuration();
			for(final String key : furtherConfiguration.keys())
			{
				final String value = furtherConfiguration.get(key);
				config.put(key, value);
			}
			clientBuilder.configuration(config);
		}

		final BlobServiceClient     client    = clientBuilder.buildClient();
		final boolean               cache     = configuration.optBoolean("cache").orElse(true);
		final AzureStorageConnector connector = cache
			? AzureStorageConnector.Caching(client)
			: AzureStorageConnector.New(client)
		;
		return BlobStoreFileSystem.New(connector);
	}

}
