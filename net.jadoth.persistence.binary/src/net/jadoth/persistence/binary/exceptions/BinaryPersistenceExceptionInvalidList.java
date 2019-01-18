package net.jadoth.persistence.binary.exceptions;

public class BinaryPersistenceExceptionInvalidList extends BinaryPersistenceException
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static final String messageBody()
	{
		return "Invalid list data";
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final long entityLength    ;
	private final long objectId        ;
	private final long typeId          ;
	private final long listStartOffset ;
	private final long listTotalLength ;
	private final long listElementCount;
	private final long elementLength   ;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryPersistenceExceptionInvalidList(
		final long entityLength    ,
		final long objectId        ,
		final long typeId          ,
		final long listStartOffset ,
		final long listTotalLength ,
		final long listElementCount,
		final long elementLength
	)
	{
		super();
		this.entityLength     = entityLength    ;
		this.objectId         = objectId        ;
		this.typeId           = typeId          ;
		this.listStartOffset  = listStartOffset ;
		this.listTotalLength  = listTotalLength ;
		this.listElementCount = listElementCount;
		this.elementLength    = elementLength   ;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public String assembleDetailString()
	{
		return messageBody() + ": " +
			"entityLength = "     + this.entityLength     + ", " +
			"objectId = "         + this.objectId         + ", " +
			"typeId = "           + this.typeId           + ", " +
			"listStartOffset = "  + this.listStartOffset  + ", " +
			"listTotalLength = "  + this.listTotalLength  + ", " +
			"listElementCount = " + this.listElementCount + ", " +
			"elementLength = "    + this.elementLength    + "."
		;
	}
	
}
