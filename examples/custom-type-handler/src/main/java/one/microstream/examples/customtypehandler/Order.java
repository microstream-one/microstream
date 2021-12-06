
package one.microstream.examples.customtypehandler;

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
