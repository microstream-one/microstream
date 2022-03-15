
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;


class EntityMetadata
{
	private final Class<?>            type  ;
	private final List<FieldMetadata> fields;
	
	private EntityMetadata(final Class<?> type, final List<FieldMetadata> fields)
	{
		this.type   = type  ;
		this.fields = fields;
	}
	
	public List<FieldMetadata> getFields()
	{
		return Collections.unmodifiableList(this.fields);
	}
	
	public Stream<Object> values(final Object entity, final String[] filters)
	{
		Objects.requireNonNull(entity, "entity is required");
		Objects.requireNonNull(this.fields, "fields is required");
		if(!this.type.equals(entity.getClass()))
		{
			throw new IllegalArgumentException(
				String.format("The entity %s is not compatible with the metadata %s", entity.getClass(), this.type)
			);
		}
		return this.getFields(filters)
			.map(f -> f.read(entity))
			.filter(Objects::nonNull)
		;
	}
	
	private Stream<FieldMetadata> getFields(final String[] filters)
	{
		if(this.isFieldsEmpty(filters))
		{
			return this.fields.stream();
		}
		
		final List<String>             storeFields = Stream.of(filters).sorted().collect(Collectors.toList());
		final Predicate<FieldMetadata> find        = f -> Collections.binarySearch(storeFields, f.getName()) >= 0;
		return this.getFields().stream().filter(find);
	}
	
	private boolean isFieldsEmpty(final String[] filters)
	{
		return Stream.of(filters).allMatch(s -> s == null || s.isBlank());
	}
	
	@Override
	public boolean equals(final Object o)
	{
		if(this == o)
		{
			return true;
		}
		if(o == null || this.getClass() != o.getClass())
		{
			return false;
		}
		final EntityMetadata that = (EntityMetadata)o;
		return Objects.equals(this.type, that.type) && Objects.equals(this.fields, that.fields);
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(this.type, this.fields);
	}
	
	static <T> EntityMetadata of(final Class<T> entity)
	{
		final List<FieldMetadata> fields = new ArrayList<>();
		for(final Field field : entity.getDeclaredFields())
		{
			final Class<?> type = field.getType();
			if(isLazyFields(type))
			{
				field.setAccessible(true);
				fields.add(FieldMetadata.of(field));
			}
		}
		return new EntityMetadata(entity, fields);
	}
	
	/**
	 * Returns if the is lazy, so is {@link Iterable} and {@link Map} type.
	 *
	 * @param type
	 *            the entity type
	 * @return if it is a Lazy field or not
	 * @see one.microstream.integrations.cdi.types.StoreType
	 */
	private static boolean isLazyFields(final Class<?> type)
	{
		return Iterable.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type);
	}
}
