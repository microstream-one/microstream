
package one.microstream.bytes;


import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;


/**
 * Size units for byte multiples:
 * <p>
 * Can be used to convert between units as well, see {@link #convert(double, ByteMultiple)}.
 * </p>
 *
 * @see <a href="https://en.wikipedia.org/wiki/Binary_prefix">https://en.wikipedia.org/wiki/Binary_prefix</a>
 * @see <a href="https://physics.nist.gov/cuu/Units/prefixes.html">https://physics.nist.gov/cuu/Units/prefixes.html</a>
 * @see <a href="https://physics.nist.gov/cuu/Units/binary.html">https://physics.nist.gov/cuu/Units/binary.html</a>
 *
 */
public enum ByteMultiple
{
	B  (1   , 1, "b" , "byte", "bytes"                           ),
	KB (1000, 1, "k" , "kb"  , "kilo" , "kilobyte" , "kilobytes" ),
	MB (1000, 2, "m" , "mb"  , "mega" , "megabyte" , "megabytes" ),
	GB (1000, 3, "g" , "gb"  , "giga" , "gigabyte" , "gigabytes" ),
	TB (1000, 4, "t" , "tb"  , "tera" , "terabyte" , "terabytes" ),
	PB (1000, 5, "p" , "pb"  , "peta" , "petabyte" , "petabytes" ),
	EB (1000, 6, "e" , "eb"  , "exa"  , "exabyte"  , "exabytes"  ),
	ZB (1000, 7, "z" , "zb"  , "zetta", "zettabyte", "zettabytes"),
	YB (1000, 8, "y" , "yb"  , "yotta", "yottabyte", "yottabytes"),
	KiB(1024, 1, "ki", "kib" , "kibi" , "kibibyte" , "kibibytes" ),
	MiB(1024, 2, "mi", "mib" , "mebi" , "mebibyte" , "mebibytes" ),
	GiB(1024, 3, "gi", "gib" , "gibi" , "gibibyte" , "gibibytes" ),
	TiB(1024, 4, "ti", "tib" , "tebi" , "tebibyte" , "tebibytes" ),
	PiB(1024, 5, "pi", "pib" , "pebi" , "pebibyte" , "pebibytes" ),
	EiB(1024, 6, "ei", "eib" , "exbi" , "exbibyte" , "exbibytes" ),
	ZiB(1024, 7, "zi", "zib" , "zebi" , "zebibyte" , "zebibytes" ),
	YiB(1024, 8, "yi", "yib" , "yobi" , "yobibyte" , "yobibytes" );
	                                  
	private final static Map<String, ByteMultiple> nameToUnit = new HashMap<>();
	static
	{
		for(final ByteMultiple sizeUnit : values())
		{
			for(final String name : sizeUnit.names)
			{
				nameToUnit.put(name,sizeUnit);
			}
		}
	}
		
	public static ByteMultiple ofName(
		final String name
	)
	{
		return nameToUnit.get(name.toLowerCase());
	}
	
	/**
	 * Fluent API helper for {@link ByteMultiple#convert(double, ByteMultiple)} to get readable code like:
	 * <p>
	 * <code>
	 * convert(1.5, ByteMultiple.MB).to(ByteMultiple.KB);
	 * </code>
	 * </p>
	 */
	@FunctionalInterface
	public static interface Conversion
	{
		public double to(ByteMultiple targetUnit);
	}
	
	/**
	 * Starts a conversion, continue with {@link Conversion#to(ByteMultiple)}, e.g.:
	 * <p>
	 * <code>
	 * convert(1.5, ByteMultiple.MB).to(ByteMultiple.KB);
	 * </code>
	 * </p>
	 * 
	 * @see Conversion
	 */
	public static Conversion convert(
		final double       sourceValue,
		final ByteMultiple sourceUnit
	)
	{
		return targetUnit -> BigDecimal.valueOf(sourceUnit.toBytes(sourceValue)).divide(targetUnit.factor).doubleValue();
	}
	
	
	private final BigDecimal	factor;
	private final String[]		names ;
	
	private ByteMultiple(
		final int       base    ,
		final int       exponent,
		final String... names
	)
	{
		this.factor = BigDecimal.valueOf(base).pow(exponent);
		this.names  = names;
	}
	
	public long toBytes(
		final double value
	)
	{
		return BigDecimal.valueOf(value).multiply(this.factor).longValue();
	}
	
}
