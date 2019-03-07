package one.microstream.examples.higherorder;

import java.awt.event.ActionListener;
import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

import one.microstream.collections.BulkList;
import one.microstream.collections.types.XList;
import one.microstream.functional.XFunc;

/**
 * @author Thomas Muenz
 *
 */
public class NewHighOrderProgramming
{
	static XList<Person> persons = new BulkList<>(); // add elements, etc.

	static Predicate<Person> isAdult = p -> p.age >= 18;


	static Consumer<Person> doStuff = p -> {/*do stuff (call outer methods, etc.)*/};


	/**
	 * Very basic example of using higher order programming paradigm which has numerous advantages over the
	 * traditional {@link Iterator}-based iterating concept:
	 * <br>
	 * - 1 line of code instead of 5, muss less to type and even more important: less to read and understand
	 * <br>
	 * - no instance (iterator) has to be created each time the iteration is performed, meaning more performance and
	 *   less memory garbage of short-living helper instances
	 * <br>
	 * - no danger of the iteratation getting inconsistent during iterating it as iterator concept has.
	 * <br>
	 * - allows collections to internally iterate on their elements (and optimize, e.g. for batch-processing) for
	 *   applying the desired logic to each element instead of externally iterating element by element through
	 *   public single-element access methods.
	 * <br>
	 * - programm logic that used to be hardcoded methods can be put into variables, enabling furthr abstraction and
	 *   reduction of program complexity without the loss of functionality
	 * <br>
	 * - the functionality even increased despite the reduction of complexity as the logic-variables can be passed
	 *   along to other methods ({@link Comparator} or {@link ActionListener} already work that way today!)
	 */
	public static void main(final String[] args)
	{
		persons.iterate(XFunc.wrapWithPredicate(doStuff, isAdult));
	}



	void evenBetter()
	{
		// reuse, pass long, etc. logic-variables
		final XList<Person> underAged = persons.filterTo(new BulkList<Person>(), XFunc.not(isAdult));
		System.out.println(underAged);
	}
}
