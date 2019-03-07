package one.microstream.experimental;

import one.microstream.experimental.basic.TypeA;
import one.microstream.experimental.basic.TypeB;
import one.microstream.experimental.multipleInheritence.MyTypeA1B1;

@SuppressWarnings("unused")
public class MainTestMultipleInheritence
{

	public static void main(final String[] args)
	{
		MyTypeA1B1 mA1B1 = new MyTypeA1B1();
		TypeA tA = mA1B1;
		TypeB tB = mA1B1;
		System.out.println(mA1B1);
	}

}
