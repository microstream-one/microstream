
package one.microstream.examples.payara;

/*-
 * #%L
 * microstream-examples-payara-micro
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

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;


import one.microstream.integrations.cdi.types.DirtyMarker;
import one.microstream.integrations.cdi.types.Storage;

import javax.inject.Inject;

@Storage
public class Inventory
{

	@Inject
	private DirtyMarker dirtyMarker;

	private final Set<Product> products = new HashSet<>();
	
	public void add(final Product product)
	{
		Objects.requireNonNull(product, "product is required");
		this.dirtyMarker.mark(this.products).add(product);
	}
	
	public Set<Product> getProducts()
	{
		return Collections.unmodifiableSet(this.products);
	}
	
	public Optional<Product> findById(final long id)
	{
		return this.products.stream().filter(this.isIdEquals(id)).limit(1).findFirst();
	}
	
	public void deleteById(final long id)
	{
		this.dirtyMarker.mark(this.products).removeIf(this.isIdEquals(id));
		
	}
	
	private Predicate<Product> isIdEquals(final long id)
	{
		return p -> p.getId() == id;
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
		final Inventory inventory = (Inventory)o;
		return Objects.equals(this.products, inventory.products);
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hashCode(this.products);
	}
	
	@Override
	public String toString()
	{
		return "Inventory{"
			+
			"products="
			+ this.products
			+
			'}';
	}
	
}
