package one.microstream.util.traversing;

/**
 * This type should actually extend {@link Throwable}, not {@link Runtime}. But sadly, the prior is checked,
 * which is a deeply flawed concept in Java and prevent proper functional programming.
 * Also, this type should be an interface instead of a class, but again: Java design flaws (not understanding their
 * own interface-based language, so funny).
 * 
 * 
 */
public abstract class AbstractTraversalSignal extends RuntimeException
{

	protected AbstractTraversalSignal()
	{
		super();
	}
	
	@Override
	public synchronized AbstractTraversalSignal fillInStackTrace()
	{
		// signals are branching mechanisms, not debugging tools. Hence no stack trace is needed or wanted.
		return this;
	}
	
}
