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

import java.time.Instant;


/**
 * @author FlorianHabermann
 *
 */
public class Turnover
{
	private final double  amount;
	private final Instant timestamp;
	
	public Turnover(final double amount, final Instant timestamp)
	{
		super();
		this.amount    = amount;
		this.timestamp = timestamp;
	}
	
	public double getAmount()
	{
		return this.amount;
	}

	public Instant getTimestamp()
	{
		return this.timestamp;
	}
}
