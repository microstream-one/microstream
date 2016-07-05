package net.jadoth.persistence.test;

final class TestOffsets
{
    final char value[];
    final int offset;
    final int count;
    int hash;


	public TestOffsets()
	{
		super();
		this.value = null;
		this.offset = 0;
		this.count = 0;
	}

	@Override
	public int hashCode()
	{
		return this.hash = System.identityHashCode(this);
	}


}