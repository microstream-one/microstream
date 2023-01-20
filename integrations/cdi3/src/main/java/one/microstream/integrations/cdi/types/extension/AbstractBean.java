
package one.microstream.integrations.cdi.types.extension;

/*-
 * #%L
 * microstream-integrations-cdi3
 * %%
 * Copyright (C) 2019 - 2023 MicroStream Software
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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.PassivationCapable;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;


/**
 * A template class to all the programmatic {@link Bean}s that are defined.
 *
 * @param <T> the bean type
 */
public abstract class AbstractBean<T> implements Bean<T>, PassivationCapable
{
	protected final BeanManager beanManager;
	private final Set<InjectionPoint> injectionPoints;

	protected AbstractBean(final BeanManager beanManager, final Set<InjectionPoint> injectionPoints)
	{
		this.beanManager = beanManager;
		this.injectionPoints = injectionPoints;
	}

	protected <B> B getInstance(final Class<B> clazz)
	{
		return CDI.current().select(clazz).get();
	}

	protected void injectDependencies(final T root)
	{
		final AnnotatedType<T> type = (AnnotatedType<T>) this.beanManager.createAnnotatedType(root.getClass());
		final CreationalContext<T> context = this.beanManager.createCreationalContext(null);
		this.beanManager.getInjectionTargetFactory(type)
				.createInjectionTarget(this)
				.inject(root, context);
	}

	@Override
	public Set<InjectionPoint> getInjectionPoints()
	{
		return this.injectionPoints;
	}

	@Override
	public boolean isNullable()
	{
		return false;
	}
	
	@Override
	public Class<? extends Annotation> getScope()
	{
		return ApplicationScoped.class;
	}
	
	@Override
	public String getName()
	{
		return null;
	}
	
	@Override
	public Set<Class<? extends Annotation>> getStereotypes()
	{
		return Collections.emptySet();
	}
	
	@Override
	public boolean isAlternative()
	{
		return false;
	}
	
	@Override
	public void destroy(final T instance, final CreationalContext<T> context)
	{
		// no-op by default
	}
	
}
