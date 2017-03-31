package net.jadoth.test.corp.logic;

import net.jadoth.Jadoth;
import net.jadoth.collections.X;
import net.jadoth.collections.types.XList;
import net.jadoth.functional.JadothAggregates;
import net.jadoth.functional.JadothPredicates;
import net.jadoth.test.corp.model.ClientCorporation;
import net.jadoth.test.corp.model.Person;
import net.jadoth.traversal.ObjectGraphTraverser;
import net.jadoth.traversal.OpenAdressingMiniSet;

public class MainTestExampleGraphTraversal
{
	public static void main(final String[] args) throws Throwable
	{
		// example graph of n entities (~ 3*n instances including value types)
		final ClientCorporation corp = Test.generateModelData(5_000);



		// 1.) print the system string of every instance in the graph
		ObjectGraphTraverser.traverseGraph(corp, e -> System.out.println(Jadoth.systemString(e)));

		// 2.) print all strings contained in the graph (~= graph querying)
		ObjectGraphTraverser.traverseGraph(corp, String.class::isInstance, System.out::println);

		// 3.) print the system string of every instance in the graph except certain types
		ObjectGraphTraverser.traverseGraph(corp,
			JadothPredicates.notIsInstanceOf(String.class, Number.class, Iterable.class),
			e -> System.out.println(Jadoth.systemString(e))
		);

		// 4.) collect all instances contained in the graph (once)
		final XList<Object> allInstances = ObjectGraphTraverser.traverseGraph(corp, X.List());
		System.out.println(allInstances.size()+" instances");

		// 5.) select entities by type and a custom condition
		final XList<Person> persons = ObjectGraphTraverser.selectFromGraph(
			corp,
			Person.class,
			p -> p.lastname().length() < 10
		);
		System.out.println(persons.size()+" matching persons.");



		// also maybe conceivable, similar to CQL. Or maybe this can replace/extend CQL.
//		final XList<Person> persons = ObjectGraphTraverser
//			.select(Person.class)
//			.from(corp)
//			.where(p -> p.lastname().length() < 10)
//			.into(X.List())
//		);

		// TODO: (WIP) consolidate redundant value type instances in a persistent graph
//		ObjectGraphTraverser.traverseGraph(corp, ValueConsolidator.New(), STORAGE.createStorer()).commit();

//		roughly 20-200ns per instance, depending on entity types and graph size
//		testPerformance(5_000);
	}

	static Object[] generateStringArray(final int amount)
	{
		final Object[] array = new Object[amount>>1];

		for(int i = 0; i < array.length; i++)
		{
			array[i] = Integer.toString(i);
		}

		return array;
	}

	static Object[] generateSimpleModel(final int amount)
	{
		int amountRest = amount - 111;

		final int leafLength = amount / 100 - 1;

		final Object[][][] root = new Object[10][][];

		graphGeneration:
		for(int i1 = 0; i1 < root.length; i1++)
		{
			final Object[][] d2 = new Object[10][];
			root[i1] = d2;
			for(int i2 = 0; i2 < d2.length; i2++)
			{
				final Object[] d3 = new Object[leafLength];
				d2[i2] = d3;
				for(int i3 = 0; i3 < d3.length; i3++)
				{
					if(--amountRest >= 0)
					{
						d3[i3] = new Integer(i3);
					}
					else
					{
						break graphGeneration;
					}
				}
			}
		}

		return root;
	}

	static void testPerformance(final int amount)
	{
		final Object root = Test.generateModelData(amount);
//		final Object root = generateSimpleModel(amount);
//		final Object root = generateStringArray(amount);


		// always at least 2 warmup runs
		final int warmupCount  = Math.max(10_000_000 / amount, 2);
		final int measureCount = 10;
		final int runCount     = warmupCount + measureCount;

		System.out.println(runCount + " runs: " + warmupCount + " warm-up, " + measureCount + " measuring.");

		long lastTime   = 0;
		long lastCount  = 0;
		long lastXTime  = 0;
		long lastXCount = 0;
		for(int i = runCount; i --> 0;)
		{
//			final HashEnum<Object> alreadyHandled = HashEnum.NewCustom(amount*4);
			final OpenAdressingMiniSet<Object> alreadyHandled = OpenAdressingMiniSet.New(amount*4);

			final ObjectGraphTraverser traverser = ObjectGraphTraverser.Factory()
				.setAlreadyHandledProvider(skipped -> alreadyHandled)
				.buildObjectGraphTraverser()
			;
			final long tStart = System.nanoTime();
			lastCount = traverser.traverse(root, JadothAggregates.count()).yield();
			final long tStop = System.nanoTime();
			lastTime = tStop - tStart;
			System.out.println(new java.text.DecimalFormat("00,000,000,000").format(lastTime)+" for "+lastCount+" instances. "+lastTime / lastCount + " ns per instance.");

			if(i < measureCount)
			{
				lastXTime += lastTime;
				lastXCount += lastCount;
			}

			System.gc();
		}

		System.out.println(
			lastXTime / lastXCount + " nanoseconds per instance on average over the last " + measureCount
			+ " after " + warmupCount + " warmup runs."
		);

	}

	static void customObjectGraphTraverser(final Object root)
	{
		final ObjectGraphTraverser traverser = ObjectGraphTraverser.Factory()
			.setHandlingLogic                 (null      ) // callback, der auf eine Instanz angewendet werden soll. Z.B. System.out::println oder was auch immer
			.setHandlingLogicProvider         (null      ) // Indirektion anstatt dem oben, um je Typ eine andere Logik auszuw�hlen oder state zu resetten oder so.

			.leafTypes                        (null, null) // "Leaves" werden zwar von der Logik gehandelt, aber ihre Referenzen werden nicht mehr durchlaufen.
			.nodeTypes                   (null, null) // Umgekehrter Fall: Zwar Referenzen weiterverfolgen, aber Instanzen an sich nicht handeln (reiner "Node")
			.excludeTypes                     (null, null) // Kombination aus beidem: weder handeln noch Referenzen verfolgen, d.h. effektiv ausschlie�en

			.setTraversalHandlerProvider      (null)       // F�r custom Logik zum Rerenzen traversieren, �hnlich wie f�r Handling-Logik.

			.setTraversableFieldSelector      (null)       // Zu traversierende Referenzen einfach nach Feld ausw�hlen

			.skipAll                          (null, null) // Konkrete Instanzen ignorieren

			.buildObjectGraphTraverser()
		;

		traverser.traverse(root);
	}

}
