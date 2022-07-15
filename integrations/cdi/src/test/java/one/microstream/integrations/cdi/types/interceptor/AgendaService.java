
package one.microstream.integrations.cdi.types.interceptor;

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

import one.microstream.integrations.cdi.types.Agenda;
import one.microstream.integrations.cdi.types.Store;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.Objects;


@ApplicationScoped
public class AgendaService
{
	@Inject
	private Agenda agenda;

	@Store
	public void addName(final String name)
	{
		Objects.requireNonNull(name, "name is required");
		this.agenda.add(name);
	}

	@Store
	public void addNameLazy(final String name)
	{
		Objects.requireNonNull(name, "name is required");
		this.agenda.addToLazy(name);
	}

	@Store(asynchronous = false)
	public void addNameSynchro(final String name)
	{
		Objects.requireNonNull(name, "name is required");
		this.agenda.add(name);
	}

	@Store(asynchronous = false)
	public void addNameLazySynchro(final String name)
	{
		Objects.requireNonNull(name, "name is required");
		this.agenda.addToLazy(name);
	}

	@Store
	public void addNames(final String... names)
	{
		Objects.requireNonNull(names, "names is required");
		Arrays.stream(names)
				.forEach(name -> this.agenda.add(name));
	}

}
