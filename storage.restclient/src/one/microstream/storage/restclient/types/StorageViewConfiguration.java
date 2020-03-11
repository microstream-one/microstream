
package one.microstream.storage.restclient.types;

import one.microstream.collections.EqHashTable;
import one.microstream.collections.types.XGettingTable;
import one.microstream.collections.types.XTable;

public interface StorageViewConfiguration
{
	public long elementRangeMaximumLength();
	
	public boolean compactSingleValueTypes();
	
	public ValueRenderer valueRenderer(String typeName);
	
	
	public static StorageViewConfiguration Default()
	{
		return new StorageViewConfiguration.Default(
			100,
			true,
			DefaultValueRenderers()
		);
	}
	
	public static XGettingTable<String, ValueRenderer> DefaultValueRenderers()
	{
		final XTable<String, ValueRenderer> valueRenderers = EqHashTable.New();
		valueRenderers.put("java.lang.String", ValueRenderer.StringLiteral   ());
		valueRenderers.put("char"            , ValueRenderer.CharacterLiteral());
		return valueRenderers;
	}
	
	public static StorageViewConfiguration New(
		final long elementRangeMaximumLength,
		final boolean compactSingleValueTypes,
		final XGettingTable<String, ValueRenderer> valueRenderers
	)
	{
		return new StorageViewConfiguration.Default(
			elementRangeMaximumLength,
			compactSingleValueTypes,
			valueRenderers.immure()
		);
	}
	
	
	public static class Default implements StorageViewConfiguration
	{
		private final long                                 elementRangeMaximumLength;
		private final boolean                              compactSingleValueTypes;
		private final XGettingTable<String, ValueRenderer> valueRenderers;
		
		Default(
			final long elementRangeMaximumLength,
			final boolean compactSingleValueTypes,
			final XGettingTable<String, ValueRenderer> valueRenderers
		)
		{
			super();
			this.elementRangeMaximumLength = elementRangeMaximumLength;
			this.compactSingleValueTypes   = compactSingleValueTypes;
			this.valueRenderers            = valueRenderers;
		}
		
		@Override
		public long elementRangeMaximumLength()
		{
			return this.elementRangeMaximumLength;
		}
		
		@Override
		public boolean compactSingleValueTypes()
		{
			return this.compactSingleValueTypes;
		}

		@Override
		public ValueRenderer valueRenderer(
			final String typeName
		)
		{
			return this.valueRenderers != null
				? this.valueRenderers.get(typeName)
				: null;
		}
		
	}
	
}
