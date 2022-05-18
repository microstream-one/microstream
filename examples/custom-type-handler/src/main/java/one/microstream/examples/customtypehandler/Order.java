
package one.microstream.examples.customtypehandler;

/*-
 * #%L
 * microstream-examples-custom-type-handler
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

import java.util.Date;


public class Order
{
	public static class Item
	{
		private final int productNumber;
		private final int amount;
		
		public Item(final int productNumber, final int amount)
		{
			super();
			this.productNumber = productNumber;
			this.amount        = amount;
		}
		
		public int getProductNumber()
		{
			return this.productNumber;
		}
		
		public int getAmount()
		{
			return this.amount;
		}
	}
	
	private Date   timestamp;
	private String customerNumber;
	private Item[] items;
	
	public Order()
	{
	}
	
	public Order(final Date timestamp, final String customerNumber, final Item... items)
	{
		super();
		this.timestamp      = timestamp;
		this.customerNumber = customerNumber;
		this.items          = items;
	}
	
	public Date getTimestamp()
	{
		return this.timestamp;
	}
	
	public void setTimestamp(final Date timestamp)
	{
		this.timestamp = timestamp;
	}
	
	public String getCustomerNumber()
	{
		return this.customerNumber;
	}
	
	public void setCustomerNumber(final String customerNumber)
	{
		this.customerNumber = customerNumber;
	}
	
	public Item[] getItems()
	{
		return this.items;
	}
	
	public void setItems(final Item[] items)
	{
		this.items = items;
	}
	
}
