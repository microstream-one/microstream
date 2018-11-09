package net.jadoth.persistence.test;

import java.io.Serializable;

import net.jadoth.meta.XDebug;


abstract class AbstractPerson
{
	static final long   BASE_PERSON_CONST_INT1 = 0xBA;
	static final Object BASE_PERSON_CONST_OBJ1 = new Object();
	static final long   BASE_PERSON_CONST_INT2 = 0XBB;
	static final Object BASE_PERSON_CONST_OBJ2 = "basePerson1";

	static long    basePersonStaticValue1  = 0xBC;
	static String  basePersonAnyReference1 = "basePerson2";
	static long    basePersonStaticValue2  = 0xBD;
	static Object  basePersonAnyReference2 = new Object();

	boolean isMale = true;
	char initial = 0x2222;
	TestPerson friend;

	public AbstractPerson(final TestPerson friend)
	{
		super();
		this.friend = friend;
	}


}

class TestPerson extends AbstractPerson
{
	static final boolean TEST_PERSON_CONST_PRIM_BOOLEAN1  = true;
	static final Boolean TEST_PERSON_CONST_REFR_BOOLEAN2  = Boolean.FALSE;
	static final char    TEST_PERSON_CONST_PRIM_CHAR1     = '\t';
	static final long    TEST_PERSON_CONST_PRIM_LONG1     = 0xCA;
	static final Object  TEST_PERSON_CONST_REFR_OBJECT1   = new Object();
	static final long    TEST_PERSON_CONST_PRIM_LOGN2     = 0xCB;
	static final String  TEST_PERSON_CONST_REFR_STRING2   = "testPerson1";
	static final Object  TEST_PERSON_CONST_REFR_MEAN_NULL = null;

	static long   testPersonStaticValue1  = 0xCC;
	static Object testPersonAnyReference1 = new Object();
	static long   testPersonStaticValue2  = 0xCD;
	static String testPersonAnyReference2 = "testPerson2";

	byte age;
	short friendCount = 0x2121;
	TestPerson spouse;
	int value = 0x4141_4141;
	long id = 0x8181_8181_8181_8181L;
	float weight = Float.intBitsToFloat(0x4242_4242);
	double credits = Double.longBitsToDouble(0x8282_8282_8282_8282L);
	Serializable ser = null;
	TestInterfaceD tiD = null;
	TestInterfaceA tiA = null;

	public TestPerson(final int age)
	{
		this(age, null, null);
	}

	public TestPerson(final int age, final TestPerson spouse, final TestPerson friend)
	{
		super(friend);
		this.age    = (byte)age;
		this.spouse = spouse;
	}

	@Override
	public String toString()
	{
		return "Person"+this.age;
	}


	public static void DEBUG_printStaticState()
	{
		final String n = "\n";
		XDebug.println(
		"static final boolean TEST_PERSON_CONST_PRIM_BOOLEAN1  = "+TEST_PERSON_CONST_PRIM_BOOLEAN1 +n+
		"static final Boolean TEST_PERSON_CONST_REFR_BOOLEAN2  = "+TEST_PERSON_CONST_REFR_BOOLEAN2 +n+
		"static final char    TEST_PERSON_CONST_PRIM_CHAR1     = "+TEST_PERSON_CONST_PRIM_CHAR1    +n+
		"static final long    TEST_PERSON_CONST_PRIM_LONG1     = "+TEST_PERSON_CONST_PRIM_LONG1    +n+
		"static final Object  TEST_PERSON_CONST_REFR_OBJECT1   = "+TEST_PERSON_CONST_REFR_OBJECT1  +n+
		"static final long    TEST_PERSON_CONST_PRIM_LOGN2     = "+TEST_PERSON_CONST_PRIM_LOGN2    +n+
		"static final String  TEST_PERSON_CONST_REFR_STRING2   = "+TEST_PERSON_CONST_REFR_STRING2  +n+
		"static final Object  TEST_PERSON_CONST_REFR_MEAN_NULL = "+TEST_PERSON_CONST_REFR_MEAN_NULL+n+
		"static       long   testPersonStaticValue1  = "+testPersonStaticValue1 +n+
		"static       Object testPersonAnyReference1 = "+testPersonAnyReference1+n+
		"static       long   testPersonStaticValue2  = "+testPersonStaticValue2 +n+
		"static       String testPersonAnyReference2 = "+testPersonAnyReference2+n
		);
	}


}
