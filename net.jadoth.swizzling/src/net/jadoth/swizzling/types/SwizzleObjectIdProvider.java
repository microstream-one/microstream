package net.jadoth.swizzling.types;


public interface SwizzleObjectIdProvider extends SwizzleObjectIdLookup
{
	public long provideNextObjectId();

	public SwizzleObjectIdProvider initializeObjectId();

	public SwizzleObjectIdProvider updateCurrentObjectId(long currentObjectId);

}
