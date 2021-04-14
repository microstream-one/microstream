package one.microstream.functional;


// sorry, but "BooleanSupplier#getAsBoolean" is beyond ridiculous for what is a general boolean term.
@FunctionalInterface
public interface BooleanTerm
{
	public boolean evaluate();
}
