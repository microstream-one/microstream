
package one.microstream.examples.cdi.javase;

/*-
 * #%L
 * microstream-examples-cdi-javase
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

import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Testing the Cache integration.
 */
public class App1
{
	public static void main(final String[] args)
	{
		try(SeContainer container = SeContainerInitializer.newInstance().initialize())
		{
			final String      sebastian = "Sebastian";
			final String      otavio    = "Otavio";
			final NameCounter counter   = container.select(NameCounter.class).get();
			
			System.out.println("The name counter values: " + counter.getNames());
			final ThreadLocalRandom random           = ThreadLocalRandom.current();
			final int               sebastianCounter = random.nextInt(1, 10);
			final int               otavioCounter    = random.nextInt(1, 10);
			System.out.printf(
				"The counters: Sebastian %d and Otavio %d %n",
				sebastianCounter,
				otavioCounter);

			for (int index = 0; index < sebastianCounter; index++)
			{
				counter.count(sebastian);
			}
			for (int index = 0; index < otavioCounter; index++)
			{
				counter.count(otavio);
			}
			
			System.out.println("The name counter values: " + counter.getNames());
		}
	}
}
