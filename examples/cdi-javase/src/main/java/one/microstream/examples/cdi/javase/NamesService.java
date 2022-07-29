
package one.microstream.examples.cdi.javase;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Set;


@ApplicationScoped
public class NamesService
{
	@Inject
	private Names names;

	public void add(final String name)
	{
		this.names.add(name);
	}
	
	public Set<String> getNames()
	{
		return this.names.get();
	}
	
}
