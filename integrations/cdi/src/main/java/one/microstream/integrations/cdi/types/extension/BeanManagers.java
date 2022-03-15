
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

import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.InjectionException;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;


/**
 * Utilitarian class to {@link javax.enterprise.inject.spi.BeanManager}
 */
public final class BeanManagers
{
	/**
	 * Get instance from the {@link BeanManager}
	 *
	 * @param clazz
	 *            the clazz to inject from the BeanManager
	 * @param beanManager
	 *            the BeanManager
	 * @param <T>
	 *            the instance type
	 * @return the instance from CDI context
	 */
	public static <T> T getInstance(final Class<T> clazz, final BeanManager beanManager)
	{
		Objects.requireNonNull(clazz, "clazz is required");
		Objects.requireNonNull(beanManager, "beanManager is required");
		return getInstanceImpl(clazz, beanManager);
	}
	
	/**
	 * Get instance from the {@link BeanManager}
	 *
	 * @param clazz
	 *            the clazz to inject from the BeanManager
	 * @param beanManager
	 *            the BeanManager
	 * @param qualifier
	 *            the qualifier
	 * @param <T>
	 *            the instance type
	 * @return the instance from CDI context
	 */
	public static <T> T getInstance(final Class<T> clazz, final Annotation qualifier, final BeanManager beanManager)
	{
		Objects.requireNonNull(clazz, "clazz is required");
		Objects.requireNonNull(qualifier, "qualifier is required");
		Objects.requireNonNull(beanManager, "beanManager is required");
		
		return getInstanceImpl(clazz, qualifier, beanManager);
	}
	
	/**
	 * Get the CDI BeanManager for the current CDI context
	 *
	 * @return the BeanManager
	 */
	public static BeanManager getBeanManager()
	{
		return CDI.current().getBeanManager();
	}
	
	/**
	 * Get instance from the {@link BeanManager} using the {@link BeanManagers#getBeanManager()}
	 *
	 * @param clazz
	 *            the clazz to inject from the BeanManager
	 * @param <T>
	 *            the instance type
	 * @return the instance from CDI context
	 */
	public static <T> T getInstance(final Class<T> clazz)
	{
		Objects.requireNonNull(clazz, "clazz is required");
		return getInstanceImpl(clazz, getBeanManager());
	}
	
	/**
	 * Get instance from the {@link BeanManager} using the {@link BeanManagers#getBeanManager()}
	 *
	 * @param clazz
	 *            the clazz to inject from the BeanManager
	 * @param qualifier
	 *            the qualifier
	 * @param <T>
	 *            the instance type
	 * @return the instance from CDI context
	 */
	public static <T> T getInstance(final Class<T> clazz, final Annotation qualifier)
	{
		Objects.requireNonNull(clazz, "clazz is required");
		return getInstanceImpl(clazz, qualifier, getBeanManager());
	}
	
	@SuppressWarnings("unchecked")
	private static <T> T getInstanceImpl(final Class<T> clazz, final BeanManager beanManager)
	{
		final Set<Bean<?>> beans = beanManager.getBeans(clazz);
		if(beans.isEmpty())
		{
			throw new InjectionException("Does not find the bean class: " + clazz + " into CDI container");
		}
		final Bean<T>              bean = (Bean<T>)beans.iterator().next();
		final CreationalContext<T> ctx  = beanManager.createCreationalContext(bean);
		return (T)beanManager.getReference(bean, clazz, ctx);
	}
	
	@SuppressWarnings("unchecked")
	private static <T> T getInstanceImpl(
		final Class<T> clazz,
		final Annotation qualifier,
		final BeanManager beanManager)
	{
		final Set<Bean<?>> beans = beanManager.getBeans(clazz, qualifier);
		checkInjection(clazz, beans);
		final Bean<T>              bean = (Bean<T>)beans.iterator().next();
		final CreationalContext<T> ctx  = beanManager.createCreationalContext(bean);
		return (T)beanManager.getReference(bean, clazz, ctx);
	}
	
	private static <T> void checkInjection(final Class<T> clazz, final Set<Bean<?>> beans)
	{
		if(beans.isEmpty())
		{
			throw new InjectionException("Does not find the bean class: " + clazz + " into CDI container");
		}
	}
	
	private BeanManagers()
	{
		throw new UnsupportedOperationException();
	}
}
