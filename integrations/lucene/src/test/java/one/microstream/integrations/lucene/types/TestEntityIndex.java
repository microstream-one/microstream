package one.microstream.integrations.lucene.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import com.github.javafaker.Faker;

@TestInstance(PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
public class TestEntityIndex
{
	private final static int    ENTITY_COUNT = 10000;
	private final static String QUERY_NAME   = "name";
	private final static String QUERY_TEXT   = "dr.";
	
	Map<String, TestEntity> entities;
	EntityIndex<TestEntity> index;
	
	@BeforeAll
	void init()
	{
		this.entities = this.createEntities();
		
		final Function<IndexableField, TestEntity> entityResolver =
			field -> this.entities.get(field.stringValue())
		;
		final EntityMapper<TestEntity> entityMapper = EntityMapper.AnnotationBased(
			TestEntity.class,
			entityResolver
		);
		this.index = EntityIndex.New(
			entityMapper,
			new ByteBuffersDirectory()
		);
		
		this.index.addAll(this.entities.values());
		
//		this.entities.values().forEach(System.out::println);
//		System.out.println("============================================");
	}
	
	@Test
	@Order(1)
	void testIndexSize()
	{
		assertEquals(ENTITY_COUNT, this.index.size());
	}
	
	@Test
	@Order(2)
	void testSearch()
	{
		final int luceneHits = this.searchLucene();
		
		final long javaHits = this.entities.values().stream()
			.filter(e -> e.getName().toLowerCase().contains(QUERY_TEXT))
			.count()
		;
		
		assertEquals(javaHits, luceneHits);
	}
	
	@Test
	@Order(3)
	void testRemove()
	{
		this.index.removeBy(this.createLuceneQuery());
		
		final int luceneHits = this.searchLucene();
		assertEquals(0, luceneHits);
	}
	
	@AfterAll
	void cleanup()
	{
		this.index.close();
	}
	

	private int searchLucene()
	{
		final EntitySearch search = EntitySearch.New(
			this.createLuceneQuery(),
			Integer.MAX_VALUE
		);
		final EntitySearchResult<TestEntity> result = this.index.search(search);
		final int luceneHits = result.fetchAll().size();
		return luceneHits;
	}

	private Query createLuceneQuery()
	{
		return this.index.createQueryBuilder().createPhraseQuery(QUERY_NAME, QUERY_TEXT);
	}
		
	private Map<String, TestEntity> createEntities()
	{
		final Map<String, TestEntity> entities = new HashMap<>();
		
		final Faker faker = new Faker();
		for(int i = 0; i < ENTITY_COUNT; i++)
		{
			final String id = faker.idNumber().valid();
			final String name = faker.name().fullName();
			final String lorem = faker.lorem().sentence();
			final double number = faker.random().nextDouble();
			entities.put(id, new TestEntity(id, name, lorem, number));
		}
		
		return entities;
	}
	
}
