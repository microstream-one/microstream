
package one.microstream.integrations.cdi.types.extension;

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

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class FieldMetadataTest
{
	@Test
	public void shouldReturnIterable()
	{
		final Inventory inventory = new Inventory("my inventory");
		inventory.add(new Product("Banana", "A fruit", 5));
		final EntityMetadata      metadata = EntityMetadata.of(Inventory.class);
		final List<FieldMetadata> fields   = metadata.getFields();
		final FieldMetadata       field    = fields.get(0);
		final Object              read     = field.read(inventory);
		Assertions.assertEquals(inventory.getProducts(), read);
	}
	
	@Test
	public void shouldReturnMap()
	{
		final Map<String, String> contacts = new HashMap<>();
		contacts.put("Otavio", "123 456789");
		contacts.put("Poliana", "723 456789");
		final Contact             contact  = new Contact(LocalDate.now(), "Ada", contacts);
		final EntityMetadata      metadata = EntityMetadata.of(Contact.class);
		final List<FieldMetadata> fields   = metadata.getFields();
		final FieldMetadata       field    = fields.get(0);
		final Object              read     = field.read(contact);
		Assertions.assertEquals(contacts, read);
	}
	
	@Test
	public void shouldReturnBoth()
	{
		final String              user               = "Otavio";
		final Set<String>         medias             = Set.of("Twitter", "Instagram");
		final Map<String, String> postsBySocialMedia = Map.of("otaviojava", "my post", "otavio", "my photo");
		final MediaUser           mediaUser          = new MediaUser(user, medias, postsBySocialMedia);
		final EntityMetadata      metadata           = EntityMetadata.of(MediaUser.class);
		final List<FieldMetadata> fields             = metadata.getFields();
		final FieldMetadata       fieldA             = fields.get(0);
		final FieldMetadata       fieldB             = fields.get(1);
		
		Assertions.assertEquals(medias, fieldA.read(mediaUser));
		Assertions.assertEquals(postsBySocialMedia, fieldB.read(mediaUser));
	}
}
