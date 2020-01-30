package one.microstream.test.binary_fields;

import java.util.Date;

public abstract class BFTestAbstract
{
	final byte ap_byte;
	
	boolean ap_boolean;
	String  arString1 ;
	
	String  aDerivedString;
	Date    aDerivedDate  ;
	
	public BFTestAbstract(final byte ap_byte)
	{
		super();
		this.ap_byte = ap_byte;
	}
	
	public long getDerivedStringValue()
	{
		return this.aDerivedString == null
			? 0
			: Long.parseLong(this.aDerivedString)
		;
	}
	
	public void setDerivedStringValue(final long value)
	{
		this.aDerivedString = String.valueOf(value);
	}
	
	public long getDerivedDateTimestamp()
	{
		return this.aDerivedDate == null
			? 0
			: this.aDerivedDate.getTime()
		;
	}
	
	public void setDerivedDateTimestamp(final long value)
	{
		this.aDerivedDate = new Date(value);
	}
	
}
