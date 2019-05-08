package one.microstream.chars;

import java.util.function.Consumer;

public interface StringTableProcessor<T>
{
	public <C extends Consumer<? super T>> C processStringTable(StringTable sourceData, C collector);



	public abstract class Abstract<T> implements StringTableProcessor<T>
	{
		protected abstract void validateColumnNames(StringTable sourceData);

		protected abstract T parseRow(String[] dataRow);

		@Override
		public final <C extends Consumer<? super T>> C processStringTable(
			final StringTable sourceData,
			final C           collector
		)
		{
			this.validateColumnNames(sourceData);
			sourceData.rows().iterate(new Consumer<String[]>()
			{
				@Override
				public void accept(final String[] dataRow)
				{
					collector.accept(Abstract.this.parseRow(dataRow));
				}
			});
			return collector;
		}
	}
}
