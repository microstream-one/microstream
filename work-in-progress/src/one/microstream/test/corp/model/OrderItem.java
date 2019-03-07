package one.microstream.test.corp.model;

public final class OrderItem
{
	////////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final Product product   ;
	final Integer amount    ;
	final Double  totalPrice;



	////////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public OrderItem(final Product product, final Integer amount, final Double totalPrice)
	{
		super();
		this.product    = product   ;
		this.amount     = amount    ;
		this.totalPrice = totalPrice;
	}



	////////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	public final Product product()
	{
		return this.product;
	}

	public final Integer amount()
	{
		return this.amount;
	}

	public final Double totalPrice()
	{
		return this.totalPrice;
	}

}
