

import net.jadoth.collections.JadothSort;
import net.jadoth.collections.X;
import net.jadoth.collections.types.XList;

public class MainTestSortMulti
{
	static final String[] STRINGS = {
		"hallo"  ,
		"String" ,
		"main"   ,
		"Strings",
		"BAAAB"   ,
		"AAAAA"   ,
		"CAAAC"   ,
		"AAACA"
		
	};
	
	public static void main(final String[] args)
	{
		final XList<String> strings1 = X.List(STRINGS);
		
		System.out.println(strings1);
		System.out.println(
			strings1.copy().sort(
				(s1, s2) -> Integer.compare(s1.length(), s2.length())
			)
		);
		System.out.println(
			strings1.copy().sort(
				(s1, s2) -> Character.compare(s1.charAt(0), s2.charAt(0))
			)
		);
		System.out.println(
			strings1.copy().sort(
				JadothSort.chain(
					(s1, s2) -> Integer.compare(s1.length(), s2.length()),
					(s1, s2) -> Character.compare(s1.charAt(0), s2.charAt(0))
				)
			)
		);
		System.out.println(
			strings1.copy().sort(
				(s1, s2) ->
				JadothSort.evaluateComparisons(
					Integer.compare(s1.length(), s2.length()),
					Character.compare(s1.charAt(0), s2.charAt(0))
				)
			)
		);
		
		
		
	}
}
