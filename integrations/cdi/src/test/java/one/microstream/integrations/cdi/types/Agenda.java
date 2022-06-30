
package one.microstream.integrations.cdi.types;

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

import one.microstream.reference.Lazy;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;


@Storage
public class Agenda
{

	@Inject
	private DirtyMarker dirtyMarker;

	private final Set<String> names;

	private final Lazy<Set<String>> lazyNames;

	public Agenda()
	{
		this.names = new ConcurrentSkipListSet<>();
		this.lazyNames = Lazy.Reference(new ConcurrentSkipListSet<>());
	}

	public void add(final String name)
	{
		this.dirtyMarker.mark(this.names)
				.add(name);
	}

	public void addToLazy(final String name)
	{
		this.dirtyMarker.mark(this.lazyNames)
				.get()
				.add(name);
	}

	public Set<String> getNames()
	{
		return Collections.unmodifiableSet(this.names);
	}

	public Set<String> getLazySet()
	{
		return this.lazyNames.get();
	}

	public boolean isLazySetLoaded()
	{
		return this.lazyNames.isLoaded();
	}

	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || this.getClass() != o.getClass())
		{
			return false;
		}
		final Agenda agenda = (Agenda)o;
		return Objects.equals(this.names, agenda.names);
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hashCode(this.names);
	}
	
	@Override
	public String toString()
	{
		return "Agenda{"
			+
			"names="
			+ this.names
			+
			'}';
	}
}
