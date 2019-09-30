package various;
import java.util.ArrayList;
import java.util.List;


@SuppressWarnings("unused")
public class CovarianceAndContravariance
{
	static void arrays()
	{
		final Object[] a1 = new Object[10];
		final String[] a2 = new String[10];

//		String[] a3 = new Object[10]; // arrays are not implicitly contravariant

		final Object[] a4 = new String[10]; // arrays are implicitly covariant. Convenient, but wrong!
		a4[0] = "Hello World";        // correct element type for the array instance "by chance"
		a4[1] = 42;                   // type system loophole (valid syntax, but runtime error)

	}

	static void genericCollections()
	{
		// simple Generics typing


		final List<Object> c1 = new ArrayList<>();           // typed "exactely"/"only" to Object
		final List<String> c2 = new ArrayList<>();           // typed "exactely"/"only" to String

//		final List<String> c3 = new ArrayList<Object>();           // generics are not implicitly contravariant

//		final List<Object> c4 = new ArrayList<String>();           // generics are NOT implicitly covariant! (no more loophole)



		// Proper covariant and contravariant typing:  type wildcards with lower bound or upper bound

		final List<? super String> c5 = new ArrayList<Object>();   // typed to "String or anything narrower"

		final List<? extends Object> c6 = new ArrayList<String>(); // typed to "Object or anything wider"

//		final List<Object> c7 = new ArrayList<String>();           // Object is not "anything", but "exactely"/"only" Object!

		List<?> c;                                           // typed to "actual anything"
		c = new ArrayList<>();
		c = new ArrayList<String>();


//		final Collection<? extends String> cInvalid = new ArrayList<? extends String>(); // wildcards are only for declarations!

	}


	public static void main(final String[] args)
	{
		arrays();
	}





}
