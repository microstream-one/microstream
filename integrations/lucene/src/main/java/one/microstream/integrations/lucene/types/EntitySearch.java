package one.microstream.integrations.lucene.types;

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
