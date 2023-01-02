package one.microstream.collections;

/*-
 * #%L
 * microstream-base
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

import static one.microstream.chars.VarString.New;

import java.util.function.Consumer;

import one.microstream.chars.VarString;
import one.microstream.collections.types.XMap;
import one.microstream.typing.KeyValue;

/**
 * Configurable {@link KeyValue} to-String concatenator.
 *
 * @param <K> the key type
 * @param <V> the value type
 *
 */
public class MapEntriesConcatenator<K, V>
{
	// (04.07.2011 TM)TODO: configurable assembler procedures for key and value

	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	// note: fields are not private to avoid accessor generation for AIC
	String mapStartSymbol = "{";
	String mapEndSymbol = "}";

	String preKeySymbol = "";
	String mappingSymbol = "=";
	String postValueSymbol = ", ";



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	public String getMapStartSymbol()
	{
		return this.mapStartSymbol;
	}

	public String getMapEndSymbol()
	{
		return this.mapEndSymbol;
	}

	public String getPreKeySymbol()
	{
		return this.preKeySymbol;
	}

	public String getMappingSymbol()
	{
		return this.mappingSymbol;
	}

	public String getPostValueSymbol()
	{
		return this.postValueSymbol;
	}



	///////////////////////////////////////////////////////////////////////////
	// setters //
	////////////

	public MapEntriesConcatenator<K, V> setMapStartSymbol(final String mapStartSymbol)
	{
		this.mapStartSymbol = mapStartSymbol;
		return this;
	}

	public MapEntriesConcatenator<K, V> setMapEndSymbol(final String mapEndSymbol)
	{
		this.mapEndSymbol = mapEndSymbol;
		return this;
	}

	public MapEntriesConcatenator<K, V> setPreKeySymbol(final String preKeySymbol)
	{
		this.preKeySymbol = preKeySymbol;
		return this;
	}

	public MapEntriesConcatenator<K, V> setMappingSymbol(final String mappingSymbol)
	{
		this.mappingSymbol = mappingSymbol;
		return this;
	}

	public MapEntriesConcatenator<K, V> setPostValueSymbol(final String postValueSymbol)
	{
		this.postValueSymbol = postValueSymbol;
		return this;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	/**
	 * Convenience method that combines all setters in one call.
	 *
	 * @param mapStartSymbol map start symbol
	 * @param mapEndSymbol map end symbol
	 * @param preKeySymbol pre key symbol
	 * @param mappingSymbol mapping symbol
	 * @param postValueSymbol post value symbol
	 * @return this instance.
	 */
	public MapEntriesConcatenator<K, V> configure(
		final String mapStartSymbol,
		final String mapEndSymbol,
		final String preKeySymbol,
		final String mappingSymbol,
		final String postValueSymbol
	)
	{
		this.mapStartSymbol = mapStartSymbol;
		this.mapEndSymbol = mapEndSymbol;
		this.preKeySymbol = preKeySymbol;
		this.mappingSymbol = mappingSymbol;
		this.postValueSymbol = postValueSymbol;
		return this;
	}

	public MapEntriesConcatenator<K, V> configureEntrySymbols(
		final String preKeySymbol,
		final String mappingSymbol,
		final String postValueSymbol
	)
	{
		this.preKeySymbol = preKeySymbol;
		this.mappingSymbol = mappingSymbol;
		this.postValueSymbol = postValueSymbol;
		return this;
	}

	public MapEntriesConcatenator<K, V> configureMapSymbols(
		final String mapStartSymbol,
		final String mapEndSymbol
	)
	{
		this.mapStartSymbol = mapStartSymbol;
		this.mapEndSymbol = mapEndSymbol;
		return this;
	}


	public String assemble(final XMap<? extends K, ? extends V> map)
	{
		return this.appendTo(New(), map).toString();
	}

	public VarString appendTo(final VarString vc, final XMap<? extends K, ? extends V> map)
	{
		vc.add(this.mapStartSymbol);
		map.iterate(new Consumer<KeyValue<? extends K, ? extends V>>()
		{
			@Override
			public void accept(final KeyValue<? extends K, ? extends V> e)
			{
				vc
				.add(MapEntriesConcatenator.this.preKeySymbol)
				.add(e.key())
				.add(MapEntriesConcatenator.this.mappingSymbol)
				.add(e.value())
				.add(MapEntriesConcatenator.this.postValueSymbol)
				;
			}
		});
		return vc.add(this.mapEndSymbol);
	}

}
