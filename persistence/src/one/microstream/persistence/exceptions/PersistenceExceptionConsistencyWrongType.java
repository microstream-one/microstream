package one.microstream.persistence.exceptions;



public class PersistenceExceptionConsistencyWrongType extends PersistenceExceptionConsistency
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final long     tid       ;
	final Class<?> actualType;
	final Class<?> passedType;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionConsistencyWrongType(final long tid, final Class<?> actualType, final Class<?> passedType)
	{
		super();
		this.tid        = tid       ;
		this.actualType = actualType;
		this.passedType = passedType;
	}
	
	@Override
	public String getMessage()
	{
//		return super.getMessage();
		return "TypeId: " + this.tid
			+ ", actual type: " + (this.actualType == null ? null : this.actualType.getCanonicalName())
			+ ", passed type: " + (this.passedType == null ? null : this.passedType.getCanonicalName())
		;
	}



}
