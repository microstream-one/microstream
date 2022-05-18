
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

import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;
import java.util.function.Supplier;
import java.util.stream.Stream;


class ContainerSupplier implements Supplier<SeContainer>
{
	private final CDIExtension config;
	
	ContainerSupplier(final CDIExtension config)
	{
		this.config = config;
	}
	
	@Override
	public SeContainer get()
	{
		final SeContainerInitializer initializer = SeContainerInitializer.newInstance();
		if(this.config.disableDiscovery())
		{
			initializer.disableDiscovery();
		}
		initializer.setClassLoader(Thread.currentThread().getContextClassLoader());
		initializer.addBeanClasses(this.config.classes());
		initializer.enableDecorators(this.config.decorators());
		initializer.enableInterceptors(this.config.interceptors());
		initializer.selectAlternatives(this.config.alternatives());
		initializer.selectAlternativeStereotypes(this.config.alternativeStereotypes());
		initializer.addPackages(this.getPackages(this.config.packages()));
		initializer.addPackages(true, this.getPackages(this.config.recursivePackages()));
		return initializer.initialize();
	}
	
	private Package[] getPackages(final Class<?>[] packages)
	{
		return Stream.of(packages).map(Class::getPackage).toArray(Package[]::new);
	}
	
}
