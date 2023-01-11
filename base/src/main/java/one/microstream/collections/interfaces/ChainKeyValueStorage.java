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
import java.util.ListIterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import one.microstream.chars.VarString;
import one.microstream.collections.AbstractChainEntry;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.equality.Equalator;
import one.microstream.functional.IndexedAcceptor;
import one.microstream.reference.ReferenceType;
import one.microstream.typing.KeyValue;

public interface ChainKeyValueStorage<K, V, EN extends AbstractChainEntry<KeyValue<K, V>, K, V, EN>>
extends ChainStorage<KeyValue<K, V>, K, V, EN>
{

	@Override
	public EN getChainEntry(long index) throws IndexOutOfBoundsException;

	@Override
	public EN getRangeChainEntry(long offset, long length) throws IndexOutOfBoundsException;

	@Override
	public EN getIntervalLowChainEntry(long lowIndex, long highIndex) throws IndexOutOfBoundsException;



	///////////////////////////////////////////////////////////////////////////
	// special map logic //
	//////////////////////

	public V searchValue(K key, Equalator<? super K> equalator);



	///////////////////////////////////////////////////////////////////////////
	// content info //
	/////////////////

	@Override
	public long size();

	/**
	 * Removes all empty entries from the passed chain and returns the number of removed entries.
	 *
	 * @return the number of removed entries.
	 */
	@Override
	public long consolidate();

	@Override
	public boolean hasVolatileElements();

	@Override
	public ReferenceType getReferenceType();

	public Iterator<K> keyIterator();

	public boolean keyEqualsContent(XGettingCollection<? extends K> other, Equalator<? super K> equalator);

	@Override
	public void removeRange(long offset, long length);

	/**
	 * Removes all entries at the indices (offsets) given in the provided {@code int} array.
	 * <p>
	 * Note that the indices array gets presorted to increase algorithm performance. If the original {@code int} array
	 * shall be unchanged, a clone must be passed.
	 *
	 * @param indices the indices (offsets) of the entries to be removed.
	 * @return the amount of actually removed entries.
	 */
	@Override
	public long removeSelection(long... indices);



	///////////////////////////////////////////////////////////////////////////
	// containing //
	///////////////

	// containing - null //

	public boolean keyContainsNull();

	public boolean keyContainsId(K element);

	public boolean keyContains(K element);

	public boolean keyContains(K sample, Equalator<? super K> equalator);

	public boolean keyContainsAll(K[] elements, int elementsOffset, int elementsLength);

	public boolean keyContainsAll(XGettingCollection<? extends K> elements);




	///////////////////////////////////////////////////////////////////////////
	// applying //
	/////////////

	public boolean keyApplies(Predicate<? super K> predicate);

	public boolean keyAppliesAll(Predicate<? super K> predicate);



	///////////////////////////////////////////////////////////////////////////
	// counting //
	/////////////

	public int keyCount(K element);

	public int keyCount(K sample, Equalator<? super K> equalator);

	public int keyCount(Predicate<? super K> predicate);



	///////////////////////////////////////////////////////////////////////////
	// data arithmetic //
	////////////////////

	public <C extends Consumer<? super K>> C keyIntersect(
		XGettingCollection<? extends K> collection,
		Equalator<? super K> equalator,
		C target
	);

	public <C extends Consumer<? super K>> C keyExcept(
		XGettingCollection<? extends K> collection,
		Equalator<? super K> equalator,
		C target
	);

	public <C extends Consumer<? super K>> C keyUnion(
		XGettingCollection<? extends K> collection,
		Equalator<? super K> equalator,
		C target
	);

	public <C extends Consumer<? super K>> C keyCopyTo(C target);

	public <C extends Consumer<? super K>> C keyCopySelection(C target, long... indices);

	public int keyCopyToArray(int offset, int length, Object[] target, int targetOffset);

	public <C extends Consumer<? super K>> C keyCopyTo(C target, Predicate<? super K> predicate);

	public Object[] keyToArray();

	public      K[] keyToArray(Class<K> type);


	///////////////////////////////////////////////////////////////////////////
	// querying //
	/////////////

	public K keyFirst();

	public K keyLast();

	public K keyGet(long index);



	///////////////////////////////////////////////////////////////////////////
	// searching //
	//////////////

	public K keySeek(K sample);

	public K keySeek(K sample, Equalator<? super K> equalator);

	public K keySearch(Predicate<? super K> predicate);

	public K keyMin(Comparator<? super K> keyComparator);

	public K keyMax(Comparator<? super K> keyComparator);



	///////////////////////////////////////////////////////////////////////////
	// executing //
	//////////////

	public void keyIterate(Consumer<? super K> procedure);

	public <A> void keyJoin(BiConsumer<? super K, A> joiner, A keyAggregate);

	public void keyIterateIndexed(IndexedAcceptor<? super K> procedure);

	public void keyIterate(Predicate<? super K> predicate, Consumer<? super K> procedure);



	///////////////////////////////////////////////////////////////////////////
	// indexing //
	/////////////

	public int keyIndexOf(K element);

	public int keyIndexOf(K sample, Equalator<? super K> equalator);

	public int keyIndexBy(Predicate<? super K> predicate);

	public int keyLastIndexOf(K element);

	public int keyLastIndexOf(K sample, Equalator<? super K> equalator);

	public int keyLastIndexBy(Predicate<? super K> predicate);

	public int keyMinIndex(Comparator<? super K> keyComparator);

	public int keyMaxIndex(Comparator<? super K> keyComparator);

	public int keyScan(Predicate<? super K> predicate);



	///////////////////////////////////////////////////////////////////////////
	// distinction //
	////////////////

	public boolean keyHasDistinctValues();

	public boolean keyHasDistinctValues(Equalator<? super K> equalator);

	public <C extends Consumer<? super K>> C keyDistinct(C target);

	public <C extends Consumer<? super K>> C keyDistinct(C target, Equalator<? super K> equalator);
	


	///////////////////////////////////////////////////////////////////////////
	// VarString appending //
	////////////////////////

	public VarString keyAppendTo(VarString vc);

	public VarString keyAppendTo(VarString vc, char separator);

	public VarString keyAppendTo(VarString vc, String separator);

	public VarString keyAppendTo(VarString vc, BiConsumer<VarString, ? super K> keyAppender);

	public VarString keyAppendTo(VarString vc, BiConsumer<VarString, ? super K> keyAppender, char separator);

	public VarString keyAppendTo(VarString vc, BiConsumer<VarString, ? super K> keyAppender, String separator);
	


	///////////////////////////////////////////////////////////////////////////
	// removing //
	/////////////

	public K keyRemove(final long index);

	public int keyRemoveNull();

	public K keyRetrieve(K element);

	public K keyRetrieve(K sample, Equalator<? super K> equalator);

	public K keyRetrieve(Predicate<? super K> predicate);

	public boolean keyRemoveOne(K element);

	public boolean keyRemoveOne(K sample, Equalator<? super K> equalator);

	public int keyRemove(K element);

	public int keyRemove(K sample, Equalator<? super K> equalator);

	public int keyRemoveAll(K[] elements, int elementsOffset, int elementsLength);

	public int keyRemoveAll(XGettingCollection<? extends K> elements);

	public int keyRemoveDuplicates();

	public int keyRemoveDuplicates(Equalator<? super K> equalator);



	///////////////////////////////////////////////////////////////////////////
	// reducing //
	/////////////

	public int keyReduce(Predicate<? super K> predicate);



	///////////////////////////////////////////////////////////////////////////
	// retaining //
	//////////////

	public int keyRetainAll(K[] elements, int elementsOffset, int elementsLength);

	public int keyRetainAll(XGettingCollection<? extends K> elements);

	public int keyRetainAll(XGettingCollection<? extends K> samples, Equalator<? super K> equalator);



	///////////////////////////////////////////////////////////////////////////
	// processing //
	///////////////

	public int keyProcess(Consumer<? super K> procedure);



	///////////////////////////////////////////////////////////////////////////
	// moving //
	///////////

	public int keyMoveRange(int offset, int length, Consumer<? super K> target);

	public int keyMoveSelection(Consumer<? super K> target, long... indices);

	public int keyMoveTo(Consumer<? super K> target, Predicate<? super K> predicate);



	///////////////////////////////////////////////////////////////////////////
	// sorting //
	////////////

	public void keySort(Comparator<? super K> keyComparator);

	public boolean keyIsSorted(Comparator<? super K> keyComparator);



	///////////////////////////////////////////////////////////////////////////
	// setting //
	////////////

	@SuppressWarnings("unchecked")
	public void keySet(int offset, K... elements);

	public void keySet(int offset, K[] elements, int elementsOffset, int elementsLength);

	public void keyFill(int offset, int length, K element);



	///////////////////////////////////////////////////////////////////////////
	// replacing //
	//////////////

	public int keyReplaceOne(K element, K replacement);

	public int keyReplace(K element, K replacement);

	public int keyReplaceAll(K[] elements, int elementsOffset, int elementsLength, K replacement);

	public int keyReplaceAll(XGettingCollection<? extends K> elements, K replacement);

	public long keySubstitute(Function<? super K, ? extends K> mapper, BiConsumer<EN, K> callback);

	public int keySubstitute(Predicate<? super K> predicate, Function<? super K, ? extends K> mapper);

	public int keySubstituteOne(Predicate<? super K> predicate, K keySubstitute);

	public int keySubstitute(Predicate<? super K> predicate, K keySubstitute);

	
	
	/////////////////////////////////////
	////
	////  Values
	////
	/////////////////////////////////////

	public boolean hasVolatileValues();

	public ReferenceType getValueReferenceType();

	public Iterator<V> valuesIterator();

	public ListIterator<V> valuesListIterator(long index);

	public boolean valuesEqualsContent(final XGettingCollection<? extends V> other, final Equalator<? super V> equalator);




	///////////////////////////////////////////////////////////////////////////
	// containing //
	///////////////

	// containing - null //

	public boolean valuesContainsNull();

	// containing - identity //

	public boolean valuesContainsId(V element);

	// containing - logical //

	public boolean valuesContains(V element);

	public boolean valuesContains(V sample, Equalator<? super V> equalator);
	
	// containing - all array //

	public boolean valuesContainsAll(V[] values, int elementsOffset, int elementsLength);

	public boolean valuesContainsAll(V[] values, int elementsOffset, int elementsLength, Equalator<? super V> equalator);

	// containing - all collection //

	public boolean valuesContainsAll(XGettingCollection<? extends V> elements);

	public boolean valuesContainsAll(XGettingCollection<? extends V> elements, Equalator<? super V> equalator);



	///////////////////////////////////////////////////////////////////////////
	// applying //
	/////////////

	public boolean valuesApplies(Predicate<? super V> predicate);

	public boolean valuesAppliesAll(Predicate<? super V> predicate);



	///////////////////////////////////////////////////////////////////////////
	// counting //
	/////////////

	public int valuesCount(V element);

	public int valuesCount(V sample, Equalator<? super V> equalator);

	public int valuesCount(Predicate<? super V> predicate);



	///////////////////////////////////////////////////////////////////////////
	// data arithmetic //
	////////////////////

	// data - data sets //

	public <C extends Consumer<? super V>> C valuesIntersect(
		XGettingCollection<? extends V> collection,
		Equalator<? super V> equalator,
		C target
	);

	public <C extends Consumer<? super V>> C valuesExcept(
		XGettingCollection<? extends V> collection,
		Equalator<? super V> equalator,
		C target
	);

	public <C extends Consumer<? super V>> C valuesUnion(
		XGettingCollection<? extends V> collection,
		Equalator<? super V> equalator,
		C target
	);

	// data - copying //

	public <C extends Consumer<? super V>> C valuesCopyTo(C target);

	public <C extends Consumer<? super V>> C valuesCopySelection(C target, long... indices);

	public int valuesCopyToArray(long offset, int length, Object[] target, int targetOffset);

	// data - conditional copying //

	public <C extends Consumer<? super V>> C valuesCopyTo(C target, Predicate<? super V> predicate);

	// data - array transformation //

	public Object[] valuesToArray();

	public      V[] valuesToArray(Class<V> type);



	///////////////////////////////////////////////////////////////////////////
	// querying //
	/////////////

	public V valuesFirst();

	public V valuesLast();

	public V valuesGet(long index);



	///////////////////////////////////////////////////////////////////////////
	// searching //
	//////////////

	// searching - sample //

	public V valuesSeek(V sample);

	public V valuesSearch(V sample, Equalator<? super V> equalator);

	// searching - predicate //

	public V valuesSearch(Predicate<? super V> predicate);

	// searching - min max //

	public V valuesMin(Comparator<? super V> comparator);

	public V valuesMax(Comparator<? super V> comparator);



	///////////////////////////////////////////////////////////////////////////
	// iterating //
	//////////////

	public void valuesIterate(Consumer<? super V> procedure);

	public void valuesIterateIndexed(IndexedAcceptor<? super V> procedure);

	public <A> void valuesJoin(BiConsumer<? super V, A> joiner, A aggregate);

	public void valuesIterate(Predicate<? super V> predicate, Consumer<? super V> procedure);



	///////////////////////////////////////////////////////////////////////////
	// indexing //
	/////////////

	// indexing - single //

	public int valuesIndexOf(V element);

	public int valuesIndexOf(V sample, Equalator<? super V> equalator);

	public int valuesIndexBy(Predicate<? super V> predicate);

	public int valuesLastIndexOf(V element);

	public int valuesLastIndexOf(V sample, Equalator<? super V> equalator);

	public int valuesLastIndexBy(Predicate<? super V> predicate);

	// indexing - min max //

	public int valuesMinIndex(Comparator<? super V> comparator);

	public int valuesMaxIndex(Comparator<? super V> comparator);

	// indexing - scan //

	public int valuesScan(Predicate<? super V> predicate);



	///////////////////////////////////////////////////////////////////////////
	// distinction //
	////////////////

	// distinction querying //

	public boolean valuesHasDistinctValues();

	public boolean valuesHasDistinctValues(Equalator<? super V> equalator);

	// distinction copying //

	public <C extends Consumer<? super V>> C valuesDistinct(C target);

	public <C extends Consumer<? super V>> C valuesDistinct(C target, Equalator<? super V> equalator);



	///////////////////////////////////////////////////////////////////////////
	// VarString appending //
	////////////////////////

	public VarString valuesAppendTo(VarString vc);

	public VarString valuesAppendTo(VarString vc, char separator);

	public VarString valuesAppendTo(VarString vc, String separator);

	public VarString valuesAppendTo(VarString vc, BiConsumer<VarString, ? super V> appender);

	public VarString valuesAppendTo(VarString vc, BiConsumer<VarString, ? super V> appender, char separator);

	public VarString valuesAppendTo(VarString vc, BiConsumer<VarString, ? super V> appender, String separator);

	public String valuesToString();



	///////////////////////////////////////////////////////////////////////////
	// removing //
	/////////////

	// removing - indexed //

	public V valuesRemove(final long index);

	// removing - null //

	public int valuesRemoveNull();

	// removing - one single //

	public V valuesRetrieve(V element);

	public V valuesRetrieve(Predicate<? super V> predicate);

	public boolean valuesRemoveOne(V element);

	public boolean valuesRemoveOne(V sample, Equalator<? super V> equalator);

	// removing - multiple single //

	public int valuesRemove(V element);

	public int valuesRemove(V sample, Equalator<? super V> equalator);

	// removing - multiple all array //

	public int valuesRemoveAll(V[] values, int elementsOffset, int elementsLength);

	public int valuesRemoveAll(V[] samples, int samplesOffset, int samplesLength, Equalator<? super V> equalator);

	// removing - multiple all collection //

	public int valuesRemoveAll(XGettingCollection<? extends V> elements);

	public int valuesRemoveAll(XGettingCollection<? extends V> samples, Equalator<? super V> equalator);

	// removing - duplicates //

	public int valuesRemoveDuplicates();

	public int valuesRemoveDuplicates(Equalator<? super V> equalator);



	///////////////////////////////////////////////////////////////////////////
	// reducing //
	/////////////

	public int valuesReduce(Predicate<? super V> predicate);



	///////////////////////////////////////////////////////////////////////////
	// retaining //
	//////////////

	public int valuesRetainAll(V[] values, int elementsOffset, int elementsLength);

	public int valuesRetainAll(V[] samples, int elementsOffset, int elementsLength, Equalator<? super V> equalator);

	public int valuesRetainAll(XGettingCollection<? extends V> elements);

	public int valuesRetainAll(XGettingCollection<? extends V> samples, Equalator<? super V> equalator);


	///////////////////////////////////////////////////////////////////////////
	// processing //
	///////////////

	public int valuesProcess(Consumer<? super V> procedure);



	///////////////////////////////////////////////////////////////////////////
	// moving //
	///////////

	public int valuesMoveRange(int offset, int length, Consumer<? super V> target);

	public int valuesMoveSelection(Consumer<? super V> target, long... indices);

	public int valuesMoveTo(Consumer<? super V> target, Predicate<? super V> predicate);



	///////////////////////////////////////////////////////////////////////////
	// sorting //
	////////////

	public void valuesSort(Comparator<? super V> comparator);

	public boolean valuesIsSorted(Comparator<? super V> comparator);



	///////////////////////////////////////////////////////////////////////////
	// setting //
	////////////

	public V valuesSet(long offset, V value);

	public void valuesSet(long offset, V[] values);

	public void valuesSet(long offset, V[] values, int valuesOffset, int valuesLength);

	public void valuesFill(long offset, long length, V value);



	///////////////////////////////////////////////////////////////////////////
	// replacing //
	//////////////

	public boolean valuesReplaceOne(V element, V replacement);

	public boolean valuesReplaceOne(V sample, Equalator<? super V> equalator, V replacement);

	public int valuesReplace(V element, V replacement);

	public int valuesReplace(V sample, Equalator<? super V> equalator, V replacement);

	public int valuesReplaceAll(V[] values, int valuesOffset, int valuesLength, V replacement);

	public int valuesReplaceAll(
		V[] samples,
		int samplesOffset,
		int samplesLength,
		Equalator<? super V> equalator,
		V replacement
	);

	public int valuesReplaceAll(XGettingCollection<? extends V> elements, V replacement);

	public int valuesReplaceAll(XGettingCollection<? extends V> samples, Equalator<? super V> equalator, V replacement);

	public int valuesSubstitute(Function<? super V, ? extends V> mapper);

	public int valuesSubstitute(Predicate<? super V> predicate, Function<V, V> mapper);

	

	///////////////////////////////////////////////////////////////////////////
	// substituting //
	/////////////////

	public boolean valuesSubstituteOne(Predicate<? super V> predicate, V substitute);

	public int valuesSubstitute(Predicate<? super V> predicate, V substitute);



	///////////////////////////////////////////////////////////////////////////
	// swapping //
	/////////////

	@Override
	public void shiftTo(final long sourceIndex, final long targetIndex);

	@Override
	public void shiftTo(final long sourceIndex, final long targetIndex, final long length);

	@Override
	public void shiftBy(final long sourceIndex, final long distance);

	@Override
	public void shiftBy(final long sourceIndex, final long distance, final long length);

	@Override
	public void swap(long indexA, long indexB);

	@Override
	public void swap(long indexA, long indexB, long length);

	@Override
	public void reverse();

	@Override
	public void shuffle();

}
