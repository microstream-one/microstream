package net.jadoth.test.collections;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Consumer;

import net.jadoth.X;
import net.jadoth.collections.BulkList;
import net.jadoth.collections.types.XList;
import net.jadoth.functional.JadothEqualators;
import net.jadoth.functional.JadothPredicates;
import net.jadoth.util.Equalator;
import net.jadoth.util.JadothExceptions;
import net.jadoth.util.JadothTypes;
import net.jadoth.util.chars.JadothChars;

/**
 * @author Thomas Muenz
 *
 */
public class TestJaList
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	static final Equalator<String> EQUAL_STRING = JadothEqualators.equality(String.class);

	static final char n = '\n';
	static final String[] singleAddValues = {
		"single0", "single1", "single2", "single3", "single4", "single5", "single6", "single7", "single8", "single9"
	};
	static final String[] arrayAddValues = {
		"array0", "array1", "array2", "array3", "array4", "array5", "array6", "array7", "array8", "array9"
	};
	static final BulkList<String> collectionAddValues = new BulkList<>(
		"coll0", "coll1", "coll2", "coll3", "coll4", "coll5", "coll6", "coll7", "coll8", "coll9"
	);

	static final BulkList<String> iterableAddValues = new BulkList<>(
		"iter0", "iter1", "iter2", "iter3", "iter4", "iter5", "iter6", "iter7", "iter8", "iter9"
	);



	public static void main(final String[] args)
	{
		test(new BulkList<String>());
		testRemoveIdenticalDuplicates(new BulkList<String>());
		testRemoveNulls(new BulkList<String>());
	}

	static XList<String> testRemoveIdenticalDuplicates(final XList<String> testee)
	{
		System.out.println("testRemoveIdenticalDuplicates:");

		testee.addAll(collectionAddValues);
		testee.addAll(collectionAddValues);
		testee.addAll(collectionAddValues);
		System.out.println(testee);

		testee.removeDuplicates();
		System.out.println(testee);

		if(JadothTypes.to_int(testee.size()) != JadothTypes.to_int(collectionAddValues.size()))
		{
			throw JadothExceptions.cutStacktraceByOne(new TestException());
		}

		return testee;
	}

	static void testRemoveNulls(final XList<String> testee)
	{
		System.out.println("testRemoveNulls:");
		testee.addAll(
			null, "A", "B", null, null, "C", null, "D", "E", "F", null, null, null
		);

		testee.removeAll(null);

		System.out.println(testee);
		if(JadothTypes.to_int(testee.size()) != 6)
		{
			throw new TestException();
		}
	}

	static XList<String> test(final XList<String> testee)
	{
		new TestJaList(testee)
		.testAddSingle()
		.testAddCollection()
		.testAddArray()
		.testAddIterable()
//		.testContains(arrayAddValues[1])
		.testContainsAll()
		.testIndexOf(collectionAddValues.at(0))
		.testLastIndexOf(singleAddValues[3])
		.testAddArray()
//		.testContains(arrayAddValues[0])
		.testLastIndexOf(arrayAddValues[5])
		.testToArray()
		.testProcess()
		.testInsertAtStart()
		.testInsertAtEnd()
		.testInsertBeforeEnd()
		.testInsertAtRandom()
		.testGetConsecutive()
		.testGetRandom()
		.testSize()
		.testIsEmpty()
		.testContainsAll()
		.testForeach()
		.testIterator()
		.testListIterator()
		.testRetainAll()

		.print()

		.testClear()
		.testSize()
		.testIsEmpty()
		.print()

		.done()
		;
		return testee;
	}




	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final XList<String> testee;
	private final BulkList<String> matcher;

	private final String nameTestee;
	private final String nameMatcher;
	private final int nameMaxLength;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * @param testee
	 */
	public TestJaList(final XList<String> testee)
	{
		super();
		this.testee = testee;
		this.matcher = new BulkList<>();

		this.nameTestee = testee.getClass().getSimpleName();
		this.nameMatcher = this.matcher.getClass().getSimpleName();
		this.nameMaxLength = Math.max(
			this.nameTestee.length(),
			Math.max(this.nameMatcher.length(), XList.class.getSimpleName().length())
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// tests           //
	////////////////////

	public TestJaList testAddSingle()
	{
		for(final String s : singleAddValues)
		{
			this.testee.add(s);
			this.matcher.add(s);
		}
		return this.check("testAddSingle");
	}

	public TestJaList testAddCollection()
	{
		this.testee.addAll(collectionAddValues);
		this.matcher.addAll(collectionAddValues);
		return this.check("testAddCollection");
	}

	public TestJaList testAddIterable()
	{
		this.testee.addAll(iterableAddValues);
		this.matcher.addAll(iterableAddValues);
		return this.check("testAddIterable");
	}

	public TestJaList testAddArray()
	{
		this.testee.addAll(arrayAddValues);
		for(final String s : arrayAddValues)
		{
			this.matcher.add(s);
		}
		return this.check("testAddArray");
	}

//	public TestJaList testContains(final String s)
//	{
//		return this.check("testContains", this.testee.contains(s, EQUAL_STRING), this.matcher.contains(s));
//	}

	public TestJaList testContainsAll()
	{
		final boolean testeeContains = this.testee.containsAll(collectionAddValues);
		final boolean matcherContains = this.matcher.containsAll(collectionAddValues);
		return this.check("testContainsAll", testeeContains, matcherContains);
	}

	public TestJaList testRetainAll()
	{
		final boolean testeeContains = this.testee.retainAll(collectionAddValues) > 0;
		final boolean matcherContains = this.matcher.retainAll(collectionAddValues) > 0;
		this.check("testRetainAll changed", testeeContains, matcherContains);
		return this.check("testRetainAll");
	}

	public TestJaList testIndexOf(final String s)
	{
		return this.check("testIndexOf", this.testee.indexBy(JadothPredicates.isEqualTo(s)), this.matcher.indexOf(s));
	}
	public TestJaList testLastIndexOf(final String s)
	{
		return this.check("testLastIndexOf", this.testee.lastIndexOf(s), this.matcher.lastIndexOf(s));
	}

	public TestJaList testProcess()
	{
		final int[] ir = X.ints(0);
		this.testee.iterate(new Consumer<String>(){
			@Override public void accept(final String element) {
				ir[0] = TestJaList.this.addLength(ir[0], element);
			}
		});

		int i2 = 0;
		for(final String s : this.matcher)
		{
			i2 = this.addLength(i2, s);
		}

		return this.check("testProcess", ir[0], i2);
	}
	int addLength(int currentLength, final String element)
	{
		if(element != null)
		{
			currentLength += element.length();
		}
		return currentLength;
	}


	public TestJaList testForeach()
	{
		int totalLengthTestee = 0;
		for(final String s : this.testee)
		{
			totalLengthTestee = this.addLength(totalLengthTestee, s);
		}

		int totalLengthMatcher = 0;
		for(final String s : this.matcher)
		{
			totalLengthMatcher = this.addLength(totalLengthMatcher, s);
		}
		return this.check("testForeach", totalLengthTestee, totalLengthMatcher);
	}

	public TestJaList testIterator()
	{
		int totalLengthTestee = 0;
		for(final Iterator<String> iterator = this.testee.iterator(); iterator.hasNext();)
		{
			final String s = iterator.next();
			totalLengthTestee = this.addLength(totalLengthTestee, s);
		}

		int totalLengthMatcher = 0;
		for(final Iterator<String> iterator = this.matcher.iterator(); iterator.hasNext();)
		{
			final String s = iterator.next();
			totalLengthMatcher = this.addLength(totalLengthMatcher, s);
		}
		return this.check("testIterator", totalLengthTestee, totalLengthMatcher);
	}

	public TestJaList testListIterator()
	{
		int totalLengthTestee = 0;
		for(final Iterator<String> iterator = this.testee.listIterator(); iterator.hasNext();)
		{
			final String s = iterator.next();
			totalLengthTestee = this.addLength(totalLengthTestee, s);
		}

		int totalLengthMatcher = 0;
		for(final Iterator<String> iterator = this.matcher.listIterator(); iterator.hasNext();)
		{
			final String s = iterator.next();
			totalLengthMatcher = this.addLength(totalLengthMatcher, s);
		}
		return this.check("testListIterator", totalLengthTestee, totalLengthMatcher);
	}


	public TestJaList testInsertAtStart()
	{
		for(int i = 1; i <= 3; i++)
		{
			final String s = "insertStart"+i;
			this.testee.input(0, s);
			this.matcher.input(0, s);
		}
		return this.check("testInsertAtStart");
	}

	public TestJaList testInsertAtEnd()
	{
		for(int i = 1; i <= 3; i++)
		{
			final String s = "insertEnd"+i;
			this.testee.input(JadothTypes.to_int(this.testee.size()), s);
			this.matcher.input(JadothTypes.to_int(this.matcher.size()), s);
		}
		return this.check("testInsertAtEnd");
	}


	public TestJaList testInsertBeforeEnd()
	{
		for(int i = 1; i <= 3; i++)
		{
			final String s = "insertBeforeEnd"+i;
			this.testee.input(JadothTypes.to_int(this.testee.size())-1, s);
			this.matcher.input(JadothTypes.to_int(this.matcher.size())-1, s);
		}
		return this.check("testInsertBeforeEnd");
	}

	public TestJaList testInsertAtRandom()
	{
		for(int i = 1; i <= 100; i++)
		{
			final int insertPos = this.randomIndex(this.ensureSameSize(null));
			final String s = "insertRandom"+i;
			this.testee.input(insertPos, s);
			this.matcher.input(insertPos, s);
		}
		return this.check("testInsertAtRandom");
	}

	public TestJaList testClear()
	{
		this.testee.clear();
		this.matcher.clear();
		return this.check("testClear", JadothTypes.to_int(this.testee.size()), JadothTypes.to_int(this.matcher.size()));
	}

	public TestJaList testToArray()
	{
		final Object[] testeeArray = this.testee.toArray();
		final Object[] matcherArray = this.matcher.toArray();
		return this.check("testToArray", Arrays.equals(testeeArray, matcherArray));
	}

	public TestJaList testGetConsecutive()
	{
		for(int i = 0, size = this.ensureSameSize("testGetConsecutive"); i < size; i++)
		{
			this.check(null, this.testee.at(i) == this.matcher.at(i));
		}
		this.pass("testGetConsecutive");
		return this;
	}

	public TestJaList testGetRandom()
	{
		final int size = this.ensureSameSize("testGetRandom");
		for(int i = 0; i < size; i++)
		{
			final int randomIndex = this.randomIndex(size);
			this.check(null, this.testee.at(randomIndex) == this.matcher.at(randomIndex));
		}
		this.pass("testGetRandom");
		return this;
	}

	public TestJaList testIsEmpty()
	{
		return this.check("testIsEmpty", this.testee.isEmpty(), this.matcher.isEmpty());
	}

	public TestJaList testSize()
	{
		return this.check("testSize", JadothTypes.to_int(this.testee.size()), JadothTypes.to_int(this.matcher.size()));
	}



	///////////////////////////////////////////////////////////////////////////
	// checks          //
	////////////////////

	private int ensureSameSize(final String testName)
	{
		final int sizeTestee = JadothTypes.to_int(this.testee.size());
		final int sizeMatcher = JadothTypes.to_int(this.matcher.size());
		this.check(testName == null ?null :testName+" ensureSameSize", sizeTestee, sizeMatcher);
		return sizeTestee;
	}

	private TestJaList check(final String testName)
	{
		boolean elementCheck = false;
		for(int i = 0, size = Math.max(JadothTypes.to_int(this.testee.size()), JadothTypes.to_int(this.matcher.size())); i < size; i++)
		{
			try
			{
				elementCheck = this.testee.at(i) == this.matcher.at(i);
			}
			catch(final Exception e)
			{
				this.fail(testName, e);
			}
			if(!elementCheck)
			{
				this.fail(testName);
			}
		}
		this.pass(testName);
		return this;
	}

	private TestJaList check(final String testName, final boolean result)
	{
		if(!result)
		{
			this.fail(testName);
		}
		this.pass(testName);
		return this;
	}

	private TestJaList check(final String testName, final boolean testeeBoolean, final boolean matcherBoolean)
	{
		if(testeeBoolean != matcherBoolean)
		{
			this.fail(testName);
		}
		this.pass(testName);
		return this;
	}

	private TestJaList check(final String testName, final long testeeInt, final long matcherInt)
	{
		if(testeeInt != matcherInt)
		{
			this.fail(testName+"("+testeeInt+" != "+matcherInt+")");
		}
		this.pass(testName);
		return this;
	}



	///////////////////////////////////////////////////////////////////////////
	// utils           //
	////////////////////

	private int randomIndex(final int size)
	{
		return (int)(Math.random() * size);
	}

	private void fail(final String checkName) throws TestException
	{
		throw JadothExceptions.cutStacktraceByOne(
		new TestException(checkName+" failed:"+n+
			XList.class.getSimpleName()+" ("+this.nameTestee+") size: "+JadothTypes.to_int(this.testee.size())+n+
			this.matcher.getClass().getSimpleName()+"size: "+JadothTypes.to_int(this.matcher.size())+n+
			n+
			this.compareString()
		));
	}
	private void fail(final String checkName, final Throwable cause) throws TestException
	{
		throw JadothExceptions.cutStacktraceByN(new TestException(checkName, cause), 2);
	}

	private void pass(final String checkName)
	{
		if(checkName == null) return;
		System.out.println("Passed "+checkName+".");
	}

	private TestJaList done()
	{
		System.out.println("Done.");
		return this;
	}

	private String compareString()
	{
		return this.padTypeName(XList.class.getSimpleName())+": "+this.testee.toString()+n+
		this.padTypeName(this.nameMatcher)+": "+this.matcher.toString()
		;
	}

	private String padTypeName(final String typeName)
	{
		return JadothChars.padSpace(typeName, this.nameMaxLength);
	}

	public TestJaList print()
	{
		System.out.println(n+this.compareString()+n);
		return this;
	}



	///////////////////////////////////////////////////////////////////////////
	// Exceptions      //
	////////////////////

	public static class TestException extends RuntimeException
	{

		public TestException()
		{
			super();
		}
		public TestException(final Throwable cause)
		{
			super(cause);
		}
		public TestException(final String message, final Throwable cause)
		{
			super(message, cause);
		}
		public TestException(final String message)
		{
			super(message);
		}
	}
}
