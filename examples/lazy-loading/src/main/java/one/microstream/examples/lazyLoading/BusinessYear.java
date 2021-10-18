
package one.microstream.examples.lazyLoading;

/*-
 * #%L
 * microstream-examples-lazy-loading
 * %%
 * Copyright (C) 2019 - 2021 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

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
