package one.microstream.examples;

import java.util.function.Consumer;
import java.util.function.Predicate;

import one.microstream.X;
import one.microstream.collections.types.XAddingCollection;
import one.microstream.collections.types.XGettingList;
import one.microstream.collections.types.XList;

/**
 * @author Thomas Muenz
 *
 */
public class MainExampleCollections20
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	// specified variables
	final int n;
	final int k;
	final int l;

	// specified logic
	final Predicate<String> condition1 = new Predicate<String>() {
		@Override public boolean test(final String e) {
			return e != null && e.length() < MainExampleCollections20.this.l;
		}
	};
	final Consumer<String> procedure1 = new Consumer<String>() {
		@Override public void accept(final String e) {
			System.out.println(e);
		}
	};



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public MainExampleCollections20(final int n, final int k, final int l)
	{
		super();
		this.n = n;
		this.k = k;
		this.l = l;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	public void selectAndPrint(final XGettingList<String> strings)
	{
//		final int lastIndex = XTypes.to_int(strings.size()) - 1;
//		strings.rngIterate(lastIndex, -this.n, this.procedure1);
//		strings.rngIterate(lastIndex - this.n, -(strings.size()/2 - this.n), this.condition1, this.procedure1, this.n, this.k);
	}

	public <C extends XAddingCollection<String>> C select(final XGettingList<String> strings, final C target)
	{
//		final int lastIndex = XTypes.to_int(strings.size()) - 1;
//		strings.rngCopyTo(lastIndex, -this.n, target);
//		strings.rngCopyTo(lastIndex - this.n, -(strings.size()/2 - this.n), target, this.condition1, this.n, this.k);
		return target;
	}

	public void printAll(final XGettingList<String> strings)
	{
		strings.iterate(this.procedure1);
	}



	///////////////////////////////////////////////////////////////////////////
	//      main        //
	/////////////////////

	/**
	 * @param args
	 */
	public static void main(final String[] args)
	{
		final XList<String> strings = X.List(
			"Java",
			"Eclipse",
			"compiler",
			"VM",
			"class",
			"Object",
			"main",
			"syntax",
			"IDE",
			"interface",
			"String",
			"Collection"
		);

		final MainExampleCollections20 example = new MainExampleCollections20(2, 3, 5);

		example.selectAndPrint(strings);
//		example.printAll(strings);

//		final XList<String> selected = example.select(strings, list("already", "contained", "stuff"));
//		System.out.println(selected);
	}

}
