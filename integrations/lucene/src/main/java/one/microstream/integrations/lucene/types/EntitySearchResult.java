package one.microstream.integrations.lucene.types;

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
