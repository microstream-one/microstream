package one.microstream.cache.hibernate.types;

/*-
 * #%L
 * microstream-cache-hibernate
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

import java.util.Collections;

import org.hibernate.boot.registry.selector.SimpleStrategyRegistrationImpl;
import org.hibernate.boot.registry.selector.StrategyRegistration;
import org.hibernate.boot.registry.selector.StrategyRegistrationProvider;
import org.hibernate.cache.spi.RegionFactory;

public class CacheStrategyRegistrationProvider implements StrategyRegistrationProvider
{
	public CacheStrategyRegistrationProvider()
	{
		super();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Iterable<StrategyRegistration> getStrategyRegistrations()
	{
		SimpleStrategyRegistrationImpl<RegionFactory> registration = new SimpleStrategyRegistrationImpl<>(
			RegionFactory.class,
			CacheRegionFactory.class,
			"jcache",
			CacheRegionFactory.class.getName(),
			CacheRegionFactory.class.getSimpleName()
		);
		return Collections.singleton(registration);
	}	
}
