package net.jadoth.test.collections;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 *
 */

/**
 * @author Thomas Muenz
 *
 */
public class MainTestJDKCollectionBug
{


	/**
	 * @param args
	 */
	public static void main(final String[] args)
	{
		final List<String> VALUES = Arrays.asList("A", "B", "C");
		final ArrayList<List<String>> lists = new ArrayList<>();

		final LinkedList<String> linkedList = new LinkedList<>(VALUES);
		final ArrayList<String> arrayList = new ArrayList<>(VALUES);


		System.out.println(
			"Adding "+linkedList.getClass().getCanonicalName()+" linkedList "+linkedList+" to lists, index "+lists.size()
		);
		lists.add(linkedList);

		System.out.println(
			"Adding "+arrayList.getClass().getCanonicalName()+"  arrayList  "+arrayList+" to lists, index "+lists.size()
		);
		lists.add(arrayList);

		System.out.println("");
		/*
		 * the bug is: diffuse use and mix up of .equals()'s double meaning of
		 * a) comparing identities and b) comparing content in JDK classes (or conceptual in Java itself).
		 * ArrayList (and probably all other collections as well) themselves compare only their elements,
		 * neither list types nor identity itself.
		 *
		 * But for queries on a list on the other hand, .equals() is actually treated as pseudo-identity
		 * for the list's elements
		 * (which is wrong if equals() is implemented to compare only contents like lists themselves do)
		 *
		 * So querying for example a list of lists will behave buggy
		 */
		System.out.println("lists.indexOf(arrayList): "+lists.indexOf(arrayList));

		System.out.println("");
		/*
		 * this can be fatal!
		 * e.g.: you need to remove the arrayList for performance reasons in later processing and keep the more
		 * flexible linkedList (because heavy inserting will follow etc.).
		 * The collection implementations can't provide enough functionality to do this right
		 * (= remove the actual identity, independant from content equality)
		 *
		 * As a result, the LinkedList is removed and the ArrayList is kept.
		 * (=> programm broken)
		 */
		System.out.println("lists.remove(arrayList)");
		lists.remove(arrayList);
		System.out.println("remaining elements:");
		for(final List<String> list : lists)
		{
			System.out.println(" - "+list.getClass().getCanonicalName());
		}

		/*
		 * This is only a simple example to show that the JDK collections massively lack functionality.
		 * (Partly even that the whole "equality" concept around Object.equals() is flawed in Java).
		 * Other examples would be numerous badly missing generic procedures
		 * (like max(), min(), copy(), you name it) that have to be provided by collections interfaces and theirs
		 * implementations.
		 * Static util methods that instantiate overkill Iterators and or copy everything back and forth betweeen
		 * temporary arrays all the time are no proper alternative but more like an emergency workaround.
		 */


		/*
		 * Conclusion:
		 * There HAS to be a way to distinct between object identity and object equality (of differently implemented
		 * kinds) when using collections. Either by distinct implementations
		 * (like IdentityArrayList, EqualityArrayList, etc), or, to be more flexible,
		 * by providing both identity- and equality-queries on the same implementation
		 * (e.g. you want to query the list for all elements equal to object A but later on remove all elements
		 * from the list that actually are object B. Copying everything back and forth to and from an alternative
		 * implementation all the time will kill application performance in no time.
		 * (And then people will argue that java is inherently slow, of course ^^)
		 *
		 * This distinction (among many other features) is what XCollections do.
		 */

	}

}
