package net.jadoth.csv;

import net.jadoth.functional.BiProcedure;

public interface CsvRowAssembler<R> extends BiProcedure<R, CsvAssembler>
{
	public static void addNonNullDelimited(final CharSequence s, final CsvAssembler assembler)
	{
		if(s == null)
		{
			return;
		}
		assembler.addRowValueDelimited(s);
	}

	public static void addNonNullSimple(final CharSequence s, final CsvAssembler assembler)
	{
		if(s == null)
		{
			return;
		}
		assembler.addRowValueSimple(s);
	}



	@Override
	public void accept(R row, CsvAssembler rowAssembler);

}
