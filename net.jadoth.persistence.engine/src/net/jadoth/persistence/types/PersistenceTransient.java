package net.jadoth.persistence.types;

import net.jadoth.reference.Referencing;
import net.jadoth.swizzling.types.Lazy;


/**
 * A reference providing generic optionally transient (i.e. skipping) storing functionality.
 * <p>
 * This type can be seen as a counterpart to {@link Lazy} on the storing side.
 * <p>
 * As explained there, functionality modifying persistence behavior (loading, storing) is a business logic level
 * concern and therefor must be defined there explicity.
 * <p>
 * Despite its short name, this type offers the possibility to turn the transient functionality on and off, as
 * constant transience does not require a relaying reference at all but just the exclusion of the class field.
 *
 * @author Thomas Muenz
 *
 * @param <T>
 */
public final class PersistenceTransient<T> implements Referencing<T>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final T subject;

	// (21.05.2013 TM)FIXME: Transient: implement typeHandler
	boolean isTransient; // sadly causes 7 byte memory alignment overhead, but what can one do



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceTransient(final T subject)
	{
		super();
		this.subject = subject;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	public final PersistenceTransient<T> setTransient(final boolean isTransient)
	{
		this.isTransient = isTransient;
		return this;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final T get()
	{
		return this.subject;
	}

}
