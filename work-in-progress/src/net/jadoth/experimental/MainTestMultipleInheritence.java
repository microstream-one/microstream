package net.jadoth.experimental;

import net.jadoth.experimental.basic.TypeA;
import net.jadoth.experimental.basic.TypeB;
import net.jadoth.experimental.multipleInheritence.MyTypeA1B1;

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
