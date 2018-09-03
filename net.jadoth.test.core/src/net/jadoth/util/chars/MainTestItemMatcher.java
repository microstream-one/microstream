package net.jadoth.util.chars;

import static net.jadoth.collections.XArrays.shuffle;
import static net.jadoth.math.XMath.random;

import java.util.function.BiConsumer;

import net.jadoth.X;
import net.jadoth.chars.Levenshtein;
import net.jadoth.chars.VarString;
import net.jadoth.collections.HashEnum;
import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.collections.types.XGettingList;
import net.jadoth.meta.XDebug;
import net.jadoth.util.matching.ItemMatch;
import net.jadoth.util.matching.ItemMatchResult;
import net.jadoth.util.matching.ItemMatcher;

public class MainTestItemMatcher
{
	static final boolean normalize = true;

	static final int SIZE_GENERATED = 1000;
	static final int LOOPS = 100;


	//     0            1           2        3               4               5             6               7
	static final XGettingList<String> src = X.List(
		"firstname", "lastname" , "age", "emailAddress", "postalAddress", "noteLink", "newColumn1", "someMiscAddress"
	);
	static final XGettingList<String> trg = X.List(
		"Name"     , "Firstname", "Age", "Address"     , "Freetext"     , "Email",    "OtherAddress"
	);


	static final ItemMatcher<String> STRING_MATCHER_FACTORY = new ItemMatcher.Implementation<String>()
		.setEqualator(null)
		.setSimilator(Levenshtein::substringSimilarity)
		.setMatchCallback(MainTestItemMatcher::printMatch)
	;

	
	public static boolean printMatch(
		final String sourceItem          ,
		final String targetItem          ,
		final double similarity          ,
		final int    sourceCandidateCount,
		final int    targetCandidateCount
	)
	{
		System.out.println("matching\t" + sourceItem + "\t<-" + similarity + "->\t" + targetItem);
		return true;
	}

	
	public static void join(final VarString vc, final Object e)
	{
		vc.add(e);
	}
	
	// because a default context concise "::join" would have been too much to ask.
	public static final BiConsumer<VarString, Object> join = MainTestItemMatcher::join;


	static void testSimple()
	{
		final ItemMatch<String> match = STRING_MATCHER_FACTORY.match(src, trg);

		System.out.println("INPUT:");
		XDebug.printCollection(src, null, "\t", null, null);
		XDebug.printCollection(trg, null, "\t", null, null);
		System.out.println();
		System.out.println("OUTPUT:");
		System.out.println(ItemMatcher.Static.assembleMappingSchemeVertical(match, VarString.New(), join));

		final ItemMatchResult<String> result = match.getResult();
		System.out.println(ItemMatcher.Static.assembleMappingSchemeHorizontal(result, VarString.New(), join));
	}


	static void testRandomGenerated()
	{
		final String[] sourceStrings = new String[SIZE_GENERATED];
		for(int i = 0; i < SIZE_GENERATED; i++)
		{
			sourceStrings[i] = ""+random(SIZE_GENERATED);
		}

		final String[] targetStrings = new String[SIZE_GENERATED];
		for(int i = 0; i < SIZE_GENERATED; i++)
		{
			targetStrings[i] = ""+random(SIZE_GENERATED);
		}

		long tStart, tStop;
		tStart = System.nanoTime();
//		final ItemMatch<String> match = STRING_MATCHER_FACTORY.match(EqHashEnum.New(sourceStrings), EqHashEnum.New(targetStrings));
		tStop = System.nanoTime();
		System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));

//		System.out.println("OUTPUT:");
//		System.out.println(ItemMatcher.Static.assembleMappingSchemeVertical(match, LargeVarString(), VarString.objectToString));
	}

	static void testGenerated()
	{
		final String[] sourceStrings = new String[SIZE_GENERATED];
		for(int i = 0; i < SIZE_GENERATED; i++)
		{
			sourceStrings[i] = "s"+i;
		}

		final String[] targetStrings = new String[SIZE_GENERATED];
		for(int i = 0; i < SIZE_GENERATED; i++)
		{
			targetStrings[i] = "t"+i;
		}

		final XGettingEnum<String> src = HashEnum.New(shuffle(sourceStrings));
		final XGettingEnum<String> trg = HashEnum.New(shuffle(targetStrings));
		ItemMatch<String> match = STRING_MATCHER_FACTORY.match(src, trg);


		for(int r = LOOPS; r --> 0;)
		{
			long tStart, tStop;
			tStart = System.nanoTime();
			match = STRING_MATCHER_FACTORY.match(src, trg);
			tStop = System.nanoTime();
			System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
		}


		System.out.println("INPUT:");
		XDebug.printCollection(src, null, "\t", null, null);
		XDebug.printCollection(trg, null, "\t", null, null);
		System.out.println();
		System.out.println("OUTPUT:");
		System.out.println(ItemMatcher.Static.assembleMappingSchemeHorizontal(match.getResult(), VarString.New(), join));
	}

	public static void main(final String[] args)
	{
		testSimple();
//		testRandomGenerated();
//		testGenerated();
	}

}
