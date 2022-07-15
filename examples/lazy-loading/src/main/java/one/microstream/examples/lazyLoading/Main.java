
package one.microstream.examples.lazyLoading;

/*-
 * #%L
 * microstream-examples-lazy-loading
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
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

import java.nio.file.Paths;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.Year;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import one.microstream.storage.embedded.types.EmbeddedStorage;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;


/**
 * Lazy loading example. Lazy part is in {@link BusinessYear}.
 *
 *
 */
public class Main
{
	public static void main(final String[] args)
	{
		final EmbeddedStorageManager storageManager = EmbeddedStorage.start(Paths.get("data"));
		if(storageManager.root() == null)
		{
			storageManager.setRoot(createSampleData());
			storageManager.storeRoot();
		}
		
		calcRevenue(storageManager);
		
		System.out.println();
		System.out.println("No going full in memory!");

		calcRevenue(storageManager);
		
		storageManager.shutdown();
	}
	
	private static void calcRevenue(final EmbeddedStorageManager storageManager)
	{
		final MyBusinessApp        root           = (MyBusinessApp)storageManager.root();

		final int                  startYear      = Year.now().getValue() - 3;
		final long                 now            = System.currentTimeMillis();

		final Map<Integer, Double> yearToRevenue  = root.getBusinessYears().entrySet().stream()
			.filter(e -> e.getKey() >= startYear)
			.collect(Collectors.toMap(
				e -> e.getKey(),
				e -> e.getValue().turnovers()
					.mapToDouble(Turnover::getAmount)
					.sum()));

		final double               overallRevenue = yearToRevenue.values().stream()
			.mapToDouble(Double::doubleValue)
			.sum();

		final NumberFormat         numberFormat   = NumberFormat.getCurrencyInstance();

		System.out.println();

		System.out.println(
			"Overall Revenue since " + startYear + ": " +
				numberFormat.format(overallRevenue));

		yearToRevenue.entrySet().stream()
			.sorted((e1, e2) -> Integer.compare(e1.getKey(), e2.getKey()))
			.forEach(e -> System.out.println(
				"Revenue in " +
					e.getKey() +
					": " +
					numberFormat.format(e.getValue())));

		final long recordCount = root.getBusinessYears().entrySet().stream()
			.filter(e -> e.getKey() >= startYear)
			.mapToLong(e -> 2 + 2 * e.getValue().turnovers().count())
			.sum();

		System.out.println(
			"Took " + (System.currentTimeMillis() - now) +
				" ms for " + NumberFormat.getIntegerInstance().format(recordCount) + " records");
	}
	
	private static MyBusinessApp createSampleData()
	{
		System.out.println("Creating sample data...");
		
		final MyBusinessApp app    = new MyBusinessApp();
		
		final Random        random = new Random();
		
		for(int year = 2000, thisYear = Year.now().getValue(); year <= thisYear; year++)
		{
			final BusinessYear businessYear = new BusinessYear();
			for(int i = 0, c = (int)(random.nextDouble() * 100000); i < c; i++)
			{
				businessYear.addTurnover(new Turnover(random.nextDouble() * 100_000, Instant.now()));
			}
			app.getBusinessYears().put(year, businessYear);
		}
		
		return app;
	}
}
