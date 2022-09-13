package one.microstream.afs.aws.types;

/*-
 * #%L
 * microstream-afs-aws
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

import java.net.URI;
import java.net.URISyntaxException;

import one.microstream.afs.types.AFileSystem;
import one.microstream.configuration.exceptions.ConfigurationException;
import one.microstream.configuration.types.Configuration;
import one.microstream.configuration.types.ConfigurationBasedCreator;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.auth.credentials.SystemPropertyCredentialsProvider;
import software.amazon.awssdk.awscore.client.builder.AwsClientBuilder;
import software.amazon.awssdk.regions.Region;

public abstract class AwsFileSystemCreator extends ConfigurationBasedCreator.Abstract<AFileSystem>
{
	protected AwsFileSystemCreator()
	{
		super(AFileSystem.class);
	}

	protected void populateBuilder(
		final AwsClientBuilder<?, ?> clientBuilder,
		final Configuration          configuration
	)
	{
		configuration.opt("endpoint-override").ifPresent(endpointOverride ->
		{
			try
			{
				clientBuilder.endpointOverride(new URI(endpointOverride));
			}
			catch(final URISyntaxException e)
			{
				throw new ConfigurationException(configuration, e);
			}
		});
		configuration.opt("region").ifPresent(
			region -> clientBuilder.region(Region.of(region))
		);
		configuration.opt("credentials.type").ifPresent(credentialsType ->
		{
			switch(credentialsType)
			{
				case "environment-variables":
				{
					clientBuilder.credentialsProvider(EnvironmentVariableCredentialsProvider.create());
				}
				break;

				case "system-properties":
				{
					clientBuilder.credentialsProvider(SystemPropertyCredentialsProvider.create());
				}
				break;

				case "static":
				{
					clientBuilder.credentialsProvider(
						StaticCredentialsProvider.create(
							AwsBasicCredentials.create(
								configuration.get("credentials.access-key-id"),
								configuration.get("credentials.secret-access-key")
							)
						)
					);
				}
				break;

				case "default":
				{
					clientBuilder.credentialsProvider(DefaultCredentialsProvider.create());
				}
				break;

				default:
					// no credentials provider is used if not explicitly set
			}
		});
	}

}
