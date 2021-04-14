package one.microstream.util.xcsv;

import java.util.function.BiConsumer;

public interface XCsvRowAssembler<R> extends BiConsumer<R, XCsvAssembler>
{
	public static void addNonNullDelimited(final CharSequence s, final XCsvAssembler assembler)
	{
		if(s == null)
		{
			return;
		}
		assembler.addRowValueDelimited(s);
	}

	public static void addNonNullSimple(final CharSequence s, final XCsvAssembler assembler)
	{
		if(s == null)
		{
			return;
		}
		assembler.addRowValueSimple(s);
	}



	@Override
	public void accept(R row, XCsvAssembler rowAssembler);

}
