
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

import one.microstream.storage.types.StorageManager;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@ApplicationScoped
public class NameDAO
{
	@Inject
	private StorageManager manager;
	
	private Agenda         root;
	
	@PostConstruct
	public void init()
	{
		if(Objects.isNull(this.manager.root()))
		{
			this.root = new Agenda();
			this.manager.setRoot(this.root);
		}
		else
		{
			this.root = (Agenda)this.manager.root();
		}
	}
	
	public void add(final String name)
	{
		this.root.add(Objects.requireNonNull(name, "name is required"));
	}
	
	public List<String> getNamesSorted()
	{
		return this.root.getNames().stream().sorted().collect(Collectors.toList());
	}
}
