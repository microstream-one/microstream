package doclink.test;

/**
 * TestSubjectClass with docLink to {@link doclink.test.TestTargetClass}:<br>
 * {@docLink doclink.test.TestTargetClass}
 * <p>
 * Fields in {@link doclink.test.TestTargetClass}:
 * 
 * {@docLink doclink.test.TestTargetClass#field0}<br>
 * {@code field0}
 * <p>
 * {@docLink doclink.test.TestTargetClass#field1}<br>
 * {@code field1}
 * <p>
 * {@docLink doclink.test.TestTargetClass#field2}<br>
 * {@code field2}
 * <p>
 * {@docLink doclink.test.TestTargetClass#field3}<br>
 * {@code field3}
 * <p>
 * 
 * @author TM
 *
 */
public class TestSubjectClass
{

	/**
	 * TestSubjectClass.method3(int) with docLink to {@link TestSubjectClass#method3(int, String)}:<br>
	 * {@docLink TestTargetClass#method3(int, String)}
	 * 
	 * @param value {@docLink TestTargetClass#method3(int):value}
	 * 
	 * @return {@docLink TestTargetClass#method3(int)@return}
	 */
	public String method3(final int value)
	{
		/* empty */
		return null;
	}

	/**
	 *
	 * TestSubjectClass.method3(int, String) with docLink to {@link TestTargetClass#method3(int, String)}:<br>
	 * {@docLink TestTargetClass#method3(int, String)}
	 *
	 * @param value1 {@docLink TestTargetClass#method3(int, String):}
	 * @param value2 {@docLink TestTargetClass#method3(int, String):}
	 *
	 * @return {@docLink TestTargetClass#method3(int, String)@return}
	 *
	 * @see {@docLink TestTargetClass#method3(int, String)@see:0}
	 * @see {@docLink TestTargetClass#method3(int, String)@see:1}
	 * @see {@docLink TestTargetClass#method3(int, String)@see:2}
	 */
	public String method3(final int value1, final String value2)
	{
		/* empty */
		return null;
	}
	
	/**
	 * TestSubjectClass.method3(int, int) with docLink to {@literal TestTargetClass#method3(int, int)}:<br>
	 * {@docLink TestTargetClass#method3(int, int)}
	 *
	 * @param value1 {@docLink TestTargetClass#method3(int, int):}
	 * @param value2 {@docLink TestTargetClass#method3(int, int):}
	 *
	 * @return {@docLink TestTargetClass#method3(int, int)@return}
	 */
	public String method3(final int value1, final int value2)
	{
		/* empty */
		return null;
	}
	
}
