package one.microstream.collections.interfaces;

/*-
 * #%L
 * microstream-base
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

import java.util.Comparator;
import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import one.microstream.chars.VarString;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.equality.Equalator;
import one.microstream.functional.IndexedAcceptor;
import one.microstream.reference.ReferenceType;
import one.microstream.typing.Composition;


public interface ChainStorage<E, K, V, EN extends ChainStorage.Entry<E, K, V, EN>> extends Iterable<E>, Composition
{
	public interface Entry<E, K, V, EN extends Entry<E, K, V, EN>> extends Composition
	{
		// only marker interface so far
	}


	public void appendEntry(EN entry);

	public void prependEntry(EN entry);

	public void clear();



	///////////////////////////////////////////////////////////////////////////
	// chain navigation //
	/////////////////////

	public EN getChainEntry(long index) throws IndexOutOfBoundsException;

	public EN getRangeChainEntry(long offset, long length) throws IndexOutOfBoundsException;

	public EN getIntervalLowChainEntry(long lowIndex, long highIndex) throws IndexOutOfBoundsException;



	///////////////////////////////////////////////////////////////////////////
	// content info //
	/////////////////

	public long size();

	/**
	 * Removes all empty entries from the passed chain and returns the number of removed entries.
	 *
	 * @return the number of removed entries.
	 */
	public long consolidate();

	public boolean hasVolatileElements();

	public ReferenceType getReferenceType();

	@Override
	public Iterator<E> iterator();

	public boolean equalsContent(XGettingCollection<? extends E> other, Equalator<? super E> equalator);



	///////////////////////////////////////////////////////////////////////////
	// containing //
	///////////////

	public boolean containsNull();

	public boolean containsId(E element);

	public boolean contains(E element);

	public boolean contains(E sample, Equalator<? super E> equalator);

	public boolean containsAll(E[] elements, int elementsOffset, int elementsLength);

	public boolean containsAll(XGettingCollection<? extends E> elements);



	///////////////////////////////////////////////////////////////////////////
	// applying //
	/////////////

	public boolean containsSearched(Predicate<? super E> predicate);

	public boolean appliesAll(Predicate<? super E> predicate);



	///////////////////////////////////////////////////////////////////////////
	// counting //
	/////////////

	public long count(E element);

	public long count(E sample, Equalator<? super E> equalator);

	public long count(Predicate<? super E> predicate);



	///////////////////////////////////////////////////////////////////////////
	// data arithmetic //
	////////////////////

	// data - data sets //

	public <C extends Consumer<? super E>> C intersect(
		XGettingCollection<? extends E> samples,
		Equalator<? super E> equalator,
		C target
	);

	public <C extends Consumer<? super E>> C except(
		XGettingCollection<? extends E> samples,
		Equalator<? super E> equalator,
		C target
	);

	public <C extends Consumer<? super E>> C union(
		XGettingCollection<? extends E> samples,
		Equalator<? super E> equalator,
		C target
	);

	// data - copying //

	public <C extends Consumer<? super E>> C copyTo(C target);

	public <C extends Consumer<? super E>> C copySelection(C target, long[] indices);

	public int copyToArray(long offset, int length, Object[] target, int targetOffset);

	public <C extends Consumer<? super E>> C copyTo(C target, Predicate<? super E> predicate);

	// data - array transformation //

	public Object[] toArray();

	public      E[] toArray(Class<E> type);



	///////////////////////////////////////////////////////////////////////////
	// querying //
	/////////////

	public E first();

	public E last();

	public E get(long index);



	///////////////////////////////////////////////////////////////////////////
	// searching //
	//////////////

	// searching - sample //

	public E seek(E sample);

	public E seek(E sample, Equalator<? super E> equalator);

	// searching - predicate //

	public E search(Predicate<? super E> predicate);

	// searching - min max //

	public E min(Comparator<? super E> comparator);

	public E max(Comparator<? super E> comparator);



	///////////////////////////////////////////////////////////////////////////
	// executing //
	//////////////

	// executing - procedure //

	public void iterate(Consumer<? super E> procedure);

	public <A> void join(BiConsumer<? super E, A> joiner, A aggregate);

	// executing - indexed procedure //

	public void iterateIndexed(IndexedAcceptor<? super E> procedure);

	// executing - conditional //

	// (14.01.2013 TM)FIXME: replace (predicate, procedure) variants by simple (procedure) with wrapping procedure

	public void iterate(Predicate<? super E> predicate, Consumer<? super E> procedure);

	// executing - conditional, limited //




	///////////////////////////////////////////////////////////////////////////
	// indexing //
	/////////////

	// indexing - single //

	public long indexOf(E element);

	public long indexOf(E sample, Equalator<? super E> equalator);

	public long indexOf(Predicate<? super E> predicate);

	public long lastIndexOf(E element);

	public long lastIndexOf(E sample, Equalator<? super E> equalator);

	public long lastIndexBy(Predicate<? super E> predicate);

	// indexing - min max //

	public long minIndex(Comparator<? super E> comparator);

	public long maxIndex(Comparator<? super E> comparator);

	// indexing - scan //

	public long scan(Predicate<? super E> predicate);



	///////////////////////////////////////////////////////////////////////////
	// distinction //
	////////////////

	// distinction querying //

	public boolean hasDistinctValues();

	public boolean hasDistinctValues(Equalator<? super E> equalator);

	// distinction copying //

	public <C extends Consumer<? super E>> C distinct(C target);

	public <C extends Consumer<? super E>> C distinct(C target, Equalator<? super E> equalator);



	///////////////////////////////////////////////////////////////////////////
	// VarString appending //
	//////////////////////

	public VarString appendTo(VarString vc);

	public VarString appendTo(VarString vc, char separator);

	public VarString appendTo(VarString vc, String separator);

	public VarString appendTo(VarString vc, BiConsumer<VarString, ? super E> appender);

	public VarString appendTo(VarString vc, BiConsumer<VarString, ? super E> appender, char separator);

	public VarString appendTo(VarString vc, BiConsumer<VarString, ? super E> appender, String separator);

	@Override
	public String toString();



	///////////////////////////////////////////////////////////////////////////
	// removing //
	/////////////

	// removing - indexed //

	public E remove(long index);

	public void removeRange(long offset, long length);

	public void retainRange(long offset, long length);

	/**
	 * Removes all entries at the indices (offsets) given in the provided {@code int} array.
	 * <p>
	 * Note that the indices array gets presorted to increase algorithm performance. If the original {@code int} array
	 * shall be unchanged, a clone must be passed.
	 *
	 * @param indices the indices (offsets) of the entries to be removed.
	 * @return the amount of actually removed entries.
	 */
	public long removeSelection(long... indices);

	// removing - null //

	public long removeNull();

	// removing - one single //

	public E retrieve(E element);

	public E retrieve(E sample, Equalator<? super E> equalator);

	public E retrieve(Predicate<? super E> predicate);

	public boolean removeOne(E element);

	public boolean removeOne(E sample, Equalator<? super E> equalator);

	// removing - multiple single //

	public long remove(E element);

	public long remove(E sample, Equalator<? super E> equalator);

	// removing - multiple all array //

	public long removeAll(E[] elements, int elementsOffset, int elementsLength);

	// removing - multiple all collection //

	public long removeAll(XGettingCollection<? extends E> elements);

	// removing - duplicates //

	public long removeDuplicates();

	public long removeDuplicates(Equalator<? super E> equalator);



	///////////////////////////////////////////////////////////////////////////
	// reducing //
	/////////////

	public long reduce(Predicate<? super E> predicate);



	///////////////////////////////////////////////////////////////////////////
	// retaining //
	//////////////

	// retaining - array //

	public long retainAll(E[] elements, int elementsOffset, int elementsLength);

	public long retainAll(XGettingCollection<? extends E> elements);

	public long retainAll(XGettingCollection<? extends E> samples, Equalator<? super E> equalator);



	///////////////////////////////////////////////////////////////////////////
	// processing //
	///////////////

	public long process(Consumer<? super E> procedure);



	///////////////////////////////////////////////////////////////////////////
	// moving //
	///////////

	public long moveRange(long offset, long length, Consumer<? super E> target);

	public long moveSelection(Consumer<? super E> target, long... indices);

	public long moveTo(Consumer<? super E> target, Predicate<? super E> predicate);



	///////////////////////////////////////////////////////////////////////////
	// ordering //
	/////////////

	public void shiftTo(final long sourceIndex, final long targetIndex);

	public void shiftTo(final long sourceIndex, final long targetIndex, final long length);

	public void shiftBy(final long sourceIndex, final long distance);

	public void shiftBy(final long sourceIndex, final long distance, final long length);

	public void swap(long indexA, long indexB);

	public void swap(long indexA, long indexB, long length);

	public void reverse();



	///////////////////////////////////////////////////////////////////////////
	// sorting //
	////////////

	public void sort(Comparator<? super E> comparator);

	public boolean isSorted(Comparator<? super E> comparator);

	public void shuffle();



	///////////////////////////////////////////////////////////////////////////
	// setting //
	////////////

	public void set(long offset, E[] elements);

	public void set(long offset, E[] elements, int elementsOffset, int elementsLength);

	public void fill(long offset, long length, E element);



	///////////////////////////////////////////////////////////////////////////
	// replacing //
	//////////////

	// replacing - one single //

	public long replaceOne(E element, E replacement);

	// replacing - multiple single //

	public long replace(E element, E replacement);

	// replacing - multiple all array //

	public long replaceAll(E[] elements, int elementsOffset, int elementsLength, E replacement);

	// replacing - multiple all collection //

	public long replaceAll(XGettingCollection<? extends E> elements, E replacement);

	public long replaceOneBy(Predicate<? super E> predicate, E substitute);

	public long replaceBy(Predicate<? super E> predicate, E substitute);
	
	// replacing - mapped //

	public long substitute(Function<E, E> mapper);

	public long substitute(Predicate<? super E> predicate, Function<E, E> mapper);

}
