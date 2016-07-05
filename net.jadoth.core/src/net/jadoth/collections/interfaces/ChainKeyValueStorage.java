package net.jadoth.collections.interfaces;

import java.util.Comparator;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import net.jadoth.collections.AbstractChainEntry;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.functional.BiProcedure;
import net.jadoth.functional.IndexProcedure;
import net.jadoth.reference.ReferenceType;
import net.jadoth.util.Equalator;
import net.jadoth.util.KeyValue;
import net.jadoth.util.chars.VarString;

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
	//  content info    //
	/////////////////////

	@Override
	public long size();

	/**
	 * Removes all empty entries from the passed chain and returns the number of removed entries.
	 *
	 * @param <E> the element type of the passed chain.
	 * @param chain the chain to be consolidated (cleared of empty entries).
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
	 * Removes all entries at the indices (offsets) given in the passed {@code int} array.
	 * <p>
	 * Note that the indices array gets presorted to increase algorithm performance. If the original {@code int} array
	 * shall be unchanged, a clone must be passed.
	 *
	 * @param <E> the element type of the chain.
	 * @param chain the chain whose entries shall be removed.
	 * @param indices the indices (offsets) of the entries to be removed.
	 * @return the amount of actually removed entries.
	 */
	@Override
	public long removeSelection(long... indices);



	///////////////////////////////////////////////////////////////////////////
	//   containing     //
	/////////////////////

	// containing - null //

	public boolean keyContainsNull();

	public boolean keyRngContainsNull(final int offset, int length);

	public boolean keyContainsId(K element);

	public boolean keyRngContainsId(int offset, int length, K element);

	public boolean keyContains(K element);

	public boolean keyContains(K sample, Equalator<? super K> equalator);

	public boolean keyRngContains(int offset, int length, K element);

	public boolean keyContainsAll(K[] elements, int elementsOffset, int elementsLength);

	public boolean keyRngContainsAll(
		int offset,
		int length,
		K[] elements,
		int elementsOffset,
		int elementsLength
	);

	public boolean keyContainsAll(XGettingCollection<? extends K> elements);

	public boolean keyRngContainsAll(int offset, int length, XGettingCollection<? extends K> elements);




	///////////////////////////////////////////////////////////////////////////
	//    applying      //
	/////////////////////

	public boolean keyApplies(Predicate<? super K> predicate);

	public boolean keyRngApplies(int offset, int length, Predicate<? super K> predicate);

	public boolean keyAppliesAll(Predicate<? super K> predicate);

	public boolean keyRngAppliesAll(int offset, int length, Predicate<? super K> predicate);



	///////////////////////////////////////////////////////////////////////////
	//    counting      //
	/////////////////////

	public int keyCount(K element);

	public int keyCount(K sample, Equalator<? super K> equalator);

	public int keyRngCount(int offset, int length, K element);

	public int keyCount(Predicate<? super K> predicate);

	public int keyRngCount(int offset, int length, Predicate<? super K> predicate);



	///////////////////////////////////////////////////////////////////////////
	// data arithmetic  //
	/////////////////////

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

	public <C extends Consumer<? super K>> C keyRngIntersect(
		int offset,
		int length,
		XGettingCollection<? extends K> collection,
		Equalator<? super K> equalator,
		C target
	);

	public <C extends Consumer<? super K>> C keyRngExcept(
		int offset,
		int length,
		XGettingCollection<? extends K> collection,
		Equalator<? super K> equalator,
		C target
	);

	public <C extends Consumer<? super K>> C keyRngUnion(
		int offset,
		int length,
		XGettingCollection<? extends K> collection,
		Equalator<? super K> equalator,
		C target
	);

	public <C extends Consumer<? super K>> C keyCopyTo(C target);

	public <C extends Consumer<? super K>> C keyRngCopyTo(int offset, int length, C target);

	public <C extends Consumer<? super K>> C keyCopySelection(C target, long... indices);

	public int keyCopyToArray(int offset, int length, Object[] target, int targetOffset);

	public <C extends Consumer<? super K>> C keyCopyTo(C target, Predicate<? super K> predicate);

	public <C extends Consumer<? super K>> C keyRngCopyTo(
		int offset,
		int length,
		C target,
		Predicate<? super K> predicate
	);

	public Object[] keyToArray();

	public      K[] keyToArray(Class<K> type);

	public Object[] keyRngToArray(int offset, int length);

	public      K[] keyRngToArray(int offset, int length, Class<K> type);



	///////////////////////////////////////////////////////////////////////////
	//    querying      //
	/////////////////////

	public K keyFirst();

	public K keyLast();

	public K keyGet(long index);



	///////////////////////////////////////////////////////////////////////////
	//    searching     //
	/////////////////////

	public K keySeek(K sample);

	public K keySeek(K sample, Equalator<? super K> equalator);

	public K keySearch(Predicate<? super K> predicate);

	public K keyRngSearch(int offset, int length, Predicate<? super K> predicate);

	public K keyMin(Comparator<? super K> keyComparator);

	public K keyMax(Comparator<? super K> keyComparator);

	public K keyRngMin(int offset, int length, Comparator<? super K> keyComparator);

	public K keyRngMax(int offset, int length, Comparator<? super K> keyComparator);



	///////////////////////////////////////////////////////////////////////////
	//    executing     //
	/////////////////////

	public void keyIterate(Consumer<? super K> procedure);

	public <A> void keyJoin(BiProcedure<? super K, A> joiner, A keyAggregate);

	public void keyRngIterate(int offset, int length, Consumer<? super K> procedure);

	public void keyIterate(IndexProcedure<? super K> procedure);

	public void keyRngIterate(int offset, int length, IndexProcedure<? super K> procedure);

	public void keyIterate(Predicate<? super K> predicate, Consumer<? super K> procedure);



	///////////////////////////////////////////////////////////////////////////
	//    indexing      //
	/////////////////////

	public int keyIndexOf(K element);

	public int keyIndexOf(K sample, Equalator<? super K> equalator);

	public int keyRngIndexOf(int offset, int length, K element);

	public int keyRngIndexOf(int offset, int length, K sample, Equalator<? super K> equalator);

	public int keyIndexOf(Predicate<? super K> predicate);

	public int keyRngIndexOf(int offset, int length, Predicate<? super K> predicate);

	public int keyMinIndex(Comparator<? super K> keyComparator);

	public int keyMaxIndex(Comparator<? super K> keyComparator);

	public int keyRngMinIndex(int offset, int length, Comparator<? super K> keyComparator);

	public int keyRngMaxIndex(int offset, int length, Comparator<? super K> keyComparator);

	public int keyScan(Predicate<? super K> predicate);

	public int keyRngScan(int offset, int length, Predicate<? super K> predicate);



	///////////////////////////////////////////////////////////////////////////
	//   distinction    //
	/////////////////////

	public boolean keyHasDistinctValues();

	public boolean keyHasDistinctValues(Equalator<? super K> equalator);

	public <C extends Consumer<? super K>> C keyDistinct(C target);

	public <C extends Consumer<? super K>> C keyDistinct(C target, Equalator<? super K> equalator);

	public <C extends Consumer<? super K>> C keyRngDistinct(int offset, int length, C target);

	public <C extends Consumer<? super K>> C keyRngDistinct(
		int offset,
		int length,
		C target,
		Equalator<? super K> equalator
	);



	///////////////////////////////////////////////////////////////////////////
	// VarString appending //
	//////////////////////

	public VarString keyAppendTo(VarString vc);

	public VarString keyAppendTo(VarString vc, char separator);

	public VarString keyAppendTo(VarString vc, String separator);

	public VarString keyAppendTo(VarString vc, BiProcedure<VarString, ? super K> keyAppender);

	public VarString keyAppendTo(VarString vc, BiProcedure<VarString, ? super K> keyAppender, char separator);

	public VarString keyAppendTo(VarString vc, BiProcedure<VarString, ? super K> keyAppender, String separator);

	public VarString keyRngAppendTo(int offset, int length, VarString vc);

	public VarString keyRngAppendTo(int offset, int length, VarString vc, char separator);

	public VarString keyRngAppendTo(int offset, int length, VarString vc, String separator);

	public VarString keyRngAppendTo(int offset, int length, VarString vc, BiProcedure<VarString, ? super K> keyAppender);

	public VarString keyRngAppendTo(int offset, int length, VarString vc, BiProcedure<VarString, ? super K> keyAppender, char separator);

	public VarString keyRngAppendTo(int offset, int length, VarString vc, BiProcedure<VarString, ? super K> keyAppender, String separator);



	///////////////////////////////////////////////////////////////////////////
	//    removing      //
	/////////////////////

	public K keyRemove(final long index);

	public int keyRemoveNull();

	public int keyRngRemoveNull(long offset, long length);

	public K keyRetrieve(K element);

	public K keyRetrieve(K sample, Equalator<? super K> equalator);

	public K keyRetrieve(Predicate<? super K> predicate);

	public K keyRngRetrieve(long offset, long length, K element);

	public K keyRngRetrieve(long offset, long length, K sample, Equalator<? super K> equalator);

	public boolean keyRemoveOne(K element);

	public boolean keyRemoveOne(K sample, Equalator<? super K> equalator);

	public int keyRemove(K element);

	public int keyRemove(K sample, Equalator<? super K> equalator);

	public int keyRngRemove(int offset, int length, K element);

	public int keyRemoveAll(K[] elements, int elementsOffset, int elementsLength);

	public int keyRngRemoveAll(int offset, int length, K[] elements, int elementsOffset, int elementsLength);

	public int keyRemoveAll(XGettingCollection<? extends K> elements);

	public int keyRngRemoveAll(int offset, int length, XGettingCollection<? extends K> elements);

	public int keyRemoveDuplicates();

	public int keyRemoveDuplicates(Equalator<? super K> equalator);

	public int keyRngRemoveDuplicates(int offset, int length);

	public int keyRngRemoveDuplicates(int offset, int length, Equalator<? super K> equalator);



	///////////////////////////////////////////////////////////////////////////
	//     reducing     //
	/////////////////////

	public int keyReduce(Predicate<? super K> predicate);

	public int keyRngReduce(int offset, int length, Predicate<? super K> predicate);



	///////////////////////////////////////////////////////////////////////////
	//    retaining     //
	/////////////////////

	public int keyRetainAll(K[] elements, int elementsOffset, int elementsLength);

	public int keyRngRetainAll(int offset, int length, K[] elements, int elementsOffset, int elementsLength);

	public int keyRetainAll(XGettingCollection<? extends K> elements);

	public int keyRetainAll(XGettingCollection<? extends K> samples, Equalator<? super K> equalator);

	public int keyRngRetainAll(int offset, int length, XGettingCollection<? extends K> elements);



	///////////////////////////////////////////////////////////////////////////
	//   processing     //
	/////////////////////

	public int keyProcess(Consumer<? super K> procedure);

	public int keyRngProcess(int offset, int length, Consumer<? super K> procedure);



	///////////////////////////////////////////////////////////////////////////
	//     Moving       //
	/////////////////////

	public int keyMoveRange(int offset, int length, Consumer<? super K> target);

	public int keyMoveSelection(Consumer<? super K> target, long... indices);

	public int keyMoveTo(Consumer<? super K> target, Predicate<? super K> predicate);

	public int keyRngMoveTo(int offset, int length, Consumer<? super K> target, Predicate<? super K> predicate);



	///////////////////////////////////////////////////////////////////////////
	//     sorting       //
	//////////////////////

	public void keySort(Comparator<? super K> keyComparator);

	public void keyRngSort(int offset, int length, Comparator<? super K> keyComparator);

	public boolean keyIsSorted(Comparator<? super K> keyComparator);

	public boolean keyRngIsSorted(int offset, int length, Comparator<? super K> keyComparator);



	///////////////////////////////////////////////////////////////////////////
	//     setting      //
	/////////////////////

	@SuppressWarnings("unchecked")
	public void keySet(int offset, K... elements);

	public void keySet(int offset, K[] elements, int elementsOffset, int elementsLength);

	public void keyFill(int offset, int length, K element);



	///////////////////////////////////////////////////////////////////////////
	//    replacing     //
	/////////////////////

	public int keyReplaceOne(K element, K replacement);

	public int keyRngReplaceOne(int offset, int length, K element, K replacement);

	public int keyReplace(K element, K replacement);

	public int keyRngReplace(int offset, int length, K element, K replacement);

	public int keyRngReplace(int offset, int length, K sample, Equalator<? super K> equalator, K replacement);

	public int keyReplaceAll(K[] elements, int elementsOffset, int elementsLength, K replacement);

	public int keyRngReplaceAll(int offset, int length, K[] elements, int elementsOffset, int elementsLength, K replacement);

	public int keyReplaceAll(XGettingCollection<? extends K> elements, K replacement);

	public int keyRngReplaceAll(int offset, int length, XGettingCollection<? extends K> elements, K replacement);

	public int keyModify(Function<K, K> mapper);

	public int keyModify(Predicate<? super K> predicate, Function<K, K> mapper);

	public int keyRngModify(int offset, int length, Function<K, K> mapper);

	public int keyRngModify(int offset, int length, Predicate<? super K> predicate, Function<K, K> mapper);

	public int keySubstituteOne(Predicate<? super K> predicate, K keySubstitute);

	public int keyRngSubstituteOne(int offset, int length, Predicate<? super K> predicate, K keySubstitute);

	public int keySubstitute(Predicate<? super K> predicate, K keySubstitute);

	public int keyRngSubstitute(int offset, int length, Predicate<? super K> predicate, K keySubstitute);

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
	//   containing     //
	/////////////////////

	// containing - null //

	public boolean valuesContainsNull();

	public boolean valuesRngContainsNull(final int offset, int length);

	// containing - identity //

	public boolean valuesContainsId(V element);

	public boolean valuesRngContainsId(int offset, int length, V value);

	// containing - logical //

	public boolean valuesContains(V element);

	public boolean valuesContains(V sample, Equalator<? super V> equalator);

	public boolean valuesRngContains(int offset, int length, V value);

	public boolean valuesRngContains(int offset, int length, V sample, Equalator<? super V> equalator);

	// containing - all array //

	public boolean valuesContainsAll(V[] values, int elementsOffset, int elementsLength);

	public boolean valuesContainsAll(V[] values, int elementsOffset, int elementsLength, Equalator<? super V> equalator);

	public boolean valuesRngContainsAll(
		int offset,
		int length,
		V[] values,
		int elementsOffset,
		int elementsLength
	);

	public boolean valuesRngContainsAll(
		int offset,
		int length,
		V[] values,
		int elementsOffset,
		int elementsLength,
		Equalator<? super V> equalator
	);

	// containing - all collection //

	public boolean valuesContainsAll(XGettingCollection<? extends V> elements);

	public boolean valuesContainsAll(XGettingCollection<? extends V> elements, Equalator<? super V> equalator);

	public boolean valuesRngContainsAll(int offset, int length, XGettingCollection<? extends V> elements);

	public boolean valuesRngContainsAll(
		int offset,
		int length,
		XGettingCollection<? extends V> samples,
		Equalator<? super V> equalator
	);



	///////////////////////////////////////////////////////////////////////////
	//    applying      //
	/////////////////////

	// applying - single //

	public boolean valuesApplies(Predicate<? super V> predicate);

	public boolean valuesRngApplies(int offset, int length, Predicate<? super V> predicate);

	// applying - all //

	public boolean valuesAppliesAll(Predicate<? super V> predicate);

	public boolean valuesRngAppliesAll(int offset, int length, Predicate<? super V> predicate);



	///////////////////////////////////////////////////////////////////////////
	//    counting      //
	/////////////////////

	// counting - element //

	public int valuesCount(V element);

	public int valuesCount(V sample, Equalator<? super V> equalator);

	public int valuesRngCount(int offset, int length, V value);

	public int valuesRngCount(int offset, int length, V sample, Equalator<? super V> equalator);

	// counting - predicate //

	public int valuesCount(Predicate<? super V> predicate);

	public int valuesRngCount(int offset, int length, Predicate<? super V> predicate);



	///////////////////////////////////////////////////////////////////////////
	// data arithmetic  //
	/////////////////////

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

	public <C extends Consumer<? super V>> C valuesRngIntersect(
		int offset,
		int length,
		XGettingCollection<? extends V> collection,
		Equalator<? super V> equalator,
		C target
	);

	public <C extends Consumer<? super V>> C valuesRngExcept(
		int offset,
		int length,
		XGettingCollection<? extends V> collection,
		Equalator<? super V> equalator,
		C target
	);

	public <C extends Consumer<? super V>> C valuesRngUnion(
		int offset,
		int length,
		XGettingCollection<? extends V> collection,
		Equalator<? super V> equalator,
		C target
	);

	// data - copying //

	public <C extends Consumer<? super V>> C valuesCopyTo(C target);

	public <C extends Consumer<? super V>> C valuesRngCopyTo(int offset, int length, C target);

	public <C extends Consumer<? super V>> C valuesCopySelection(C target, long... indices);

	public int valuesCopyToArray(long offset, int length, Object[] target, int targetOffset);

	// data - conditional copying //

	public <C extends Consumer<? super V>> C valuesCopyTo(C target, Predicate<? super V> predicate);

	public <C extends Consumer<? super V>> C valuesRngCopyTo(
		int offset,
		int length,
		C target,
		Predicate<? super V> predicate
	);

	// data - array transformation //

	public Object[] valuesToArray();

	public      V[] valuesToArray(Class<V> type);

	public Object[] valuesRngToArray(int offset, int length);

	public      V[] valuesRngToArray(int offset, int length, Class<V> type);



	///////////////////////////////////////////////////////////////////////////
	//    querying      //
	/////////////////////

	public V valuesFirst();

	public V valuesLast();

	public V valuesGet(long index);



	///////////////////////////////////////////////////////////////////////////
	//    searching     //
	/////////////////////

	// searching - sample //

	public V valuesGet(V sample);

	public V valuesRngGet(int offset, int length, V sample);

	public V valuesSearch(V sample, Equalator<? super V> equalator);

	public V valuesRngSearch(int offset, int length, V sample, Equalator<? super V> equalator);

	// searching - predicate //

	public V valuesSearch(Predicate<? super V> predicate);

	public V valuesRngSearch(int offset, int length, Predicate<? super V> predicate);

	// searching - min max //

	public V valuesMin(Comparator<? super V> comparator);

	public V valuesMax(Comparator<? super V> comparator);

	public V valuesRngMin(int offset, int length, Comparator<? super V> comparator);

	public V valuesRngMax(int offset, int length, Comparator<? super V> comparator);



	///////////////////////////////////////////////////////////////////////////
	//    executing     //
	/////////////////////

	// executing - procedure //

	public void valuesIterate(Consumer<? super V> procedure);

	public void valuesRngIterate(int offset, int length, Consumer<? super V> procedure);

	// executing - indexed procedure //

	public void valuesIterate(IndexProcedure<? super V> procedure);

	public void valuesRngIterate(int offset, int length, IndexProcedure<? super V> procedure);

	public <A> void valuesJoin(BiProcedure<? super V, A> joiner, A aggregate);

	// executing - conditional //

	public void valuesIterate(Predicate<? super V> predicate, Consumer<? super V> procedure);



	///////////////////////////////////////////////////////////////////////////
	//    indexing      //
	/////////////////////

	// indexing - single //

	public int valuesIndexOf(V element);

	public int valuesIndexOf(V sample, Equalator<? super V> equalator);

	public int valuesRngIndexOf(int offset, int length, V value);

	public int valuesRngIndexOf(int offset, int length, V sample, Equalator<? super V> equalator);

	// indexing - predicate //

	public int valuesIndexOf(Predicate<? super V> predicate);

	public int valuesRngIndexOf(int offset, int length, Predicate<? super V> predicate);

	// indexing - min max //

	public int valuesMinIndex(Comparator<? super V> comparator);

	public int valuesMaxIndex(Comparator<? super V> comparator);

	public int valuesRngMinIndex(int offset, int length, Comparator<? super V> comparator);

	public int valuesRngMaxIndex(int offset, int length, Comparator<? super V> comparator);

	// indexing - scan //

	public int valuesScan(Predicate<? super V> predicate);

	public int valuesRngScan(int offset, int length, Predicate<? super V> predicate);



	///////////////////////////////////////////////////////////////////////////
	//   distinction    //
	/////////////////////

	// distinction querying //

	public boolean valuesHasDistinctValues();

	public boolean valuesHasDistinctValues(Equalator<? super V> equalator);

	// distinction copying //

	public <C extends Consumer<? super V>> C valuesDistinct(C target);

	public <C extends Consumer<? super V>> C valuesDistinct(C target, Equalator<? super V> equalator);

	public <C extends Consumer<? super V>> C valuesRngDistinct(int offset, int length, C target);

	public <C extends Consumer<? super V>> C valuesRngDistinct(
		int offset,
		int length,
		C target,
		Equalator<? super V> equalator
	);



	///////////////////////////////////////////////////////////////////////////
	// VarString appending //
	//////////////////////

	public VarString valuesAppendTo(VarString vc);

	public VarString valuesAppendTo(VarString vc, char separator);

	public VarString valuesAppendTo(VarString vc, String separator);

	public VarString valuesAppendTo(VarString vc, BiProcedure<VarString, ? super V> appender);

	public VarString valuesAppendTo(VarString vc, BiProcedure<VarString, ? super V> appender, char separator);

	public VarString valuesAppendTo(VarString vc, BiProcedure<VarString, ? super V> appender, String separator);

	public VarString valuesRngAppendTo(int offset, int length, VarString vc);

	public VarString valuesRngAppendTo(int offset, int length, VarString vc, char separator);

	public VarString valuesRngAppendTo(int offset, int length, VarString vc, String separator);

	public VarString valuesRngAppendTo(int offset, int length, VarString vc, BiProcedure<VarString, ? super V> appender);

	public VarString valuesRngAppendTo(int offset, int length, VarString vc, BiProcedure<VarString, ? super V> appender, char separator);

	public VarString valuesRngAppendTo(int offset, int length, VarString vc, BiProcedure<VarString, ? super V> appender, String separator);

	public String valuesToString();



	///////////////////////////////////////////////////////////////////////////
	//    removing      //
	/////////////////////

	// removing - indexed //

	public V valuesRemove(final long index);

	// removing - null //

	public int valuesRemoveNull();

	public int valuesRngRemoveNull(int offset, int length);

	// removing - one single //

	public V valuesRetrieve(V element);

	public V valuesRetrieve(Predicate<? super V> predicate);

	public V valuesRngRetrieve(int offset, int length, V value);

	public V valuesRngRetrieve(int offset, int length, V sample, Equalator<? super V> equalator);

	public boolean valuesRemoveOne(V element);

	public boolean valuesRemoveOne(V sample, Equalator<? super V> equalator);

	public boolean valuesRngRemoveOne(int offset, int length, V value);

	public boolean valuesRngRemoveOne(int offset, int length, V sample, Equalator<? super V> equalator);

	// removing - multiple single //

	public int valuesRemove(V element);

	public int valuesRemove(V sample, Equalator<? super V> equalator);

	public int valuesRngRemove(int offset, int length, V value);

	public int valuesRngRemove(int offset, int length, V sample, Equalator<? super V> equalator);

	// removing - multiple all array //

	public int valuesRemoveAll(V[] values, int elementsOffset, int elementsLength);

	public int valuesRemoveAll(V[] samples, int samplesOffset, int samplesLength, Equalator<? super V> equalator);

	public int valuesRngRemoveAll(int offset, int length, V[] values, int elementsOffset, int elementsLength);

	public int valuesRngRemoveAll(int offset, int length, V[] samples, int samplesOffset, int samplesLength, Equalator<? super V> equalator);

	// removing - multiple all collection //

	public int valuesRemoveAll(XGettingCollection<? extends V> elements);

	public int valuesRemoveAll(XGettingCollection<? extends V> samples, Equalator<? super V> equalator);

	public int valuesRngRemoveAll(int offset, int length, XGettingCollection<? extends V> elements);

	public int valuesRngRemoveAll(int offset, int length, XGettingCollection<? extends V> samples, Equalator<? super V> equalator);

	// removing - duplicates //

	public int valuesRemoveDuplicates();

	public int valuesRemoveDuplicates(Equalator<? super V> equalator);

	public int valuesRngRemoveDuplicates(int offset, int length);

	public int valuesRngRemoveDuplicates(int offset, int length, Equalator<? super V> equalator);



	///////////////////////////////////////////////////////////////////////////
	//     reducing     //
	/////////////////////

	// reducing - predicate //

	public int valuesReduce(Predicate<? super V> predicate);

	public int valuesRngReduce(int offset, int length, Predicate<? super V> predicate);



	///////////////////////////////////////////////////////////////////////////
	//    retaining     //
	/////////////////////

	// retaining - array //

	public int valuesRetainAll(V[] values, int elementsOffset, int elementsLength);

	public int valuesRetainAll(V[] samples, int elementsOffset, int elementsLength, Equalator<? super V> equalator);

	public int valuesRngRetainAll(int offset, int length, V[] values, int elementsOffset, int elementsLength);

	public int valuesRngRetainAll(
		int offset,
		int length,
		V[] samples,
		int elementsOffset,
		int elementsLength,
		Equalator<? super V> equalator
	);

	// retaining - collection //

	public int valuesRetainAll(XGettingCollection<? extends V> elements);

	public int valuesRetainAll(XGettingCollection<? extends V> samples, Equalator<? super V> equalator);

	public int valuesRngRetainAll(int offset, int length, XGettingCollection<? extends V> elements);

	public int valuesRngRetainAll(int offset, int length, XGettingCollection<? extends V> samples, Equalator<? super V> equalator);



	///////////////////////////////////////////////////////////////////////////
	//   processing     //
	/////////////////////

	public int valuesProcess(Consumer<? super V> procedure);

	public int valuesRngProcess(int offset, int length, Consumer<? super V> procedure);



	///////////////////////////////////////////////////////////////////////////
	//     Moving       //
	/////////////////////

	public int valuesMoveRange(int offset, int length, Consumer<? super V> target);

	public int valuesMoveSelection(Consumer<? super V> target, long... indices);

	// moving - conditional //

	public int valuesMoveTo(Consumer<? super V> target, Predicate<? super V> predicate);

	public int valuesRngMoveTo(int offset, int length, Consumer<? super V> target, Predicate<? super V> predicate);



	///////////////////////////////////////////////////////////////////////////
	//     sorting       //
	//////////////////////

	public void valuesSort(Comparator<? super V> comparator);

	public void valuesRngSort(int offset, int length, Comparator<? super V> comparator);

	public boolean valuesIsSorted(Comparator<? super V> comparator);

	public boolean valuesRngIsSorted(int offset, int length, Comparator<? super V> comparator);



	///////////////////////////////////////////////////////////////////////////
	//     setting      //
	/////////////////////

	public V valuesSet(long offset, V value);

	public void valuesSet(long offset, V[] values);

	public void valuesSet(long offset, V[] values, int valuesOffset, int valuesLength);

	public void valuesFill(long offset, long length, V value);



	///////////////////////////////////////////////////////////////////////////
	//    replacing     //
	/////////////////////

	// replacing - one single //

	public boolean valuesReplaceOne(V element, V replacement);

	public boolean valuesReplaceOne(V sample, Equalator<? super V> equalator, V replacement);

	public int valuesRngReplaceOne(int offset, int length, V value, V replacement);

	public int valuesRngReplaceOne(int offset, int length, V sample, Equalator<? super V> equalator, V replacement);

	// replacing - multiple single //

	public int valuesReplace(V element, V replacement);

	public int valuesReplace(V sample, Equalator<? super V> equalator, V replacement);

	public int valuesRngReplace(int offset, int length, V value, V replacement);

	public int valuesRngReplace(int offset, int length, V sample, Equalator<? super V> equalator, V replacement);

	// replacing - multiple all array //

	public int valuesReplaceAll(V[] values, int valuesOffset, int valuesLength, V replacement);

	public int valuesReplaceAll(
		V[] samples,
		int samplesOffset,
		int samplesLength,
		Equalator<? super V> equalator,
		V replacement
	);

	public int valuesRngReplaceAll(int offset, int length, V[] values, int valuesOffset, int valuesLength, V replacement);

	public int valuesRngReplaceAll(
		int offset,
		int length,
		V[] samples,
		int samplesOffset,
		int samplesLength,
		Equalator<? super V> equalator,
		V replacement
	);

	// replacing - multiple all collection //

	public int valuesReplaceAll(XGettingCollection<? extends V> elements, V replacement);

	public int valuesReplaceAll(XGettingCollection<? extends V> samples, Equalator<? super V> equalator, V replacement);

	public int valuesRngReplaceAll(int offset, int length, XGettingCollection<? extends V> elements, V replacement);

	public int valuesRngReplaceAll(
		int offset,
		int length,
		XGettingCollection<? extends V> samples,
		Equalator<? super V> equalator,
		V replacement
	);

	// replacing - mapped //

	public int valuesModify(Function<V, V> mapper);

	public int valuesModify(Predicate<? super V> predicate, Function<V, V> mapper);

	public int valuesRngModify(int offset, int length, Function<V, V> mapper);

	public int valuesRngModify(int offset, int length, Predicate<? super V> predicate, Function<V, V> mapper);



	///////////////////////////////////////////////////////////////////////////
	//  substituting    //
	/////////////////////

	// substituting - one //

	public boolean valuesSubstituteOne(Predicate<? super V> predicate, V substitute);

	public int valuesRngSubstituteOne(int offset, int length, Predicate<? super V> predicate, V substitute);

	// substituting - multiple //

	public int valuesSubstitute(Predicate<? super V> predicate, V substitute);

	public int valuesRngSubstitute(int offset, int length, Predicate<? super V> predicate, V substitute);



	///////////////////////////////////////////////////////////////////////////
	//     swapping     //
	/////////////////////

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
	public void rngReverse(long offset, long length);

	@Override
	public void shuffle();

	@Override
	public void rngShuffle(long offset, long length);

}
