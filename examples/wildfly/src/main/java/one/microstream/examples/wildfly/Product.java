
package one.microstream.examples.wildfly;

/*-
 * #%L
 * microstream-examples-wildfly
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

import java.util.Objects;

import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;


public class Product
{
	private final long   id         ;
	private final String name       ;
	private final String description;
	private final int    rating     ;
	
	@JsonbCreator
	public Product(
		@JsonbProperty("id")          final long   id         ,
		@JsonbProperty("name")        final String name       ,
		@JsonbProperty("description") final String description,
		@JsonbProperty("rating")      final int    rating
	)
	{
		this.id          = id         ;
		this.name        = name       ;
		this.description = description;
		this.rating      = rating     ;
	}
	
	public long getId()
	{
		return this.id;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public String getDescription()
	{
		return this.description;
	}
	
	public int getRating()
	{
		return this.rating;
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
		final Product product = (Product)o;
		return this.id == product.id;
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hashCode(this.id);
	}
	
	@Override
	public String toString()
	{
		return "Product{"
			+
			"id="
			+ this.id
			+
			", name='"
			+ this.name
			+ '\''
			+
			", description='"
			+ this.description
			+ '\''
			+
			", rating="
			+ this.rating
			+
			'}';
	}
}
