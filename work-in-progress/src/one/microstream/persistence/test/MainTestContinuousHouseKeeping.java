package one.microstream.persistence.test;

public class MainTestContinuousHouseKeeping extends TestStorage
{
	public static void main(final String[] args)
	{
		ROOT.set(testGraphEvenMoreManyType());
//		ROOT.set(testGraph());
		testContinuousHouseKeeping();
		exit();
	}
}
