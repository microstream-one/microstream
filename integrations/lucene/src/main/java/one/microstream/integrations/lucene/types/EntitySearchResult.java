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

import java.util.List;

import org.apache.lucene.search.ScoreDoc;

public interface EntitySearchResult<E>
{
	public List<E> entities();
	
	public EntitySearchResult<E> next();
	
	
	public static class Default<E> implements EntitySearchResult<E>
	{
		private final EntityIndex.Default<E> index       ;
		private final EntitySearch           search      ;
		private final List<E>                entities    ;
		private final ScoreDoc               lastScoreDoc;
		
		Default(
			final EntityIndex.Default<E> index       ,
			final EntitySearch           search      ,
			final List<E>                entities    ,
			final ScoreDoc               lastScoreDoc
		)
		{
			super();
			this.index        = index;
			this.search       = search;
			this.entities     = entities;
			this.lastScoreDoc = lastScoreDoc;
		}
		
		@Override
		public List<E> entities()
		{
			return this.entities;
		}
		
		@Override
		public EntitySearchResult<E> next()
		{
			return this.lastScoreDoc != null
				? this.index.search(this.search, this.lastScoreDoc)
				: null
			;
		}
		
	}
	
	
}
