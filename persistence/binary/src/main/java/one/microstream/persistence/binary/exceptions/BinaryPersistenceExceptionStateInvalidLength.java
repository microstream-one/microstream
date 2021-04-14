package one.microstream.persistence.binary.exceptions;


public class BinaryPersistenceExceptionStateInvalidLength extends BinaryPersistenceExceptionState
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final long address  ;
	private final long length   ;
	private final long typeId   ;
	private final long objectOid;




	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryPersistenceExceptionStateInvalidLength(
		final long address,
		final long length ,
		final long typeId ,
		final long objectOid
	)
	{
		this(address, length, typeId, objectOid, null, null);
	}

	public BinaryPersistenceExceptionStateInvalidLength(
		final long address  ,
		final long length   ,
		final long typeId   ,
		final long objectOid,
		final String message
	)
	{
		this(address, length, typeId, objectOid, message, null);
	}

	public BinaryPersistenceExceptionStateInvalidLength(
		final long address  ,
		final long length   ,
		final long typeId   ,
		final long objectOid,
		final Throwable cause
	)
	{
		this(address, length, typeId, objectOid, null, cause);
	}

	public BinaryPersistenceExceptionStateInvalidLength(
		final long address  ,
		final long length   ,
		final long typeId   ,
		final long objectOid,
		final String message, final Throwable cause
	)
	{
		this(address, length, typeId, objectOid, message, cause, true, true);
	}

	public BinaryPersistenceExceptionStateInvalidLength(
		final long address  ,
		final long length   ,
		final long typeId   ,
		final long objectOid,
		final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.address   = address  ;
		this.length    = length   ;
		this.typeId    = typeId   ;
		this.objectOid = objectOid;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	public long getLength()
	{
		return this.length;
	}

	public long getTypeId()
	{
		return this.typeId;
	}

	public long getAddress()
	{
		return this.address;
	}

	public long getObjectOid()
	{
		return this.objectOid;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public String getMessage()
	{
		return "Invalid length: " + this.length + "."
			+ "TypeId = " + this.typeId
			+ ", objectId = " + this.objectOid
			+ ", address = " + this.address
			+ (super.getMessage() != null ? " Details: " + super.getMessage() : "")
		;
	}



}
