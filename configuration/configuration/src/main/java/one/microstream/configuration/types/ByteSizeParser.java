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

import static one.microstream.X.notNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for {@link ByteSize}s out of textual representation.
 * 
 */
@FunctionalInterface
public interface ByteSizeParser
{
	/**
	 * Tries to parse a {@link ByteSize} out of <code>text</code>.
	 * It usually consists of an amount and an unit, e.g. <code>"1MB"</code> or <code>"1.2 GB"</code>.
	 * 
	 * @param text the textual input
	 * @return the parsed {@link ByteSize}
	 * @throws IllegalArgumentException if the text couldn't be parsed to a {@link ByteSize}
	 * @see ByteSize
	 * @see ByteUnit
	 */
	public ByteSize parse(String text);
	
	
	/**
	 * Preudo-constructor method to create a new {@link ByteSizeParser}.
	 * @return a new {@link ByteSizeParser}
	 */
	public static ByteSizeParser New()
	{
		return new ByteSizeParser.Default(ByteUnit.B);
	}
		
	/**
	 * Preudo-constructor method to create a new {@link ByteSizeParser}.
	 * 
	 * @param defaultUnit the default unit which is used if no unit is given in the input
	 * @return a new {@link ByteSizeParser}
	 */
	public static ByteSizeParser New(
		final ByteUnit defaultUnit
	)
	{
		return new ByteSizeParser.Default(
			notNull(defaultUnit)
		);
	}
	
	
	public static class Default implements ByteSizeParser
	{
		private final Pattern pattern = Pattern.compile(
			"(?<amount>[0-9]*\\.?[0-9]*([eE][-+]?[0-9]+)?)(?:\\s*)(?<unit>[a-z]+)",
			Pattern.CASE_INSENSITIVE
		);
		
		private final ByteUnit defaultUnit;
		
		Default(
			final ByteUnit defaultUnit
		)
		{
			super();
			this.defaultUnit = defaultUnit;
		}
		
		@Override
		public ByteSize parse(
			final String text
		)
		{
			final Matcher matcher = this.pattern.matcher(text);
			if(matcher.find())
			{
				return this.parseWithUnit(
					matcher.group("amount"),
					matcher.group("unit")
				);
			}

			try
			{
				return ByteSize.New(
					Double.parseDouble(text),
					this.defaultUnit
				);
			}
			catch(final NumberFormatException nfe)
			{
				throw new IllegalArgumentException(
					"Invalid byte size: " + text,
					nfe
				);
			}
		}

		private ByteSize parseWithUnit(
			final String amountText,
			final String unitText
		)
		{
			final double amount;
			try
			{
				amount = Double.parseDouble(amountText);
			}
			catch(final NumberFormatException nfe)
			{
				throw new IllegalArgumentException(
					"Invalid byte size: " + amountText + unitText,
					nfe
				);
			}

			final ByteUnit byteMultiple = ByteUnit.ofName(unitText);
			if(byteMultiple == null)
			{
				throw new IllegalArgumentException(
					"Invalid byte size: " + amountText + unitText +
					", unknown unit: " + unitText
				);
			}

			return ByteSize.New(amount, byteMultiple);
		}
		
	}
	
}
