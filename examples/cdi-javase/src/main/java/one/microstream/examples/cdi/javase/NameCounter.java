
package one.microstream.examples.cdi.javase;

/*-
 * #%L
 * microstream-examples-cdi-javase
 * %%
 * Copyright (C) 2019 - 2021 MicroStream Software
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

import one.microstream.integrations.cdi.types.cache.StorageCache;

import javax.cache.Cache;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import static java.util.Optional.ofNullable;


@ApplicationScoped
public class NameCounter
{
	@Inject
	@StorageCache
	private Cache<String, Integer> counter;

	public synchronized int count(final String name)
	{
		int counter = this.show(name);
		counter++;
		this.counter.put(name, counter);
		return counter;
	}
	
	public int show(final String name)
	{
		return ofNullable(this.counter.get(name)).orElse(0);
	}
	
	public Map<String, Integer> getNames()
	{
		
		final Map<String, Integer> map = new HashMap<>();
		this.counter.forEach(c -> map.put(c.getKey(), c.getValue()));
		return map;
	}
}
