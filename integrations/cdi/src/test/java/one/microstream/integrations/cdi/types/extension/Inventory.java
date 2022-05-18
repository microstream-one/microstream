
package one.microstream.integrations.cdi.types.extension;

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

import java.util.ArrayList;
import java.util.List;


/**
 * this class servers as root node for our custom data structure
 *
 */
public class Inventory
{
	private final List<Product> products = new ArrayList<>();
	private final String        name    ;
	
	public Inventory(final String name)
	{
		this.name = name;
	}
	
	public void add(final Product product)
	{
		this.products.add(product);
	}
	
	public List<Product> getProducts()
	{
		return this.products;
	}
	
	@Override
	public String toString()
	{
		return "Inventory [products=" + this.products + ", name=" + this.name + "]";
	}
	
	public String getName()
	{
		return this.name;
	}
	
}
