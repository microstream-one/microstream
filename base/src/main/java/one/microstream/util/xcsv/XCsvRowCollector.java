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

import one.microstream.collections.types.XGettingList;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.functional._charRangeProcedure;


public interface XCsvRowCollector extends _charRangeProcedure
{
	public void beginTable(
		String                   tableName  ,
		XGettingSequence<String> columnNames,
		XGettingList<String>     columnTypes
	);
	
	@Override
	public void accept(char[] data, int offset, int length);
	
	/**
	 * Calls without collected values (e.g. repeated calls) may not have undesired effects.
	 */
	public void completeRow();

	/**
	 * Calls without collected rows (e.g. repeated calls) may not have undesired effects.
	 */
	public void completeTable();
	
}
