package one.microstream.chars;

/**
 * A "{@link ObjectStringConverter}" is hereby defined as a logic instance that handles
 * both conversion to and from a String-form of instances of a certain type.
 * 
 * 
 */
public interface ObjectStringConverter<T> extends ObjectStringAssembler<T>, ObjectStringParser<T>
{
	// just a typing interface so far
}
