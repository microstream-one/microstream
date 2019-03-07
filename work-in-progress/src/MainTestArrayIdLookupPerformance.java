import one.microstream.collections.HashEnum;

@SuppressWarnings("unused")
public class MainTestArrayIdLookupPerformance
{
	static final int LOOPS = 1000;
	static final int RUNS = 20;
	static final int SIZE = 16;
	static final String[] strings = new String[SIZE];
	static final HashEnum<String> stringSet = HashEnum.NewCustom(SIZE, 1f);

	static{
		for(int i = 0; i < SIZE; i++)
		{
			strings[i] = Integer.toString(i);
		}
		stringSet.addAll(strings);
	}


	public static void main(final String[] args)
	{
		final String subject = strings[SIZE/2];

		System.out.println("Dummy1:");
		System.out.println(dummy1(subject));

		System.out.println("Dummy2:");
		System.out.println(dummy2(subject));

		System.out.println("Array:");
		System.out.println(searchArray(subject));

		System.out.println("Set:");
		System.out.println(searchSet(subject));

//		System.out.println(stringSet.analyze());

	}


	static int dummy1(final String subject)
	{
		final String[] strings = MainTestArrayIdLookupPerformance.strings;
		final int i = 0;
		int dummy = 0;
		long tStart;
		long tStop;
		for(int k = 0; k < RUNS; k++)
		{
			tStart = System.nanoTime();
			for(int l = 0; l < LOOPS; l++)
			{
				dummy = l;
			}
			tStop = System.nanoTime();
			System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
		}
		return dummy;
	}
	static int dummy2(final String subject)
	{
		final String[] strings = MainTestArrayIdLookupPerformance.strings;
		final int i = 0;
		int dummy = 0;
		long tStart;
		long tStop;
		for(int k = 0; k < RUNS; k++)
		{
			tStart = System.nanoTime();
			for(int l = 0; l < LOOPS; l++)
			{
				dummy = l + l;
			}
			tStop = System.nanoTime();
			System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
		}
		return dummy;
	}

	static int searchSet(final String subject)
	{
		final HashEnum<String> stringSet = MainTestArrayIdLookupPerformance.stringSet;
		int i = 0;
		long tStart;
		long tStop;
		for(int k = 0; k < RUNS; k++)
		{
			tStart = System.nanoTime();
			for(int l = 0; l < LOOPS; l++)
			{
				if(stringSet.containsId(subject))
				{
					i++;
				}
			}
			tStop = System.nanoTime();
			System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
		}
		return i;
	}

	static int searchArray(final String subject)
	{
		final String[] strings = MainTestArrayIdLookupPerformance.strings;
		final int len = strings.length;
		int i = 0;
		long tStart;
		long tStop;
		for(int k = 0; k < RUNS; k++)
		{
			tStart = System.nanoTime();
			for(int l = 0; l < LOOPS; l++)
			{
				for(i = 0; i < len; i++)
				{
					if(strings[i] == subject) break;
				}
			}
			tStop = System.nanoTime();
			System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
		}
		return i;
	}

}
