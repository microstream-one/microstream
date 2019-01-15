package net.jadoth.persistence.binary.types;

public final class BuildItem2 extends Binary2
{

	@Override
	public final Chunk[] channelChunks()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public final void storeEntityHeader(
		final long entityContentLength,
		final long entityTypeId       ,
		final long entityObjectId
	)
	{
		throw new UnsupportedOperationException();
	}
	
}
