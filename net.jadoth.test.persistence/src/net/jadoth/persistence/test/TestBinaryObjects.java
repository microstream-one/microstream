package net.jadoth.persistence.test;

import static net.jadoth.math.XMath.sequence;

import java.util.Arrays;

import net.jadoth.meta.XDebug;

public class TestBinaryObjects
{
	protected static final Object[] orig_objects, objects;
	protected static final int[] indices;

	static {
		orig_objects = defineObjects();
		indices = sequence(orig_objects.length - 1);
		objects = orig_objects;

		// (07.04.2012)XXX: shuffling
//		orderByIndices(
//			orig_objects = defineObjects(),
//			indices = shuffle(sequence(orig_objects.length - 1)),
//			0,
//			objects = new Object[orig_objects.length]
//		);
		XDebug.debugln(
			"Test Arrays:\n"
			+Arrays.toString(orig_objects)+"\n"
			+Arrays.toString(indices)+"\n"
			+Arrays.toString(objects)+"\n"
		);
	}


	private static Object[] defineObjects()
	{
		final Object[] objects = {
			(byte)19,
			Boolean.TRUE,
			Boolean.FALSE,
			(short)29,
			'C',
			49,
			49.5f,
			89,
			89.5,
			"S",
			(byte)19,
			Boolean.TRUE,
			Boolean.FALSE,
			(short)29,
			'C',
			49,
			49.5f,
			89,
			89.5,
			"S",
//			new Object(),
			Object.class,
			new byte[]{},
			new byte[]{110},
			new byte[]{110, 111, 112},
			new boolean[]{},
			new boolean[]{true},
			new boolean[]{true, false, true},
			new short[]{},
			new short[]{230},
			new short[]{230, 231, 232},
			new char[]{},
			new char[]{'a'},
			new char[]{'a', 'b', 'c'},
			new int[]{},
			new int[]{450},
			new int[]{450, 451, 452},
			new float[]{},
			new float[]{460.5f},
			new float[]{460.5f, 461.5f, 462.5f},
			new long[]{},
			new long[]{870},
			new long[]{870, 871, 872},
			new double[]{},
			new double[]{880.5},
			new double[]{880.5, 881.5, 882.5},
		};
		return objects;
	}

	protected static TestPerson createTestPersons()
	{
		return new TestPerson(1,
			new TestPerson(2,
				new TestPerson(21),
				null
			),
			new TestPerson(3,
				null,
				null
			)
		);
	}
}

