package one.microstream.test.corp.model;

public final class Product
{
	////////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final String productName;
	final Vendor vendor     ;
	final Double price      ;



	////////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public Product(final String productName, final Vendor vendor, final Double price)
	{
		super();
		this.productName = productName;
		this.vendor      = vendor     ;
		this.price       = price      ;
	}



	////////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	public final String productName()
	{
		return this.productName;
	}

	public final Vendor vendor()
	{
		return this.vendor;
	}

	public final Double price()
	{
		return this.price;
	}

}
