package one.microstream.test.binary_fields;

public class BFTestInter extends BFTestAbstract
{
	final short ip_short;
	
	char ip_char;

	public BFTestInter(final byte ap_byte, final short ip_short)
	{
		super(ap_byte);
		this.ip_short = ip_short;
	}
	
}
