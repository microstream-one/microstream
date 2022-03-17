
package one.microstream.integrations.cdi.types.test;

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

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.AnnotationUtils;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
import java.util.function.Consumer;


class CDIJUnitExtension implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback
{
	private SeContainer               container;
	private CreationalContext<Object> context  ;
	
	@Override
	public void beforeAll(final ExtensionContext extensionContext)
	{
		AnnotationUtils.findAnnotation(extensionContext.getElement(), CDIExtension.class)
			.ifPresent(config -> this.container = new ContainerSupplier(config).get())
		;
	}
	
	@Override
	public void afterAll(final ExtensionContext extensionContext)
	{
		if(this.container != null)
		{
			this.doClose(this.container);
			this.container = null;
		}
	}
	
	@Override
	public void beforeEach(final ExtensionContext extensionContext)
	{
		if(this.container != null)
		{
			extensionContext.getTestInstance().ifPresent(this.inject());
		}
	}
	
	@SuppressWarnings("unchecked")
	private Consumer<Object> inject()
	{
		return instance ->
		{
			final BeanManager             manager         = this.container.getBeanManager();
			final AnnotatedType<Object>   annotatedType   = manager.createAnnotatedType((Class<Object>)instance.getClass());
			final InjectionTarget<Object> injectionTarget = manager.createInjectionTarget(annotatedType);
			this.context = manager.createCreationalContext(null);
			injectionTarget.inject(instance, this.context);
		};
	}
	
	@Override
	public void afterEach(final ExtensionContext extensionContext)
	{
		if(this.context != null)
		{
			this.context.release();
			this.context = null;
		}
	}
	
	private void doClose(final SeContainer container)
	{
		container.close();
	}
}
