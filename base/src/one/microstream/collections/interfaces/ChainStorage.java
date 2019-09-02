package one.microstream.collections.interfaces;

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



// (21.09.2013)FIXME: why are the entries no interfaces on the interface level?
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

	// containing - null //

	public boolean containsNull();

	public boolean rngContainsNull(final long offset, long length);

	// containing - identity //

	public boolean containsId(E element);

	public boolean rngContainsId(long offset, long length, E element);

	// containing - logical //

	public boolean contains(E element);

	public boolean contains(E sample, Equalator<? super E> equalator);

	public boolean rngContains(long offset, long length, E element);

	// containing - all array //

	public boolean containsAll(E[] elements, int elementsOffset, int elementsLength);

	public boolean rngContainsAll(
		long offset,
		long length,
		E[] elements,
		int elementsOffset,
		int elementsLength
	);

	// containing - all collection //

	public boolean containsAll(XGettingCollection<? extends E> elements);

	public boolean rngContainsAll(long offset, long length, XGettingCollection<? extends E> elements);



	///////////////////////////////////////////////////////////////////////////
	// applying //
	/////////////

	// applying - single //

	public boolean containsSearched(Predicate<? super E> predicate);

	public boolean rngContainsSearched(long offset, long length, Predicate<? super E> predicate);

	// applying - all //

	public boolean appliesAll(Predicate<? super E> predicate);

	public boolean rngAppliesAll(long offset, long length, Predicate<? super E> predicate);



	///////////////////////////////////////////////////////////////////////////
	// counting //
	/////////////

	// counting - element //

	public long count(E element);

	public long count(E sample, Equalator<? super E> equalator);

	public long rngCount(long offset, long length, E element);

	// counting - predicate //

	public long count(Predicate<? super E> predicate);

	public long rngCount(long offset, long length, Predicate<? super E> predicate);



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

	public <C extends Consumer<? super E>> C rngIntersect(
		long offset,
		long length,
		XGettingCollection<? extends E> samples,
		Equalator<? super E> equalator,
		C target
	);

	public <C extends Consumer<? super E>> C rngExcept(
		long offset,
		long length,
		XGettingCollection<? extends E> samples,
		Equalator<? super E> equalator,
		C target
	);

	public <C extends Consumer<? super E>> C rngUnion(
		long offset,
		long length,
		XGettingCollection<? extends E> samples,
		Equalator<? super E> equalator,
		C target
	);

	// data - copying //

	public <C extends Consumer<? super E>> C copyTo(C target);

	public <C extends Consumer<? super E>> C rngCopyTo(long offset, long length, C target);

	public <C extends Consumer<? super E>> C copySelection(C target, long[] indices);

	public int copyToArray(long offset, int length, Object[] target, int targetOffset);

	// data - conditional copying //

	public <C extends Consumer<? super E>> C copyTo(C target, Predicate<? super E> predicate);

	public <C extends Consumer<? super E>> C rngCopyTo(
		long offset,
		long length,
		C target,
		Predicate<? super E> predicate
	);

	// data - array transformation //

	public Object[] toArray();

	public      E[] toArray(Class<E> type);

	public Object[] rngToArray(long offset, int length);

	public      E[] rngToArray(long offset, int length, Class<E> type);



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

	public E rngSearch(long offset, long length, Predicate<? super E> predicate);

	// searching - min max //

	public E min(Comparator<? super E> comparator);

	public E max(Comparator<? super E> comparator);

	public E rngMin(long offset, long length, Comparator<? super E> comparator);

	public E rngMax(long offset, long length, Comparator<? super E> comparator);



	///////////////////////////////////////////////////////////////////////////
	// executing //
	//////////////

	// executing - procedure //

	public void iterate(Consumer<? super E> procedure);

	public <A> void join(BiConsumer<? super E, A> joiner, A aggregate);

	public void rngIterate(long offset, long length, Consumer<? super E> procedure);

	// executing - indexed procedure //

	public void iterateIndexed(IndexedAcceptor<? super E> procedure);

	public void rngIterateIndexed(long offset, long length, IndexedAcceptor<? super E> procedure);

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

	public long rngIndexOf(long offset, long length, E element);

	public long rngIndexOf(long offset, long length, E sample, Equalator<? super E> equalator);

	// indexing - predicate //

	public long indexOf(Predicate<? super E> predicate);

	public long rngIndexOf(long offset, long length, Predicate<? super E> predicate);

	// indexing - min max //

	public long minIndex(Comparator<? super E> comparator);

	public long maxIndex(Comparator<? super E> comparator);

	public long rngMinIndex(long offset, long length, Comparator<? super E> comparator);

	public long rngMaxIndex(long offset, long length, Comparator<? super E> comparator);

	// indexing - scan //

	public long scan(Predicate<? super E> predicate);

	public long rngScan(long offset, long length, Predicate<? super E> predicate);



	///////////////////////////////////////////////////////////////////////////
	// distinction //
	////////////////

	// distinction querying //

	public boolean hasDistinctValues();

	public boolean hasDistinctValues(Equalator<? super E> equalator);

	// distinction copying //

	public <C extends Consumer<? super E>> C distinct(C target);

	public <C extends Consumer<? super E>> C distinct(C target, Equalator<? super E> equalator);

	public <C extends Consumer<? super E>> C rngDistinct(long offset, long length, C target);

	public <C extends Consumer<? super E>> C rngDistinct(
		long offset,
		long length,
		C target,
		Equalator<? super E> equalator
	);



	///////////////////////////////////////////////////////////////////////////
	// VarString appending //
	//////////////////////

	public VarString appendTo(VarString vc);

	public VarString appendTo(VarString vc, char separator);

	public VarString appendTo(VarString vc, String separator);

	public VarString appendTo(VarString vc, BiConsumer<VarString, ? super E> appender);

	public VarString appendTo(VarString vc, BiConsumer<VarString, ? super E> appender, char separator);

	public VarString appendTo(VarString vc, BiConsumer<VarString, ? super E> appender, String separator);

	public VarString rngAppendTo(long offset, long length, VarString vc);

	public VarString rngAppendTo(long offset, long length, VarString vc, char separator);

	public VarString rngAppendTo(long offset, long length, VarString vc, String separator);

	public VarString rngAppendTo(long offset, long length, VarString vc, BiConsumer<VarString, ? super E> appender);

	public VarString rngAppendTo(long offset, long length, VarString vc, BiConsumer<VarString, ? super E> appender, char separator);

	public VarString rngAppendTo(long offset, long length, VarString vc, BiConsumer<VarString, ? super E> appender, String separator);

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
	 * Removes all entries at the indices (offsets) given in the passed {@code int} array.
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

	public long rngRemoveNull(long offset, long length);

	// removing - one single //

	public E retrieve(E element);

	public E retrieve(E sample, Equalator<? super E> equalator);

	public E retrieve(Predicate<? super E> predicate);

	public E rngRetrieve(long offset, long length, E element);

	public E rngRetrieve(long offset, long length, E sample, Equalator<? super E> equalator);

	public boolean removeOne(E element);

	public boolean removeOne(E sample, Equalator<? super E> equalator);

	// removing - multiple single //

	public long remove(E element);

	public long remove(E sample, Equalator<? super E> equalator);

	public long rngRemove(long offset, long length, E element);

	// removing - multiple all array //

	public long removeAll(E[] elements, int elementsOffset, int elementsLength);

	public long rngRemoveAll(long offset, long length, E[] elements, int elementsOffset, int elementsLength);

	// removing - multiple all collection //

	public long removeAll(XGettingCollection<? extends E> elements);

	public long rngRemoveAll(long offset, long length, XGettingCollection<? extends E> elements);

	// removing - duplicates //

	public long removeDuplicates();

	public long removeDuplicates(Equalator<? super E> equalator);

	public long rngRemoveDuplicates(long offset, long length);

	public long rngRemoveDuplicates(long offset, long length, Equalator<? super E> equalator);



	///////////////////////////////////////////////////////////////////////////
	// reducing //
	/////////////

	// reducing - predicate //

	public long reduce(Predicate<? super E> predicate);

	public long rngReduce(long offset, long length, Predicate<? super E> predicate);



	///////////////////////////////////////////////////////////////////////////
	// retaining //
	//////////////

	// retaining - array //

	public long retainAll(E[] elements, int elementsOffset, int elementsLength);

	public long rngRetainAll(long offset, long length, E[] elements, int elementsOffset, int elementsLength);

	// retaining - collection //

	public long retainAll(XGettingCollection<? extends E> elements);

	public long retainAll(XGettingCollection<? extends E> samples, Equalator<? super E> equalator);

	public long rngRetainAll(long offset, long length, XGettingCollection<? extends E> elements);



	///////////////////////////////////////////////////////////////////////////
	// processing //
	///////////////

	public long process(Consumer<? super E> procedure);

	public long rngProcess(long offset, long length, Consumer<? super E> procedure);



	///////////////////////////////////////////////////////////////////////////
	// moving //
	///////////

	public long moveRange(long offset, long length, Consumer<? super E> target);

	public long moveSelection(Consumer<? super E> target, long... indices);

	// moving - conditional //

	public long moveTo(Consumer<? super E> target, Predicate<? super E> predicate);

	public long rngMoveTo(long offset, long length, Consumer<? super E> target, Predicate<? super E> predicate);



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

	public void rngReverse(long offset, long length);



	///////////////////////////////////////////////////////////////////////////
	// sorting //
	////////////

	public void sort(Comparator<? super E> comparator);

	public void rngSort(long offset, long length, Comparator<? super E> comparator);

	public boolean isSorted(Comparator<? super E> comparator);

	public boolean rngIsSorted(long offset, long length, Comparator<? super E> comparator);

	public void shuffle();

	public void rngShuffle(long offset, long length);



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

	public long rngReplaceOne(long offset, long length, E element, E replacement);

	// replacing - multiple single //

	public long replace(E element, E replacement);

	public long rngReplace(long offset, long length, E element, E replacement);

	public long rngReplace(long offset, long length, E sample, Equalator<? super E> equalator, E replacement);

	// replacing - multiple all array //

	public long replaceAll(E[] elements, int elementsOffset, int elementsLength, E replacement);

	public long rngReplaceAll(long offset, long length, E[] elements, int elementsOffset, int elementsLength, E replacement);

	// replacing - multiple all collection //

	public long replaceAll(XGettingCollection<? extends E> elements, E replacement);

	public long rngReplaceAll(long offset, long length, XGettingCollection<? extends E> elements, E replacement);

	// replacing - mapped //

	public long substitute(Function<E, E> mapper);

	public long substitute(Predicate<? super E> predicate, Function<E, E> mapper);

	public long rngSubstitute(long offset, long length, Function<E, E> mapper);

	public long rngSubstitute(long offset, long length, Predicate<? super E> predicate, Function<E, E> mapper);



	///////////////////////////////////////////////////////////////////////////
	// substituting //
	/////////////////

	// substituting - one //

	public long replaceOneBy(Predicate<? super E> predicate, E substitute);

	public long rngReplaceOneBy(long offset, long length, Predicate<? super E> predicate, E substitute);

	// substituting - multiple //

	public long replaceBy(Predicate<? super E> predicate, E substitute);

	public long rngReplaceOne(long offset, long length, Predicate<? super E> predicate, E substitute);

}
