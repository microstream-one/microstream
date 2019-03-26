package one.microstream.util.chars;

import static one.microstream.collections.XArrays.shuffle;
import static one.microstream.math.XMath.random;

import java.text.DecimalFormat;

import one.microstream.X;
import one.microstream.chars.Levenshtein;
import one.microstream.chars.VarString;
import one.microstream.collections.HashEnum;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.collections.types.XGettingList;
import one.microstream.meta.XDebug;
import one.microstream.util.matching.MultiMatch;
import one.microstream.util.matching.MultiMatchAssembler;
import one.microstream.util.matching.MultiMatcher;

public class MainTestMultiMatcher
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


	static final MultiMatcher<String> STRING_MATCHER = MultiMatcher.<String>New()
		.setEqualator(null)
		.setSimilator(Levenshtein::substringSimilarity)
		.setValidator(MainTestMultiMatcher::printMatch)
	;
	
	private static final DecimalFormat FORMAT = MultiMatchAssembler.Defaults.defaultSimilarityFormatter();

	
	public static boolean printMatch(
		final String sourceItem          ,
		final String targetItem          ,
		final double similarity          ,
		final int    sourceCandidateCount,
		final int    targetCandidateCount
	)
	{
		System.out.println("matching\t" + sourceItem + "\t<-" + FORMAT.format(similarity) + "->\t" + targetItem);
		return true;
	}

	static void testSimple()
	{
		final MultiMatch<String> match = STRING_MATCHER.match(src, trg);

		System.out.println("INPUT:");
		XDebug.printCollection(src, null, "\t", null, null);
		XDebug.printCollection(trg, null, "\t", null, null);
		System.out.println();
		System.out.println("OUTPUT:");
		System.out.println(match.assembler().assembleMappingSchemeVertical(VarString.New()));
		System.out.println(match.assembler().assembleMappingSchemeHorizontal(VarString.New()));
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
		MultiMatch<String> match = STRING_MATCHER.match(src, trg);


		for(int r = LOOPS; r --> 0;)
		{
			long tStart, tStop;
			tStart = System.nanoTime();
			match = STRING_MATCHER.match(src, trg);
			tStop = System.nanoTime();
			System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
		}


		System.out.println("INPUT:");
		XDebug.printCollection(src, null, "\t", null, null);
		XDebug.printCollection(trg, null, "\t", null, null);
		System.out.println();
		System.out.println("OUTPUT:");
		System.out.println(match.assembler().assembleMappingSchemeHorizontal(VarString.New()));
	}

	public static void main(final String[] args)
	{
		testSimple();
//		testRandomGenerated();
//		testGenerated();
	}

}
