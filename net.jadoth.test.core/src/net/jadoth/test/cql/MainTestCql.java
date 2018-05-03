package net.jadoth.test.cql;

import static net.jadoth.cql.CQL.not;

import net.jadoth.X;
import net.jadoth.collections.BulkList;
import net.jadoth.collections.EqHashTable;
import net.jadoth.collections.JadothSort;
import net.jadoth.collections.KeyValue;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.collections.types.XImmutableCollection;
import net.jadoth.collections.types.XList;
import net.jadoth.collections.types.XSequence;
import net.jadoth.cql.CQL;
import net.jadoth.cql.CqlAggregation;
import net.jadoth.cql.CqlProjection;
import net.jadoth.cql.CqlQuery;
import net.jadoth.cql.CqlSelection;
import net.jadoth.cql.CqlTransfer;
import net.jadoth.meta.JadothDebug;

public class MainTestCql
{
	// (07.04.2017 TM)NOTE: Test-Commit privat per VPN
	
	static final XList<String> STRINGS = X.List("class", "result", "am", "", "test", "", "I", "the");

	public static void main(final String[] args)
	{
		CQL.where(String::isEmpty).and(String::isEmpty).or(String::isEmpty);

		final CqlSelection<String> qs = CQL
			.select(CQL.not(String::isEmpty))
			.skip(2)
			.limit(3)
		;
		final XSequence<String> r_qs1 = qs.executeOn(STRINGS);
		print(r_qs1.sort(JadothSort::compareLength));

		final BulkList<String> r_qs2 = qs.executeInto(STRINGS, BulkList.<String>New());
		print(r_qs2.sort(JadothSort::compareLength));

		


		final CqlProjection<String, Integer> qsp = CQL
			.select(CQL.not(String::isEmpty))
			.project(String::length)
		;

		final XSequence<Integer> r_qsp1 = qsp.executeOn(STRINGS);
		print(r_qsp1.sort(Integer::compare));

		final BulkList<Integer> r_qsp2 =  qsp.executeInto(STRINGS, BulkList.<Integer>New());
		print(r_qsp2.sort(Integer::compare));

		final CqlSelection<String> qsf = CQL
			.select (CQL.not(String::isEmpty))
			.from   (STRINGS)
			.orderBy(JadothSort::compareLength)
		;
		final XGettingCollection<String> r_qsf = qsf.execute();
		print(r_qsf);

		final CqlProjection<String, Integer> qsfp = CQL
			.select(CQL.not(String::isEmpty))
			.from(STRINGS)
			.project(String::length)
			.orderBy(Integer::compare)
		;
		final XGettingCollection<Integer> r_qsfp = qsfp.execute();
		print(r_qsfp);

		final CqlTransfer<String, BulkList<String>> qt = CQL
			.select(CQL.not(String::isEmpty))
			.into(BulkList.<String>New())
		;
		final BulkList<String> r_qt1 = qt.execute();
		print(r_qt1);

		final CqlQuery<String, Integer, BulkList<Integer>> qspt = CQL
			.select(CQL.not(String::isEmpty))
			.project(String::length)
			.into(BulkList.<Integer>New())
		;
		final BulkList<Integer> r_qspt1 = qspt.execute();
		print(r_qspt1);

		final CqlQuery<String, Integer, BulkList<Integer>> qssptsl = CQL
			.select(CQL.not(String::isEmpty))
			.from(
				CQL.select(CQL.not(String::isEmpty)).from(STRINGS)
			)
			.project(String::length)
//			.into(CQL.resultingBulkList())
//			.into(CqlResultor.New((Supplier<BulkList<Integer>>)BulkList::New))
//			.into(CQL.resulting(() -> BulkList.<Integer>New()))
			.into(() -> BulkList.<Integer>New())
			.skip(5)
			.limit(10)
		;
		final BulkList<Integer> r_qssptsl = qssptsl.executeOn(STRINGS);
		print(r_qssptsl);

		final CqlQuery<String, Integer, XImmutableCollection<Integer>> qsfposl = CQL
			.select(CQL.not(String::isEmpty))
			.from(
				CQL
				.select(CQL.not(String::isEmpty))
				.from(STRINGS)
			)
			.project(String::length)
			.over(x -> XImmutableCollection.<Integer>Builder())
			.skip(5)
			.limit(10)
		;
		final XImmutableCollection<Integer> r_qsfposl = qsfposl.execute();
		print(BulkList.New(r_qsfposl).sort(Integer::compare));

		final EqHashTable<Integer, BulkList<String>> uber1337Aggregation =
			CQL
			.<KeyValue<Integer, String>, EqHashTable<Integer, BulkList<String>>>aggregate(
				EqHashTable::New,
				(e, r) -> r.ensure(e.key(), i -> BulkList.<String>New(i)).add(e.value()),
				r -> r.keys().sort(Integer::compare)
			)
			.from(
				CQL
				.select(not(String::isEmpty))
				.from(STRINGS)
				.project(e -> X.KeyValue(e.length(), e))
			)
			.execute()
		;

		uber1337Aggregation.iterate(e -> System.out.println(e.key()+" >> "+e.value()));

		final CqlAggregation<String, Double> query =
			CQL
			.aggregate(CQL.sum(String::length))
			.from(STRINGS)
		;
		System.out.println(query.execute());
		
		final XList<Object[]> result = CQL
			.project(
				String::length,
				String::isEmpty
			)
			.from(STRINGS)
			.select(s ->
				s.length() <= 3
			)
			.executeInto(X.List())
		;
		System.out.println(result);
	}



	static void print(final XGettingCollection<?> elements)
	{
		JadothDebug.debugln(elements.toString(), 1);
	}

}
