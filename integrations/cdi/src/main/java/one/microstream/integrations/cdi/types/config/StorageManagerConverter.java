
package one.microstream.integrations.cdi.types.config;

/*-
 * #%L
 * microstream-integrations-cdi
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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.microprofile.config.spi.Converter;

import one.microstream.storage.embedded.configuration.types.EmbeddedStorageConfiguration;
import one.microstream.storage.types.StorageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A Config converter to {@link StorageManager}
 */
public class StorageManagerConverter implements Converter<StorageManager>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(StorageManagerConverter.class);
	
	private static final Map<String, StorageManager> MAP    = new ConcurrentHashMap<>();
	
	@Override
	public StorageManager convert(final String value) throws IllegalArgumentException, NullPointerException
	{
		return MAP.computeIfAbsent(value, this::createStorageManager);
	}
	
	private StorageManager createStorageManager(final String value)
	{
		LOGGER.info("Loading configuration to start the class StorageManager from the key: " + value);
		return EmbeddedStorageConfiguration.load(value)
			.createEmbeddedStorageFoundation()
			.createEmbeddedStorageManager()
			.start()
		;
	}
	
}
