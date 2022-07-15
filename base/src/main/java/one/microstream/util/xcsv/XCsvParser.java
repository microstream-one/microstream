package one.microstream.util.xcsv;

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


public interface XCsvParser<I>
{
	public XCsvConfiguration parseCsvData(
		XCsvDataType                   dataType              ,
		XCsvConfiguration              config                ,
		I                              input                 ,
		XCsvSegmentsParser.Provider<I> segmentsParserProvider,
		XCsvRowCollector               rowAggregator
	);
	
	public default XCsvConfiguration parseCsvData(
		final XCsvConfiguration              config                ,
		final I                              input                 ,
		final XCsvSegmentsParser.Provider<I> segmentsParserProvider,
		final XCsvRowCollector               rowAggregator
	)
	{
		return this.parseCsvData(null, config, input, segmentsParserProvider, rowAggregator);
	}
	
	public default XCsvConfiguration parseCsvData(
		final XCsvConfiguration config       ,
		final I                 input        ,
		final XCsvRowCollector  rowAggregator
	)
	{
		return this.parseCsvData(null, config, input, null, rowAggregator);
	}

	public default XCsvConfiguration parseCsvData(
		final XCsvDataType      dataType     ,
		final XCsvConfiguration config       ,
		final I                 input        ,
		final XCsvRowCollector  rowAggregator
	)
	{
		return this.parseCsvData(dataType, config, input, null, rowAggregator);
	}


	public interface Creator<D>
	{
		public XCsvParser<D> createCsvStringParser();
	}

	public interface Provider<D>
	{
		public XCsvParser<D> provideCsvStringParser();

		public void disposeCsvStringParser(XCsvParser<D> parser);
	}

}
