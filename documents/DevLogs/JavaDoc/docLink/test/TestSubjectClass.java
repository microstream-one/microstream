package doclink.test;

/**
 * TestSubjectClass with docLink to {@link doclink.test.TestTargetClass}:<br>
 * {@linkDoc doclink.test.TestTargetClass}
 * <p>
 * Fields in {@link doclink.test.TestTargetClass}:
 * 
 * {@linkDoc doclink.test.TestTargetClass#field0}<br>
 * {@code field0}
 * <p>
 * {@linkDoc doclink.test.TestTargetClass#field1}<br>
 * {@code field1}
 * <p>
 * {@linkDoc doclink.test.TestTargetClass#field2}<br>
 * {@code field2}
 * <p>
 * {@linkDoc doclink.test.TestTargetClass#field3}<br>
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
	 * {@linkDoc TestTargetClass#method3(int, String)}
	 * <p>
	 * Test for local-to-global-reference transformation:
	 * {@linkDoc TestTargetClass#method3(int)@see:0}
	 * 
	 * @param value {@linkDoc TestTargetClass#method3(int):value}
	 * 
	 * @return {@linkDoc TestTargetClass#method3(int)@return}
	 */
	public String method3(final int value)
	{
		/* empty */
		return null;
	}

	/**
	 *
	 * TestSubjectClass.method3(int, String) with docLink to {@link TestTargetClass#method3(int, String)}:<br>
	 * {@linkDoc TestTargetClass#method3(int, String)}
	 *
	 * @param value1 {@linkDoc TestTargetClass#method3(int, String):}
	 * @param value2 {@linkDoc TestTargetClass#method3(int, String):}
	 *
	 * @return {@linkDoc TestTargetClass#method3(int, String)@return}
	 *
	 * @see {@linkDoc TestTargetClass#method3(int, String)@see:0}
	 * @see {@linkDoc TestTargetClass#method3(int, String)@see:1}
	 * @see {@linkDoc TestTargetClass#method3(int, String)@see:2}
	 */
	public String method3(final int value1, final String value2)
	{
		/* empty */
		return null;
	}
	
	/**
	 * TestSubjectClass.method3(int, int) with docLink to {@literal TestTargetClass#method3(int, int)}:<br>
	 * {@linkDoc TestTargetClass#method3(int, int)}
	 *
	 * @param value1 {@linkDoc TestTargetClass#method3(int, int):}
	 * @param value2 {@linkDoc TestTargetClass#method3(int, int):}
	 *
	 * @return {@linkDoc TestTargetClass#method3(int, int)@return}
	 */
	public String method3(final int value1, final int value2)
	{
		/* empty */
		return null;
	}
	
}
