
package one.microstream.storage.restclient.exceptions;

public class StorageViewExceptionMissingTypeDescription extends StorageViewException
{
	private final long missingTypeId;
	
	public StorageViewExceptionMissingTypeDescription(
		final long missingTypeId
	)
	{
		super();
		
		this.missingTypeId = missingTypeId;
	}
	
	public long missingTypeId()
	{
		return this.missingTypeId;
	}
	
	@Override
	public String assembleDetailString()
	{
		return "Missing type description, typeId=" + this.missingTypeId;
	}
	
}
