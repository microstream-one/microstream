package net.jadoth.test.collections;

import static net.jadoth.X.box;
import static net.jadoth.math.JadothMath.sequence;

import java.util.Arrays;
import java.util.function.Predicate;

import net.jadoth.collections.BulkList;
import net.jadoth.collections.XUtilsCollection;

public class MainTestIndexing
{
	static final int SIZE = 10;


	public static void main(final String[] args)
	{
		final BulkList<Integer> ints = BulkList.New(box(sequence(SIZE - 1)));
		XUtilsCollection.shuffle(ints);

		System.out.println("values:  "+ints);
		System.out.println("indices: "+Arrays.toString(XUtilsCollection.index(ints, new Predicate<Integer>() {
			@Override public boolean test(final Integer e){
				return e >= SIZE/2;
			}
		})));

	}
}
