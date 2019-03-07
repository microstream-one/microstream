package one.microstream.test.corp.model;

import static one.microstream.X.notNull;

import one.microstream.collections.EqHashTable;
import one.microstream.collections.types.XTable;


public final class Vendor
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Corporation corporation;

	private final EqHashTable<String, Product> productsByName = EqHashTable.New();



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public Vendor(final Corporation corporation)
	{
		super();
		this.corporation = notNull(corporation);
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	public Corporation corporation()
	{
		return this.corporation;
	}

	public XTable<String, Product> productsByName()
	{
		return this.productsByName;
	}

	public synchronized void registerProduct(final Product product)
	{
		this.productsByName.add(product.productName(), product);
	}

}
