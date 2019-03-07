package one.microstream.persistence.test;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import one.microstream.X;
import one.microstream.chars.VarString;
import one.microstream.collections.EqConstHashTable;
import one.microstream.collections.EqHashTable;
import one.microstream.collections.HashEnum;
import one.microstream.collections.HashTable;
import one.microstream.collections.types.XIterable;
import one.microstream.collections.types.XTable;
import one.microstream.functional.Aggregator;

public class MainTestGraphDepthQuerying
{
	///////////////////////////////////////////////////////////////////////////
	// custom application part //
	////////////////////////////

	static final EqConstHashTable<String, Person> PERSONS = initializePersons();

	public static void main(final String[] args)
	{
		final Person root = PERSONS.get("p00");
		final XTable<Person, Integer> range2NonFriends = queryDeep(
			X.Reference(root)                          , // single root in this example
			2                                          , // graph recursion depth
			p -> p.friends                             , // node references iteration resolver
			p -> p != root && !p.friends.contains(root), // target predicate (excluding root is example-specific, not API-specific!)
			(p, c) -> c == null ? 1 : c + 1              // result aggregator
		);

		range2NonFriends.iterate(System.out::println);
	}

	static final class Person
	{
		final String           name   ;
		final HashEnum<Person> friends = HashEnum.New();

		Person(final String name)
		{
			super();
			this.name = name;
		}

		@Override
		public String toString()
		{
			return this.name;
		}

	}



	///////////////////////////////////////////////////////////////////////////
	// reusable querying logic ("API") //
	////////////////////////////////////

	static <E, R> XTable<E, R> queryDeep(
		final XIterable<? extends E> roots,
		final int depth,
		final Function<E, Iterable<E>> iterationResolver,
		final Predicate<? super E> targetPredicate,
		final BiFunction<? super E, R, R> joiner
	)
	{
		return roots.iterate(new DepthQuery<>(depth, iterationResolver, targetPredicate, joiner)).yield();
	}

	/* Prototype depth-graph-search implementation similar to example in
	 * http://www.infoq.com/presentations/graph-data-tools-techniques
	 *
	 * Of course concrete Person/friends structure could be abstracted a little bit nicer,
	 * a nodePredicate could be added,
	 * aggregation/joining could be made more flexible,
	 * etc.
	 *
	 * The goal is to show that no alien query language on an alien data structure is needed but instead specific
	 * types of querying possibilities can be tailored according to requirements in a matter of minutes in Java,
	 * resulting in a much more integrated and performant (pure Java) application instead of a mixture of
	 * different applications and languages with massive drawbacks like mapping effort / impedance mismatches,
	 * communication bottlenecks, etc.
	 * What all so-called "databases" like Neo4J do is to assume they are the main server application with their
	 * own quering/programming language and their own data persistence layer, causing the above deficiencies.
	 * What is actually only needed (what e.g. Neo4J SHOULD have done if anything) is developing datastructure concepts
	 * natively in Java and separate all other concearns (language, persistence) into dedicated modules.
	 */
	static final class DepthQuery<E, R> implements Aggregator<E, XTable<E, R>>
	{
		final HashEnum<E>                 alreadyHandledNode = HashEnum.New();
		final HashTable<E, R>             result             = HashTable.New();
		final Function<E, Iterable<E>>    iterationResolver ;
		final Predicate<? super E>        targetPredicate   ;
		final BiFunction<? super E, R, R> joiner            ;
		int                               depth             ;

		public DepthQuery(
			final int depth,
			final Function<E, Iterable<E>> iterationResolver,
			final Predicate<? super E> targetPredicate,
			final BiFunction<? super E, R, R> joiner
		)
		{
			super();
			this.depth             = depth          ;
			this.iterationResolver = iterationResolver;
			this.targetPredicate   = targetPredicate;
			this.joiner            = joiner         ;
		}

		private void handleRecursionNode(final E node, final int remainingDepth)
		{
			if(remainingDepth == 0)
			{
				this.debugPrint(remainingDepth, "Leaf handling friends of node "+node);
				for(final E friend : this.iterationResolver.apply(node))
				{
					this.handleRecursionLeaf(friend);
				}
				return;
			}

			this.debugPrint(remainingDepth, "Node handling friends of node "+node);

			final int nextDepth = remainingDepth - 1;
			for(final E friend : this.iterationResolver.apply(node))
			{
				if(this.alreadyHandledNode.add(friend))
				{
					this.handleRecursionNode(friend, nextDepth);
				}
			}
		}

		private void handleRecursionLeaf(final E leaf)
		{
			this.debugPrint(0, "Handling leaf "+leaf);
			if(this.targetPredicate.test(leaf))
			{
				this.debugPrint(0, "Adding target "+leaf);
				this.result.put(leaf, this.joiner.apply(leaf, this.result.get(leaf)));
			}
		}


		@Override
		public void accept(final E element)
		{
			this.alreadyHandledNode.add(element);
			this.handleRecursionNode(element, this.depth);
		}

		@Override
		public final HashTable<E, R> yield()
		{
			return this.result;
		}

		private void debugPrint(final int remainingDepth, final String message)
		{
			System.out.println(VarString.New().tab(this.depth - remainingDepth).toString()+ message);
		}

	}



	///////////////////////////////////////////////////////////////////////////
	// tiny example "graph" "database" //
	////////////////////////////////////

	static final EqConstHashTable<String, Person> initializePersons()
	{
		final EqHashTable<String, Person> persons = EqHashTable.New();

		final Person p00 = createPerson(persons, new Person("p00"));
		final Person p01 = createPerson(persons, new Person("p01"));
		final Person p02 = createPerson(persons, new Person("p02"));
		final Person p03 = createPerson(persons, new Person("p03"));
		final Person p04 = createPerson(persons, new Person("p04"));
		final Person p05 = createPerson(persons, new Person("p05"));
		final Person p06 = createPerson(persons, new Person("p06"));
		final Person p07 = createPerson(persons, new Person("p07"));
		final Person p08 = createPerson(persons, new Person("p08"));
		final Person p09 = createPerson(persons, new Person("p09"));

		friends(p00, p01);
		friends(p00, p02);
		friends(p00, p03);

		friends(p01, p03);
		friends(p01, p04);
		friends(p01, p05);

		friends(p02, p03);
		friends(p02, p08);
		friends(p02, p09);

		friends(p03, p09);
		friends(p03, p06);

		friends(p04, p05);
		friends(p04, p07);
		friends(p04, p09);

		return persons.immure();
	}

	static Person createPerson(final EqHashTable<String, Person> persons, final Person person)
	{
		persons.add(person.name, person);
		return person;
	}

	static void friends(final Person p1, final Person p2)
	{
		p1.friends.add(p2);
		p2.friends.add(p1);
	}

}

