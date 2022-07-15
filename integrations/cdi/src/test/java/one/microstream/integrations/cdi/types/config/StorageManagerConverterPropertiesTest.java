
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

import io.smallrye.config.inject.ConfigExtension;
import one.microstream.integrations.cdi.types.logging.TestLogger;
import one.microstream.storage.types.StorageManager;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;

import javax.inject.Inject;
import java.io.File;
import java.util.List;


@EnableAutoWeld
@AddExtensions(ConfigExtension.class)  // SmallRye Config extension to Support MicroProfile Config within this test
@DisplayName("Check if the Storage Manager will load using a property file")
public class StorageManagerConverterPropertiesTest extends AbstractStorageManagerConverterTest
{
	@Inject
	@ConfigProperty(name = "one.microstream.properties")
	private StorageManager manager;

	@Test
	public void shouldLoadFromProperties()
	{
		Assertions.assertNotNull(this.manager);
		final List<LoggingEvent> messages = TestLogger.getMessagesOfLevel(Level.INFO);

		hasMessage(messages, "Loading configuration to start the class StorageManager from the key: storage.properties");
		hasMessage(messages, "Embedded storage manager initialized");

		this.directoryHasChannels(new File("target/prop"), 2);
	}
}
