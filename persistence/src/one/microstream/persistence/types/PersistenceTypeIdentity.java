package one.microstream.persistence.types;


public interface PersistenceTypeIdentity extends PersistenceTypeIdOwner
{
	@Override
	public long typeId();

	public String typeName();


	public static int hashCode(final PersistenceTypeIdentity typeIdentity)
	{
		return Long.hashCode(typeIdentity.typeId()) & typeIdentity.typeName().hashCode();
	}

	public static boolean equals(
		final PersistenceTypeIdentity ti1,
		final PersistenceTypeIdentity ti2
	)
	{
		return ti1 == ti2
			|| ti1 != null && ti2 != null
			&& ti1.typeId() == ti2.typeId()
			&& ti1.typeName().equals(ti2.typeName())
		;
	}

}
