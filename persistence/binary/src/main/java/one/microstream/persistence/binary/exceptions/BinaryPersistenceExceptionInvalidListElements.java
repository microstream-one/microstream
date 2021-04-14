package one.microstream.persistence.binary.exceptions;

public class BinaryPersistenceExceptionInvalidListElements extends BinaryPersistenceExceptionInvalidList
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final long listElementCount;
	private final long elementLength   ;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryPersistenceExceptionInvalidListElements(
		final long entityLength    ,
		final long objectId        ,
		final long typeId          ,
		final long listStartOffset ,
		final long listTotalLength ,
		final long listElementCount,
		final long elementLength
	)
	{
		super(entityLength, objectId, typeId, listStartOffset, listTotalLength);
		this.listElementCount = listElementCount;
		this.elementLength    = elementLength   ;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	protected String assembleDetailStringBody()
	{
		return super.assembleDetailStringBody() + ", " +
			"listElementCount = " + this.listElementCount + ", " +
			"elementLength = "    + this.elementLength
		;
	}
	
}
