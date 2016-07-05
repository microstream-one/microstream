import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


public class MainTestJdkEqualityFail
{
	public static void main(final String[] args)
	{
		// die Konstruktoren sind auch nicht gerade bequem ... oder effizient (alles umkopieren!)
		final ArrayList<Integer>       arrayList  = new ArrayList<>(Arrays.asList(1, 2, 3));
		final LinkedList<Integer>      linkedList = new LinkedList<>(Arrays.asList(1, 2, 3));
		final ArrayList<List<Integer>> lists      = new ArrayList<>(Arrays.asList(arrayList, linkedList));
		lists.remove(linkedList);
		System.out.println(lists.get(0).getClass());
	}
}
