package one.microstream.afs.hazelcast.types;

/*-
 * #%L
 * microstream-afs-hazelcast
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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.hazelcast.config.ClasspathXmlConfig;
import com.hazelcast.config.ClasspathYamlConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.FileSystemXmlConfig;
import com.hazelcast.config.FileSystemYamlConfig;
import com.hazelcast.config.UrlXmlConfig;
import com.hazelcast.config.UrlYamlConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import one.microstream.afs.blobstore.types.BlobStoreFileSystem;
import one.microstream.afs.types.AFileSystem;
import one.microstream.chars.XChars;
import one.microstream.configuration.exceptions.ConfigurationException;
import one.microstream.configuration.types.Configuration;
import one.microstream.configuration.types.ConfigurationBasedCreator;

public class HazelcastFileSystemCreator extends ConfigurationBasedCreator.Abstract<AFileSystem>
{
	private final static String CLASSPATH_PREFIX = "classpath:";
	
	public HazelcastFileSystemCreator()
	{
		super(AFileSystem.class);
	}
	
	@Override
	public AFileSystem create(
		final Configuration configuration
	)
	{
		final String hazelcastConfigPath = configuration.get("hazelcast.configuration");
		if(XChars.isEmpty(hazelcastConfigPath))
		{
			return null;
		}
		
		final HazelcastInstance  hazelcast = Hazelcast.newHazelcastInstance(
			this.loadHazelcastConfig(configuration, hazelcastConfigPath)
		);
		final boolean            cache     = configuration.optBoolean("cache").orElse(true);
		final HazelcastConnector connector = cache
			? HazelcastConnector.Caching(hazelcast)
			: HazelcastConnector.New(hazelcast)
		;
		return BlobStoreFileSystem.New(connector);
	}
	
	private Config loadHazelcastConfig(
		final Configuration configuration,
		final String        path
	)
	{
		if(path.equalsIgnoreCase("default"))
		{
			return Config.load();
		}
		
		final boolean xml = path.toLowerCase().endsWith(".xml");
		if(path.toLowerCase().startsWith(CLASSPATH_PREFIX))
		{
			return xml
				? new ClasspathXmlConfig(path.substring(CLASSPATH_PREFIX.length()))
				: new ClasspathYamlConfig(path.substring(CLASSPATH_PREFIX.length()))
			;
		}
		
		try
		{
			try
			{
				final URL url = new URL(path);
				return xml
					? new UrlXmlConfig(url)
					: new UrlYamlConfig(url)
				;
			}
			catch(final MalformedURLException e)
			{
				return xml
					? new FileSystemXmlConfig(new File(path))
					: new FileSystemYamlConfig(new File(path))
				;
			}
		}
		catch(final IOException ioe)
		{
			throw new ConfigurationException(configuration, ioe);
		}
	}
	
}
