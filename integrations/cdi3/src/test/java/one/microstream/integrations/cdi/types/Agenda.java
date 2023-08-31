
package one.microstream.integrations.cdi.types;

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

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;


@Storage
public class Agenda
{

	private final Set<String> names;

	public Agenda()
	{
		this.names = new ConcurrentSkipListSet<>();
	}

	public void add(final String name)
	{
		this.names.add(name);
	}

	public Set<String> getNames()
	{
		return Collections.unmodifiableSet(this.names);
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
