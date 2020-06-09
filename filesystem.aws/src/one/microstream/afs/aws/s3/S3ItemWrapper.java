package one.microstream.afs.aws.s3;

import one.microstream.afs.AItem;

public interface S3ItemWrapper extends AItem
{
	public S3Path path();
}
