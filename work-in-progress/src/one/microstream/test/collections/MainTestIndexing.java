package one.microstream.test.collections;

import static one.microstream.X.box;
import static one.microstream.math.XMath.sequence;

import java.util.Arrays;
import java.util.function.Predicate;

import one.microstream.collections.BulkList;
import one.microstream.collections.XUtilsCollection;

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
