package doclink.test;

import doclink.DocLink;

/**
 * 
 * @author TM
 *
 */
public class TestTargetClass
{
	///////////////////////////////////////////////////////////////////////////
	// fields //
	///////////
	
	// field without JavaDoc
	private int field0;
	
	/**
	 * field1 with simple JavaDoc.
	 */
	private int field1;
	
	/**
	 * field2 with description and a tag, unlike {@link #field1}.
	 * 
	 * @see #field1
	 */
	private int field2;
	
	/**
	 * {@literal field3} with description and multiple various tags. And with more than the first sentence.
	 * Because that is a special case in JavaDoc.
	 * <p>
	 * And here is some code:<br>
	 * {@code this.field4 = 42;}
	 * <p>
	 * Tags are mixed intentionally, don't clean up.
	 * 
	 * @see #field1
	 * @see #field2
	 * @since Java 37
	 * @author me, of course.
	 * @see String#toString()
	 * @see doclink.DocLink#containsDocLink(String)
	 */
	private int field3;
	
		
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	// method without JavaDoc
	public void method0()
	{
		/* empty */
	}

	/**
	 * method1 with simple JavaDoc.
	 */
	public void method1()
	{
		/* empty */
	}
	
	/**
	 * method2 with simple JavaDoc and a return tag.
	 * 
	 * @return method2 returns null.
	 */
	public String method2()
	{
		/* empty */
		return null;
	}
	
	/**
	 * method3 with JavaDoc and a return tag.
	 * <p>
	 * Also an additional description blabla.<br>
	 * More bla.<br>
	 * So much bla.
	 * 
	 * @return method3 returns null.
	 * 
	 * @see #method1()
	 * @since Java 37
	 * @author me, of course.
	 * @see TestTargetClass#method2()
	 */
	public String method3()
	{
		/* empty */
		return null;
	}
	
	/**
	 * method3(p1a) overloaded variant of method3.
	 * <p>
	 * Also text.
	 * 
	 * @param value single-param method3's int parameter.
	 * 
	 * @return method3(p1a) suprisingly still returns null.
	 * @see #method3()
	 * @see #method3(String)
	 * @see DocLink#determineEffectiveParameterName(String, String)
	 */
	public String method3(final int value)
	{
		/* empty */
		return null;
	}

	
	/**
	 * method3(p1b) overloaded variant of method3.
	 * <p>
	 * Also text.
	 * 
	 * @param value single-param method3's {@link String} parameter.
	 * 
	 * @return method3(p1b) suprisingly still returns null.
	 * @see #method3()
	 * @see #method3(String)
	 * @see DocLink#determineEffectiveParameterName(String, String)
	 */
	public String method3(final String value)
	{
		/* empty */
		return null;
	}

	/**
	 * method3(p2a) overloaded variant of method3.
	 * <p>
	 * Also text.
	 * 
	 * @param value1 dual-param method3's int parameter {@literal value1}.
	 * @param value2 dual-param method3's {@link String} parameter {@literal value2}.
	 * 
	 * @return method3(p2a) suprisingly still returns null.
	 * @see #method3()
	 * @see #method3(String)
	 * @see DocLink#determineEffectiveParameterName(String, String)
	 */
	public String method3(final int value1, final String value2)
	{
		/* empty */
		return null;
	}

	/**
	 * method3(p2b) overloaded variant of method3.
	 * <p>
	 * Also text.
	 * Nested docLink: {@docLink doclink.DocLink#determineEffectiveParameterName(String, String):extraIdentifier}.
	 * 
	 * @param value1 dual-param method3's int parameter {@literal value1}.
	 * @param value2 dual-param method3's int parameter {@literal value2}.
	 * 
	 * @return method3(p2b) suprisingly still returns null.
	 * @see #method3()
	 * @see #method3(String)
	 * @see DocLink#determineEffectiveParameterName(String, String)
	 */
	public String method3(final int value1, final int value2)
	{
		/* empty */
		return null;
	}
	
}
