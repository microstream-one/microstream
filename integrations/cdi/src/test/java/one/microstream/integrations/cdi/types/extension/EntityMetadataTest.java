
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

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


class EntityMetadataTest
{
	@Test
	public void shouldReturnEmptyFields()
	{
		final EntityMetadata metadata = EntityMetadata.of(Cat.class);
		Assertions.assertTrue(metadata.getFields().isEmpty());
	}
	
	@Test
	public void shouldReturnEmptyFields2()
	{
		final EntityMetadata metadata = EntityMetadata.of(Product.class);
		Assertions.assertTrue(metadata.getFields().isEmpty());
	}
	
	@Test
	public void shouldReturnIterable() throws NoSuchFieldException
	{
		final EntityMetadata metadata = EntityMetadata.of(Inventory.class);
		Assertions.assertFalse(metadata.getFields().isEmpty());
		final FieldMetadata field    = metadata.getFields().get(0);
		final Field         products = Inventory.class.getDeclaredField("products");
		Assertions.assertEquals(1, metadata.getFields().size());
		Assertions.assertEquals(products, field.get());
	}
	
	@Test
	public void shouldReturnMap() throws NoSuchFieldException
	{
		final EntityMetadata metadata = EntityMetadata.of(Contact.class);
		Assertions.assertFalse(metadata.getFields().isEmpty());
		final FieldMetadata field    = metadata.getFields().get(0);
		final Field         contacts = Contact.class.getDeclaredField("contacts");
		Assertions.assertEquals(contacts, field.get());
		Assertions.assertEquals(1, metadata.getFields().size());
	}
	
	@Test
	@DisplayName("Should return both Map and Iterable")
	public void shouldReturnMapAndIterable() throws NoSuchFieldException
	{
		final EntityMetadata metadata = EntityMetadata.of(MediaUser.class);
		Assertions.assertFalse(metadata.getFields().isEmpty());
		Assertions.assertEquals(2, metadata.getFields().size());
		final FieldMetadata fieldA             = metadata.getFields().get(0);
		final FieldMetadata fieldB             = metadata.getFields().get(1);
		final Field         medias             = MediaUser.class.getDeclaredField("medias");
		final Field         postsBySocialMedia = MediaUser.class.getDeclaredField("postsBySocialMedia");
		Assertions.assertEquals(fieldA.get(), medias);
		Assertions.assertEquals(fieldB.get(), postsBySocialMedia);
	}
	
	@Test
	public void shouldReturnNPEWhenIsNull()
	{
		final EntityMetadata metadata = EntityMetadata.of(MediaUser.class);
		Assertions.assertThrows(NullPointerException.class, () -> metadata.values(null, new String[0]));
	}
	
	@Test
	public void shouldReturnIllegalErrorWhenTypesAreIncompatibles()
	{
		final EntityMetadata metadata = EntityMetadata.of(MediaUser.class);
		Assertions.assertThrows(IllegalArgumentException.class, () -> metadata.values(new Cat(), new String[0]));
	}
	
	@Test
	public void shouldReturnValues()
	{
		final EntityMetadata      metadata           = EntityMetadata.of(MediaUser.class);
		final String              user               = "Otavio";
		final Set<String>         medias             = null;
		final Map<String, String> postsBySocialMedia = Map.of("otaviojava", "my post", "otavio", "my photo");
		final MediaUser           mediaUser          = new MediaUser(user, medias, postsBySocialMedia);
		final List<Object>        values             = metadata.values(mediaUser, new String[0]).collect(Collectors.toList());
		
		Assertions.assertEquals(1, values.size());
		Assertions.assertEquals(postsBySocialMedia, values.get(0));
	}
	
	@Test
	public void shouldReturnFilter()
	{
		final EntityMetadata      metadata           = EntityMetadata.of(MediaUser.class);
		final String              user               = "Otavio";
		final Set<String>         medias             = Set.of("Twitter", "Facebook");
		final Map<String, String> postsBySocialMedia = Map.of("otaviojava", "my post", "otavio", "my photo");
		final MediaUser           mediaUser          = new MediaUser(user, medias, postsBySocialMedia);
		final List<Object>        values             =
			metadata.values(mediaUser, new String[]{"medias"}).collect(Collectors.toList());
		Assertions.assertEquals(1, values.size());
		Assertions.assertEquals(medias, values.get(0));
	}
	
	@Test
	public void shouldReturnFilter2()
	{
		final EntityMetadata      metadata           = EntityMetadata.of(MediaUser.class);
		final String              user               = "Otavio";
		final Set<String>         medias             = Set.of("Twitter", "Facebook");
		final Map<String, String> postsBySocialMedia = Map.of("otaviojava", "my post", "otavio", "my photo");
		final MediaUser           mediaUser          = new MediaUser(user, medias, postsBySocialMedia);
		final List<Object>        values             =
			metadata.values(mediaUser, new String[]{"postsBySocialMedia"}).collect(Collectors.toList());
		Assertions.assertEquals(1, values.size());
		Assertions.assertEquals(postsBySocialMedia, values.get(0));
	}
	
	@Test
	public void shouldReturnEmptyWhenIsNotIterableOrMap()
	{
		final EntityMetadata      metadata           = EntityMetadata.of(MediaUser.class);
		final String              user               = "Otavio";
		final Set<String>         medias             = Set.of("Twitter", "Facebook");
		final Map<String, String> postsBySocialMedia = Map.of("otaviojava", "my post", "otavio", "my photo");
		final MediaUser           mediaUser          = new MediaUser(user, medias, postsBySocialMedia);
		final List<Object>        values             =
			metadata.values(mediaUser, new String[]{"user"}).collect(Collectors.toList());
		Assertions.assertTrue(values.isEmpty());
	}
	
	@Test
	public void shouldIgnoreNonIterableOrMap()
	{
		final EntityMetadata      metadata           = EntityMetadata.of(MediaUser.class);
		final String              user               = "Otavio";
		final Set<String>         medias             = Set.of("Twitter", "Facebook");
		final Map<String, String> postsBySocialMedia = Map.of("otaviojava", "my post", "otavio", "my photo");
		final MediaUser           mediaUser          = new MediaUser(user, medias, postsBySocialMedia);
		final List<Object>        values             =
			metadata.values(mediaUser, new String[]{"user", "medias"}).collect(Collectors.toList());
		Assertions.assertEquals(1, values.size());
		Assertions.assertEquals(medias, values.get(0));
	}
	
	@Test
	@DisplayName("Should return all Iterable and map fields when it uses the default annotation")
	public void shouldReturnAllFieldsOnDefault()
	{
		final EntityMetadata      metadata           = EntityMetadata.of(MediaUser.class);
		final String              user               = "Otavio";
		final Set<String>         medias             = Set.of("Twitter", "Facebook");
		final Map<String, String> postsBySocialMedia = Map.of("otaviojava", "my post", "otavio", "my photo");
		final MediaUser           mediaUser          = new MediaUser(user, medias, postsBySocialMedia);
		final List<Object>        values             =
			metadata.values(mediaUser, new String[]{""}).collect(Collectors.toList());
		Assertions.assertEquals(2, values.size());
		Assertions.assertEquals(medias, values.get(0));
		Assertions.assertEquals(postsBySocialMedia, values.get(1));
	}
}
