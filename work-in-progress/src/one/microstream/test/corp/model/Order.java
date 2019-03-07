package one.microstream.test.corp.model;

import one.microstream.collections.types.XTable;

public final class Order
{
	////////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final String                     orderId        ;
	final Customer                   customer       ;
	final XTable<Product, OrderItem> items          ;
	final Address                    billingAddress ;
	final Address                    shippingAddress;



	////////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public Order(
		final String                     orderId        ,
		final Customer                   customer       ,
		final XTable<Product, OrderItem> items          ,
		final Address                    billingAddress ,
		final Address                    shippingAddress
	)
	{
		super();
		this.orderId         = orderId        ;
		this.customer        = customer       ;
		this.items           = items          ;
		this.billingAddress  = billingAddress ;
		this.shippingAddress = shippingAddress;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	public final String orderId()
	{
		return this.orderId;
	}

	public final Customer customer()
	{
		return this.customer;
	}

	public final XTable<Product, OrderItem> items()
	{
		return this.items;
	}

	public final Address billingAddress()
	{
		return this.billingAddress;
	}

	public final Address shippingAddress()
	{
		return this.shippingAddress;
	}



	////////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final String toString()
	{
		return Order.class.getSimpleName()+" "+this.orderId();
	}

}
