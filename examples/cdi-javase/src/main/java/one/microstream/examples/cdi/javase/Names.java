
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

import one.microstream.integrations.cdi.types.Storage;
import one.microstream.persistence.types.Persister;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;


@Storage
public class Names implements Supplier<Set<String>>
{

	@Inject
	private transient Persister persister;

	private final Set<String> names = new HashSet<>();
	
	public void add(final String name) {
		this.names.add(Objects.requireNonNull(name, "name is required"));
		this.persister.store(this.names);
	}
	
	@Override
	public Set<String> get()
	{
		return Collections.unmodifiableSet(this.names);
	}
	
	@Override
	public boolean equals(final Object o)
	{
		if(this == o)
		{
			return true;
		}
		if(o == null || this.getClass() != o.getClass())
		{
			return false;
		}
		final Names names = (Names)o;
		return Objects.equals(this.names, names.names);
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hashCode(this.names);
	}
	
	@Override
	public String toString()
	{
		return "Names{"
			+
			"names="
			+ this.names
			+
			'}';
	}
}
