package net.jadoth.exceptions;


/**
 * {@link ClassCastException} implemented properly.
 * <p>
 * First of all: The correct term is "TypeCastException" and not "ClassCastException" because it not only handles
 * classes but also interfaces (and the general term for both of them is "type"). Interfaces may be hackily defined
 * as "class" instead of "type" on the bytecode level (god knows why), but that does not change the proper term
 * on the level of design / API / source code.
 * <p>
 * Second: Proper exception design is to reference the involved objects if possible to keep them programmatically
 * processible. Cramming everything just into contextless and runtime expensive plain strings is so noobish I can't
 * tell. Exceptions are control flow vehicles, not just debug messages for the developer. Designing them only as
 * the latter proves very dilettantic understanding of Java or modern OOP in general.
 * <p>
 * One might think that the original Java developers themselves would get basic stuff like that right back then.
 * Sadly, they apparently didn't.
 *
 * @author Thomas Muenz
 */
public class TypeCastException extends ClassCastException
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	/**
	 * The target type the subject could not be casted to.
	 */
	private final Class<?> type   ;

	/**
	 * The subject that could not be casted.
	 */
	private final Object   subject;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Instantiates a new instance with the target type and the subject instance that caused the exception and
	 * no explicit message.
	 *
	 * @param type the target type the subject could not be casted to.
	 * @param subject the subject that could not be casted.
	 */
	public TypeCastException(final Class<?> type, final Object subject)
	{
		this(type, subject, null);
	}

	/**
	 * Instantiates a new instance with the target type and the subject instance that caused the exception and
	 * an explicit message.
	 *
	 * @param type the target type the subject could not be casted to.
	 * @param subject the subject that could not be casted.
	 * @param message an arbitrary string used as a custom message.
	 */
	public TypeCastException(final Class<?> type, final Object subject, final String message)
	{
		super(message);
		this.type    = type   ;
		this.subject = subject;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	/**
	 * The target type the subject could not be casted to.
	 *
	 * @return the target type.
	 */
	public final Class<?> type()
	{
		return this.type;
	}

	/**
	 * The subject that could not be casted.
	 *
	 * @return the subject.
	 */
	public final Object subject()
	{
		return this.subject;
	}

	/**
	 * Sadly, the Throwable implementation uses #getMessage() directly to print the exception.
	 * This is a concern conflict: getMessage should actually be the getter for the explicit message.
	 * But it is used as the String representating method as well.
	 * So a output message generically assembling the output string must override the getter.
	 * As this hides the actual getting functionality, a workaround accessor method has to be provided
	 * for potential subclasses.
	 *
	 * @return the explicit message string passed to the constructor when creating this instance.
	 */
	// I love noobish JDK developers. And why is the adjective "Throwable" not an interface in the first place.
	public final String message()
	{
		return super.getMessage();
	}

	public String assembleDetailString()
	{
		return "Cannot cast " + this.subject().getClass().getName() + " to " + this.type().getName();
	}

	protected String assembleExplicitMessageAddon()
	{
		final String explicitMessage = super.getMessage();
		return explicitMessage == null ? "" : " (" + explicitMessage + ")";
	}

	public String assembleOutputString()
	{
		return this.assembleDetailString() + this.assembleExplicitMessageAddon();
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	/**
	 * Returns an assembled output String due to bad method design in {@link Throwable}.
	 * For the actual message getter, see {@link #message()}.
	 *
	 * @return this exception type's generic string plus an explicit message if present.
	 */
	@Override
	public String getMessage() // intentionally not final to enable subclasses to change the behavior again
	{
		return this.assembleOutputString();
	}



	// ridiculous hacky buggy security hole misconceptioned serialization, but if need be...
	private static final long serialVersionUID = -6986341469122765501L;
}
