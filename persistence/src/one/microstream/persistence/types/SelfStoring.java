package one.microstream.persistence.types;


/**
 * Type to be implemented if an implementation needs to tailor the storing of its members and references directly.
 * 
 * The naming (missing "Persistence" prefix) is intentional to support convenience on the application code level.
 * 
 * @author TM
 *
 */
public interface SelfStoring
{
	public <S extends Storer> S storeBy(S storer);
}
