
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;

import one.microstream.integrations.cdi.types.Storage;


/**
 * This extension will look for entities with the Storage and then load it.
 */
@ApplicationScoped
public class StorageExtension implements Extension
{
	
	private static final Logger                 LOGGER      = Logger.getLogger(StorageExtension.class.getName());
	
	private final Set<Class<?>>                 storageRoot = new HashSet<>();
	private final Map<Class<?>, EntityMetadata> entities    = new HashMap<>();
	
	<T> void loadEntity(@Observes @WithAnnotations({Storage.class}) final ProcessAnnotatedType<T> target)
	{
		final AnnotatedType<T> annotatedType = target.getAnnotatedType();
		if(annotatedType.isAnnotationPresent(Storage.class))
		{
			final Class<T> javaClass = target.getAnnotatedType().getJavaClass();
			this.storageRoot.add(javaClass);
			LOGGER.info("New class found annotated with @Storage is " + javaClass);
		}
	}
	
	void onAfterBeanDiscovery(@Observes final AfterBeanDiscovery afterBeanDiscovery, final BeanManager beanManager)
	{
		LOGGER.info(String.format("Processing StorageExtension:  %d found", this.storageRoot.size()));
		if(this.storageRoot.size() > 1)
		{
			throw new IllegalStateException(
				"In the application must have only a class with the Storage annotation, classes: "
					+ this.storageRoot);
		}
		this.storageRoot.forEach(entity ->
		{
			final StorageBean<?> bean = new StorageBean<>(beanManager, entity);
			afterBeanDiscovery.addBean(bean);
			this.entities.put(entity, EntityMetadata.of(entity));
		});
	}
	
	<T> Optional<EntityMetadata> get(final Class<T> entity)
	{
		return Optional.ofNullable(this.entities.get(entity));
	}
	
	@Override
	public String toString()
	{
		return "StorageExtension{"
			+
			"storageRoot="
			+ this.storageRoot
			+
			", entities="
			+ this.entities
			+
			'}';
	}
}
