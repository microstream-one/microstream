package eclipseSyntaxColoringFixed;

// syntax color compendium class

/**
 * Description of <code>ClassName</code>.
 * {@link com.someCompany.aPackage.Interface}
 * @author author
 * @deprecated use <code>OtherClass</code>
 */
@Deprecated
public abstract class ClassName<E> extends SomeClass implements SomeInterface<String>
{
	enum Color { RED, GREEN, BLUE }

	/* This comment may span multiple lines. */
	static Object staticField;

	// This comment may span only this line
	private E                 field ;
	private AbstractClassName field2;

	// TODO: refactor
	@SuppressWarnings(value="all")
	public int foo(final Integer parameter)
	{
		this.abstractMethod(this.inheritedField);

		staticMethod(this.field, this.field2);

		final int local = 42 * this.hashCode();
		return methodCall(local) + parameter;
	}

}