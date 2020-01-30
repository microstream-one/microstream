package one.microstream.test.binary_fields;

public class BFTestLeaf extends BFTestInter
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	final int lp_int;
	
	float  lp_float ;
	long   lp_long  ;
	double lp_double;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BFTestLeaf(final byte ap_byte, final short ip_short, final int lp_int)
	{
		super(ap_byte, ip_short);
		this.lp_int = lp_int;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	public long get_longValue()
	{
		return this.lp_long;
	}
	
	public void set_longValue(final long value)
	{
		this.lp_long = value;
	}
	
	public double get_doubleValue()
	{
		return this.lp_double;
	}
	
	public void set_doubleValue(final double value)
	{
		this.lp_double = value;
	}
	
	
	
	static BinaryHandlerBFTestLeaf provideTypeHandler()
	{
		return new BinaryHandlerBFTestLeaf();
	}
	
}
