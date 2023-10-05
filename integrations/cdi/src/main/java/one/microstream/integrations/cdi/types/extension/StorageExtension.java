
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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessInjectionPoint;
import javax.enterprise.inject.spi.WithAnnotations;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import one.microstream.integrations.cdi.types.Storage;
import one.microstream.storage.types.StorageManager;


/**
 * This extension will look for Objects that are marked with {@link Storage}.
 */
@ApplicationScoped
public class StorageExtension implements Extension
{

	private static final Logger LOGGER = Logger.getLogger(StorageExtension.class.getName());

	private final Set<Class<?>> storageRoot = new HashSet<>();

	private final Map<Class<?>, Set<InjectionPoint>> storageInjectionPoints = new HashMap<>();

	private final Set<String> storageManagerConfigInjectionNames = new HashSet<>();

	<T> void loadEntity(@Observes @WithAnnotations({Storage.class}) final ProcessAnnotatedType<T> target)
	{
		final AnnotatedType<T> annotatedType = target.getAnnotatedType();
		if (annotatedType.isAnnotationPresent(Storage.class))
		{

			final Class<T> javaClass = target.getAnnotatedType()
					.getJavaClass();
			this.storageRoot.add(javaClass);
			LOGGER.info("New class found annotated with @Storage is " + javaClass);
		}
	}

	void collectInjectionsFromStorageBean(@Observes final ProcessInjectionPoint<?, ?> pip)
	{
		final InjectionPoint ip = pip.getInjectionPoint();
		if (ip.getBean() != null && ip.getBean()
				.getBeanClass()
				.getAnnotation(Storage.class) != null)
		{
			this.storageInjectionPoints
					.computeIfAbsent(ip.getBean()
											 .getBeanClass(), k -> new HashSet<>())
					.add(ip);
		}
		// Is @Inject @ConfigProperty on StorageManager?
		if (this.isStorageManagerFromConfig(ip))
		{
			this.storageManagerConfigInjectionNames.add(this.getConfigPropertyValueOf(ip));

		}
	}

	private String getConfigPropertyValueOf(final InjectionPoint ip)
	{
		return ip.getQualifiers()
				.stream()
				.filter(q -> q.annotationType()
						.isAssignableFrom(ConfigProperty.class))
				.findAny()
				.map(q -> ((ConfigProperty) q).name())
				.orElse("");
	}

	private boolean isStorageManagerFromConfig(final InjectionPoint ip)
	{
		return ip.getMember() instanceof Field
				&& ((Field)ip.getMember()).getType().isAssignableFrom(StorageManager.class)
				&& ip.getQualifiers()
				.stream()
				.anyMatch(q -> q.annotationType()
						.isAssignableFrom(ConfigProperty.class));
	}

	void onAfterBeanDiscovery(@Observes final AfterBeanDiscovery afterBeanDiscovery, final BeanManager beanManager)
	{
		LOGGER.info(String.format("Processing StorageExtension:  %d @Storage found", this.storageRoot.size()));
		if (this.storageRoot.size() > 1)
		{
			throw new IllegalStateException(
					"In the application must have only one class with the Storage annotation, classes: "
							+ this.storageRoot);
		}
		if (this.storageManagerConfigInjectionNames.size()>1 && !this.storageRoot.isEmpty()) {
			throw new IllegalStateException(
					"It is not supported to define multiple StorageManager's through @ConfigProperty in combination with a @Storage annotated class. Names : "
							+ this.storageManagerConfigInjectionNames);

		}
		this.storageRoot.forEach(entity ->
		{
			Set<InjectionPoint> injectionPoints = this.storageInjectionPoints.get(entity);
			if (injectionPoints == null) {
				injectionPoints = Collections.emptySet();
			}
			final StorageBean<?> bean = new StorageBean<>(beanManager, entity, injectionPoints);
			afterBeanDiscovery.addBean(bean);
		});
	}

	public Set<String> getStorageManagerConfigInjectionNames()
	{
		return this.storageManagerConfigInjectionNames;
	}

	public boolean hasStorageRoot()
	{
		return !this.storageRoot.isEmpty();
	}

	@Override
	public String toString()
	{
		return "StorageExtension{"
				+
				"storageRoot="
				+ this.storageRoot
				+
				'}';
	}
}
