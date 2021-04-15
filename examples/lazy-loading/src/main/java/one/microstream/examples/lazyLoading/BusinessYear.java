
package one.microstream.examples.lazyLoading;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import one.microstream.reference.Lazy;


public class BusinessYear
{
	private Lazy<List<Turnover>> turnovers;

	public BusinessYear()
	{
		super();
	}

	private List<Turnover> getTurnovers()
	{
		return Lazy.get(this.turnovers);
	}

	public void addTurnover(final Turnover turnover)
	{
		List<Turnover> turnovers = this.getTurnovers();
		if(turnovers == null)
		{
			this.turnovers = Lazy.Reference(turnovers = new ArrayList<>());
		}
		turnovers.add(turnover);
	}

	public Stream<Turnover> turnovers()
	{
		final List<Turnover> turnovers = this.getTurnovers();
		return turnovers != null ? turnovers.stream() : Stream.empty();
	}
}
