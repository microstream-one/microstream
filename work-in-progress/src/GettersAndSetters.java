
public class GettersAndSetters
{
	Value value;

	/**
	 * Basic Getter
	 * Returns the value of the property (not necessarily an actual field!) named "value"
	 *
	 * @return the value of the property named "value"
	 */
	public Value getValue()
	{
		return this.value;
	}

	/**
	 * Basic Setter
	 * Sets the passed value to the property (not necessarily an actual field!) named "value"
	 *
	 * @param value the new value for the property named "value"
	 */
	public void setValue(final Value value)
	{
		this.value = value;
	}



	/** Chained Setter
	 * basic setter behavior, but returns instance of instance's class type
	 *
	 * note that every basic setter could become a chained setter IF Java
	 * should ever be extended to treat "void" as implicitely returning this.
	 * see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6373386
	 * The problem with this is the missing "Self" type in Java, which in turn
	 * makes this feature request hardly likely to ever be implemented.
	 */
	public GettersAndSetters setValue1(final Value value) // the "1" is only for disambiguation in the example!
	{
		this.value = value;
		return this;
	}

	/** Defaulting Getter
	 *(returns the passed value if the actual value is null)
	 */
	public Value getValue(final Value defaultValue)
	{
		return this.value != null
			? this.value
			: defaultValue
		;
	}

	/** Optional Setter
	 * Sets the passed value only if the actual value is null.
	 * Useful for ensuring that a value is set without risking to overwrite it
	 */
	public void optSetValue(final Value value)
	{
		if(this.value != null)
		{
			this.value = value;
		}
	}

	/** Compare and Swap
	 * Sets the passed value and returns true only if the actual value is the expected value.
	 * Returns false otherwise.
	 */
	public boolean compareAndSwapValue(final Value expected, final Value value)
	{
		if(this.value != expected)
		{
			return false;
		}
		this.value = value;
		return true;
	}

	/** getSet
	 * Combination of getter and setter:
	 * sets the new value and returns the old one
	 */
	public Value getSetValue(final Value value)
	{
		final Value oldValue = this.value;
		this.value = value;
		return oldValue;
	}

	/** "has" Method
	 * simple query method to check if a certain instance is set without having
	 * to grant public access to the set value
	 */
	public boolean hasValue(final Value value)
	{
		return this.value == value;
	}

	/** Internal Setter
	 * useful to sperate checks and actual assignment or to prevent multiple
	 * chain setter casting along class hierarchies
	 *
	 * note that convenience method chaining is hardly relevant
	 * for an implementation detail method like this
	 */
	protected void internalSetValue(final Value value)
	{
		this.value = value;
	}

	/** Property-like Getter
	 * Same as basic getter, but omits the "get".
	 * See String#length(), Collection#size(), etc.
	 *
	 * Note that the whole "Getter/Setter" pattern is actually only
	 * a clumsy workaround for missing property syntax in Java.
	 * This provokes thoughts like "why should I add unnecessary
	 * boiler plate code to my access methods?"
	 *
	 * The only legit counter argument to this is: "get~" and "set~"
	 * creates a natural categorization in IntelliSense as a byproduct.
	 *
	 * However, this would be nullified by the rule of thumb
	 * "reading is more important than writing in source code"
	 *
	 * As a conclusion, this little idea here means:
	 * As soon as proper property syntax is available in Java,
	 * getters and setters as pseudo properties are OUT.
	 *
	 * For the time being, a reasonable convention seems to be
	 * to omit the "get" only if there can be no corresponding setter
	 */
	public Value value()
	{
		return this.value;
	}

}

class Value {/**/}

