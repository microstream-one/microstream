
package one.microstream.examples.openliberty;

/*-
 * #%L
 * microstream-examples-openliberty
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

import java.util.Collection;
import java.util.Optional;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;


@ApplicationScoped
public class ProductRepositoryStorage implements ProductRepository
{
	private static final Logger LOGGER = Logger.getLogger(ProductRepositoryStorage.class.getName());
	
	@Inject
	private Inventory           inventory;
	
	@Override
	public Collection<Product> getAll()
	{
		return this.inventory.getProducts();
	}
	
	@Override
	public Product save(final Product item)
	{
		this.inventory.add(item);
		return item;
	}
	
	@Override
	public Optional<Product> findById(final long id)
	{
		LOGGER.info("Finding the item by id: " + id);
		return this.inventory.findById(id);
	}
	
	@Override
	public void deleteById(final long id)
	{
		this.inventory.deleteById(id);
	}
}
