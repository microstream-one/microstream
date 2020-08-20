package one.microstream.storage.types;

public interface DatabasePart
{
	/**
	 * Returns the identifying name of the {@link Database} this part belongs to.
	 * 
	 * @return the identifying name of the {@link Database}.
	 */
	public String databaseName();
}
