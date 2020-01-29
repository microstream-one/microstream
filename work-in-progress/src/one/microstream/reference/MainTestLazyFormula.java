package one.microstream.reference;

import java.text.DecimalFormat;

import one.microstream.chars.VarString;

public class MainTestLazyFormula
{
	
	public static void main(final String[] args)
	{
		System.out.println("-------\n  #1\n-------\n\n");
		calculate(1_000_000_000, 100_000_000, 100_000, 20_000);
		calculate(1_000_000_000, 100_000_000, 100_000, 50_000);
		calculate(1_000_000_000, 100_000_000, 100_000, 90_000);

		System.out.println("-------\n  #2\n-------\n\n");
		calculate(1_000_000_000, 550_000_000, 100_000, 20_000);
		calculate(1_000_000_000, 550_000_000, 100_000, 50_000);
		calculate(1_000_000_000, 550_000_000, 100_000, 90_000);

		System.out.println("-------\n  #3\n-------\n\n");
		calculate(1_000_000_000, 800_000_000, 100_000, 20_000);
		calculate(1_000_000_000, 800_000_000, 100_000, 50_000);
		calculate(1_000_000_000, 800_000_000, 100_000, 90_000);
	}
	
	
	static void calculate(final long memoryAvailable, final long memoryUsed, final long timeout, final long age)
	{
		final long memoryAvailableSh10 = shift10(memoryAvailable);
		final long memoryUsedSh10      = shift10(memoryUsed);
		final long ageSh10             = shift10(age);
		
		final long weightSh10 = ageSh10 / timeout;
		final long weightedUsedMemorySh10 = memoryUsedSh10 + memoryUsed * weightSh10;
		final boolean clear = weightedUsedMemorySh10 >= memoryAvailableSh10;
				
		final DecimalFormat f = new java.text.DecimalFormat("00,000,000,000");
		final int p = 17;
		
		final VarString vs = VarString.New()
			.add("Calculation:").lf()
			.add("Memory Available = ").padLeft(f.format(memoryAvailable)       , p, ' ').lf()
			.add("Memory Used      = ").padLeft(f.format(memoryUsed)            , p, ' ').lf()
			.add("Timeout          = ").padLeft(f.format(timeout)               , p, ' ').lf()
			.add("Age              = ").padLeft(f.format(age)                   , p, ' ').lf()
			.lf()
			.add("Sh10 Mem. Avail. = ").padLeft(f.format(memoryAvailableSh10)   , p, ' ').lf()
			.add("Sh10 Mem. Used   = ").padLeft(f.format(memoryUsedSh10)        , p, ' ').lf()
			.add("Sh10 Age         = ").padLeft(f.format(ageSh10)           , p, ' ').lf()
			.lf()
			.add("WeightSh10       = ").padLeft(f.format(weightSh10)            , p, ' ').lf()
			.add("W.UsedMemorySh10 = ").padLeft(f.format(weightedUsedMemorySh10), p, ' ').lf()
			.add("Sh10 Mem. Avail. = ").padLeft(f.format(memoryAvailableSh10)   , p, ' ').lf()
			.lf()
			.add("Clear            = ").add(clear).lf()
			.add("---------------------------------").lf()
			.lf()
		;
		System.out.println(vs);
	}
	
	private static long shift10(final long value)
	{
		// equals *1024, which is roughly *1000, but significantly faster and the precise factor doesn't matter.
		return value << 10;
	}
	
}
