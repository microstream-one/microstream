
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
import one.microstream.integrations.cdi.types.StoreType;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Objects;


@ApplicationScoped
public class AgendaLazyService
{
	@Inject
	private Agenda agenda;
	
	@Store
	public void addNameLazy(final String name)
	{
		Objects.requireNonNull(name, "name is required");
		this.agenda.add(name);
	}
	
	@Store(fields = "names")
	public void updateName(final String name)
	{
		Objects.requireNonNull(name, "name is required");
		this.agenda.add(name);
	}
	
	@Store(value = StoreType.LAZY, root = true)
	public void addNameRoot(final String name)
	{
		Objects.requireNonNull(name, "name is required");
		this.agenda.add(name);
	}
}
