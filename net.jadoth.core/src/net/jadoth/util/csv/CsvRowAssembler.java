package net.jadoth.util.csv;

import java.util.function.BiConsumer;

public interface CsvRowAssembler<R> extends BiConsumer<R, CsvAssembler>
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
