package one.microstream.integrations.lucene.types;

/*-
 * #%L
 * microstream-integrations-lucene
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

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;
import static one.microstream.math.XMath.positive;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;

public interface EntitySearch
{
	public Query query();
	
	public int maxResults();
	
	public Sort sort();
	
	public boolean doTopScores();
	
	
	public static EntitySearch New(
		final Query   query      ,
		final int     maxResults
	)
	{
		return New(query, maxResults, null, false);
	}
	
	
	public static EntitySearch New(
		final Query   query      ,
		final int     maxResults ,
		final Sort    sort       ,
		final boolean doTopScores
	)
	{
		return new EntitySearch.Default(
			notNull (query      ),
			positive(maxResults ),
			mayNull (sort       ),
			         doTopScores
		);
	}
	
	
	public static class Default implements EntitySearch
	{
		private final Query   query      ;
		private final int     maxResults ;
		private final Sort    sort       ;
		private final boolean doTopScores;
		
		Default(
			final Query   query      ,
			final int     maxResults ,
			final Sort    sort       ,
			final boolean doTopScores
		)
		{
			super();
			this.query       = query      ;
			this.maxResults  = maxResults ;
			this.sort        = sort       ;
			this.doTopScores = doTopScores;
		}

		@Override
		public Query query()
		{
			return this.query;
		}

		@Override
		public int maxResults()
		{
			return this.maxResults;
		}

		@Override
		public Sort sort()
		{
			return this.sort;
		}

		@Override
		public boolean doTopScores()
		{
			return this.doTopScores;
		}
		
	}
	
}
