package one.microstream.integrations.lucene.types;

import static one.microstream.X.notNull;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.QueryBuilder;

import one.microstream.exceptions.IORuntimeException;


public interface EntityIndex<E> extends Closeable
{
	public EntityIndex<E> add(final E entity);
	
	public EntityIndex<E> addAll(final Iterable<? extends E> entities);
	
	public EntityIndex<E> removeBy(final Query... queries);
	
	public EntityIndex<E> removeBy(final Term... terms);
	
	public EntityIndex<E> clear();
	
	public QueryBuilder createQueryBuilder();
	
	public EntitySearchResult<E> search(final EntitySearch search);
		
	public int size();
	
	
	
	public static <E> EntityIndex<E> New(
		final EntityMapper<E> mapper   ,
		final Directory       directory
	)
	{
		return new EntityIndex.Default<>(
			notNull(mapper   ),
			notNull(directory)
		);
	}
	
	
	
	public static class Default<E> implements EntityIndex<E>
	{
		private final EntityMapper<E> mapper   ;
		private final Directory       directory;
		private IndexWriter           writer   ;
		private DirectoryReader       reader   ;
		private IndexSearcher         searcher ;
		
		Default(
			final EntityMapper<E> mapper   ,
			final Directory       directory
		)
		{
			super();
			this.mapper    = mapper   ;
			this.directory = directory;
		}

		private void lazyInit()
		{
			try
			{
				if(this.writer == null)
				{
					this.writer = new IndexWriter(
						this.directory,
						new IndexWriterConfig(new StandardAnalyzer())
					);
					this.searcher = new IndexSearcher(
						this.reader = DirectoryReader.open(this.writer)
					);
				}
				else
				{
					final DirectoryReader newReader = DirectoryReader.openIfChanged(this.reader);
					if(newReader != null && newReader != this.reader)
					{
						this.reader.close();
						this.reader   = newReader;
						this.searcher = new IndexSearcher(this.reader);
					}
				}
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}

		@Override
		public synchronized EntityIndex<E> add(final E entity)
		{
			this.lazyInit();

			try
			{
				this.writer.addDocument(
					this.mapper.toDocument(entity)
				);
				this.writer.flush();
				this.writer.commit();
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
			
			return this;
		}

		@Override
		public synchronized EntityIndex<E> addAll(final Iterable<? extends E> entities)
		{
			this.lazyInit();

			try
			{
				final List<Document> documents = StreamSupport.stream(entities.spliterator(), false)
					.map(this.mapper::toDocument)
					.collect(Collectors.toList())
				;
				this.writer.addDocuments(documents);
				this.writer.flush();
				this.writer.commit();
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
			
			return this;
		}

		@Override
		public synchronized EntityIndex<E> removeBy(final Query... queries)
		{
			this.lazyInit();

			try
			{
				this.writer.deleteDocuments(queries);
				this.writer.flush();
				this.writer.commit();
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
			
			return this;
		}

		@Override
		public synchronized EntityIndex<E> removeBy(final Term... terms)
		{
			this.lazyInit();

			try
			{
				this.writer.deleteDocuments(terms);
				this.writer.flush();
				this.writer.commit();
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
			
			return this;
		}

		@Override
		public synchronized EntityIndex<E> clear()
		{
			this.lazyInit();

			try
			{
				this.writer.deleteAll();
				this.writer.flush();
				this.writer.commit();
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
			
			return this;
		}
		
		@Override
		public synchronized QueryBuilder createQueryBuilder()
		{
			this.lazyInit();

			return new QueryBuilder(
				this.writer.getAnalyzer()
			);
		}
		
		@Override
		public EntitySearchResult<E> search(final EntitySearch search)
		{
			return this.search(search, null);
		}
		
		synchronized EntitySearchResult<E> search(final EntitySearch search, final ScoreDoc after)
		{
			this.lazyInit();

			try
			{
				final Sort    sort    = search.sort();
				final TopDocs topDocs = sort != null
					? this.searcher.searchAfter(after, search.query(), search.maxResults(), sort, search.doTopScores())
					: this.searcher.searchAfter(after, search.query(), search.maxResults())
				;
				final List<E>  result       = new ArrayList<>(topDocs.scoreDocs.length);
				      ScoreDoc lastScoreDoc = null;
				for(final ScoreDoc scoreDoc : topDocs.scoreDocs)
				{
					final Document document = this.searcher.doc(scoreDoc.doc);
					final E        entity   = this.mapper.toEntity(document);
					if(entity != null)
					{
						result.add(entity);
						lastScoreDoc = scoreDoc;
					}
				}
				return new EntitySearchResult.Default<>(
					this,
					search,
					Collections.unmodifiableList(result),
					lastScoreDoc
				);
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}
		
		@Override
		public synchronized int size()
		{
			this.lazyInit();

			return this.searcher.getIndexReader().numDocs();
		}

		@Override
		public synchronized void close() throws IOException
		{
			if(this.writer != null)
			{
				this.writer.close();
				this.reader.close();
				this.directory.close();

				this.writer    = null;
				this.reader    = null;
				this.searcher  = null;
			}
		}
		
	}
	
}
