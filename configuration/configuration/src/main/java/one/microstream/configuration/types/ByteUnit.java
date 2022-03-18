
package one.microstream.configuration.types;

/*-
 * #%L
 * microstream-configuration
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */


import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;


/**
 * Size units for byte multiples:
 * <p>
 * Can be used to convert between units as well, see {@link #convert(double, ByteUnit)}.
 * </p>
 *
 * @see <a href="https://en.wikipedia.org/wiki/Binary_prefix">https://en.wikipedia.org/wiki/Binary_prefix</a>
 * @see <a href="https://physics.nist.gov/cuu/Units/prefixes.html">https://physics.nist.gov/cuu/Units/prefixes.html</a>
 * @see <a href="https://physics.nist.gov/cuu/Units/binary.html">https://physics.nist.gov/cuu/Units/binary.html</a>
 *
 */
public enum ByteUnit
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
	                                  
	private final static Map<String, ByteUnit> nameToUnit = new HashMap<>();
	static
	{
		for(final ByteUnit sizeUnit : values())
		{
			for(final String name : sizeUnit.names)
			{
				nameToUnit.put(name,sizeUnit);
			}
		}
	}
		
	public static ByteUnit ofName(
		final String name
	)
	{
		return nameToUnit.get(name.toLowerCase());
	}
	
	/**
	 * Fluent API helper for {@link ByteUnit#convert(double, ByteUnit)} to get readable code like:
	 * <p>
	 * <code>
	 * convert(1.5, ByteMultiple.MB).to(ByteMultiple.KB);
	 * </code>
	 * </p>
	 */
	@FunctionalInterface
	public static interface Conversion
	{
		public double to(ByteUnit targetUnit);
	}
	
	/**
	 * Starts a conversion, continue with {@link Conversion#to(ByteUnit)}, e.g.:
	 * <p>
	 * <code>
	 * convert(1.5, ByteMultiple.MB).to(ByteMultiple.KB);
	 * </code>
	 * </p>
	 * 
	 * @param sourceValue the source value to convert
	 * @param sourceUnit the source unit to convert
	 * @return the conversion for method chaining
	 * 
	 * @see Conversion
	 */
	public static Conversion convert(
		final double   sourceValue,
		final ByteUnit sourceUnit
	)
	{
		return targetUnit -> BigDecimal.valueOf(sourceUnit.toBytes(sourceValue)).divide(targetUnit.factor).doubleValue();
	}
	
	
	private final BigDecimal	factor;
	private final String[]		names ;
	
	private ByteUnit(
		final int       base    ,
		final int       exponent,
		final String... names
	)
	{
		this.factor = BigDecimal.valueOf(base).pow(exponent);
		this.names  = names;
	}
	
	/**
	 * Returns the number of bytes, which this unit multiplied by <code>value</code> yields.
	 * 
	 * @param value the value to convert
	 * @return the value converted to bytes
	 */
	public long toBytes(
		final double value
	)
	{
		return BigDecimal.valueOf(value).multiply(this.factor).longValue();
	}
	
}
