package one.microstream.integrations.lucene.types;

import one.microstream.integrations.lucene.annotations.IndexId;
import one.microstream.integrations.lucene.annotations.IndexProperty;

public class TestEntity
{
	private final String id;
	private final String name;
	private final String lorem;
	private final double number;
		
	public TestEntity(final String id, final String name, final String lorem, final double number)
	{
		super();
		this.id     = id;
		this.name   = name;
		this.lorem  = lorem;
		this.number = number;
	}

	@IndexId
	public String getId()
	{
		return this.id;
	}
	
	@IndexProperty
	public String getName()
	{
		return this.name;
	}

	@IndexProperty
	public String getLorem()
	{
		return this.lorem;
	}

	@IndexProperty
	public double getNumber()
	{
		return this.number;
	}
	
	@Override
	public String toString()
	{
		return this.name;
	}
	
}
