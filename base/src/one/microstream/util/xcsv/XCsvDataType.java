package one.microstream.util.xcsv;

import one.microstream.collections.EqConstHashTable;
import one.microstream.collections.EqHashTable;
import one.microstream.math.XMath;
import one.microstream.util.xcsv.XCSV.ValueSeparatorWeight;

public enum XCsvDataType
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////
	
	XCSV(
		"xcsv",
		map(
			vc('\t', 1.3),
			vc( ';', 1.2),
			vc( ',', 1.1),
			vc( '|', 1.0),
			vc( ':', 0.9)
		)
	),
	TSV(
		"tsv",
		map(
			vc('\t', 1.3),
			vc( ';', 1.2),
			vc( ',', 1.1),
			vc( '|', 1.0),
			vc( ':', 0.9)
		)
	),
	CSV(
		"csv",
		map(
			vc('\t', 1.1),
			vc( ';', 1.2), // "," is standard, see https://en.wikipedia.org/wiki/Comma-separated_values
			vc( ',', 1.3), // "," is standard, see https://en.wikipedia.org/wiki/Comma-separated_values
			vc( '|', 1.0),
			vc( ':', 0.9)
		)
	);
	
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	final static ValueSeparatorWeight vc(final char valueSeparator, final double weight)
	{
		return new ValueSeparatorWeight.Default(
			one.microstream.util.xcsv.XCSV.validateValueSeparator(valueSeparator),
			XMath.positive((float)weight)
		);
	}
	
	final static EqConstHashTable<Character, ValueSeparatorWeight> map(final ValueSeparatorWeight... weights)
	{
		final EqHashTable<Character, ValueSeparatorWeight> table = EqHashTable.New();
		
		for(final ValueSeparatorWeight weight : weights)
		{
			table.add(Character.valueOf(weight.valueSeparator()), weight);
		}
		
		return table.immure();
	}
	
	final static char determinePreferredValueSeparator(
		final Iterable<? extends ValueSeparatorWeight> weights
	)
	{
		float maxWeight     = -1.0f;
		char  maxWeightChar = '0';
		
		for(final ValueSeparatorWeight weight: weights)
		{
			if(weight.weight() >= maxWeight)
			{
				maxWeight = weight.weight();
				maxWeightChar = weight.valueSeparator();
			}
		}
		
		if(maxWeightChar == '0')
		{
			// (13.02.2020 TM)EXCP: proper exception
			throw new RuntimeException("Invalid value separator weights: " + weights);
		}
		
		return maxWeightChar;
	}
	
	public static XCsvDataType fromIdentifier(final String identifier)
	{
		if(identifier == null)
		{
			return null;
		}
		
		return XCsvDataType.valueOf(identifier.toUpperCase());
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final String identifier;
	private final EqConstHashTable<Character, ValueSeparatorWeight> valueSeparatorWeights;
	private final XCsvConfiguration configuration;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	private XCsvDataType(
		final String                                                 identifier           ,
		final EqConstHashTable<Character, XCSV.ValueSeparatorWeight> valueSeparatorWeights
	)
	{
		this.identifier            = identifier           ;
		this.valueSeparatorWeights = valueSeparatorWeights;
		
		final char preferredValueSeparator = determinePreferredValueSeparator(valueSeparatorWeights.values());
		this.configuration = XCsvConfiguration.New(preferredValueSeparator);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	public final String identifier()
	{
		return this.identifier;
	}
	
	public final XCsvConfiguration configuration()
	{
		return this.configuration;
	}
	
	public final EqConstHashTable<Character, ValueSeparatorWeight> valueSeparatorWeights()
	{
		return this.valueSeparatorWeights;
	}
	
	public final boolean isValidValueSeparator(final Character c)
	{
		return this.valueSeparatorWeights.keys().contains(c);
	}
	
	public final boolean isValidValueSeparator(final char c)
	{
		return this.isValidValueSeparator(Character.valueOf(c));
	}
	
	public final XCSV.ValueSeparatorWeight lookupValueSeparator(final Character c)
	{
		return this.valueSeparatorWeights.get(c);
	}
	
	public final XCSV.ValueSeparatorWeight lookupValueSeparator(final char c)
	{
		return this.lookupValueSeparator(Character.valueOf(c));
	}
	
}