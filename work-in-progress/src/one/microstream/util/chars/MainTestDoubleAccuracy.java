package one.microstream.util.chars;


public class MainTestDoubleAccuracy
{
	/* Conclusion:
	 * 15 digits always yield correct results
	 * 16 digits cause inaccuracies of prly +/- 1 in the last digit in about 10% cases ("near correct")
	 * 17 digits cause inaccuracies of prly +/- 8 in the last digit in about 90% cases ("remotely correct", almost noise)
	 * 18+ digits always cause information loss (IEEE standard defines 17 digits for bin64)
	 * leading zeroes are irrelevant for accuracy
	 */
	public static void main(final String[] args)
	{
		final int size = 1000;
		final int length = 17;
		final StringBuilder sb = new StringBuilder();
		int errorCount = 0;
		for(int i = 0; i < size; i++)
		{
			sb.append("0.00");
			for(int k = 0; k < length; k++)
			{
				sb.append((int)(1.0 + Math.random()*9.0));
			}
			final double d = Double.parseDouble(sb.toString());
			if(Double.toString(d).equals(sb.toString()))
			{
				//System.out.println("Created "+vs + " <-> "+d);
			}
			else
			{
				errorCount++;
				System.err.println("Error "+errorCount+": "+sb + " <-> "+d);
			}
			sb.setLength(0);
		}

		System.out.println(0.0074998255459389793d); // 7-digit discrepancy
	}
}
