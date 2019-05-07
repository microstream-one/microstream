package one.microstream.collections;

import java.util.Comparator;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import one.microstream.collections.ListView;
import one.microstream.collections.SubListView;
import one.microstream.collections.interfaces.CapacityCarrying;
import one.microstream.collections.interfaces.CapacityExtendable;
import one.microstream.collections.interfaces.ConsolidatableCollection;
import one.microstream.collections.interfaces.ExtendedList;
import one.microstream.collections.interfaces.OptimizableCollection;
import one.microstream.collections.interfaces.Sized;
import one.microstream.collections.interfaces.Truncateable;
import one.microstream.collections.old.AbstractBridgeXList;
import one.microstream.collections.types.XAddingList;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XGettingList;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.collections.types.XImmutableList;
import one.microstream.collections.types.XInputtingList;
import one.microstream.collections.types.XInsertingList;
import one.microstream.collections.types.XJoinable;
import one.microstream.collections.types.XList;
import one.microstream.collections.types.XPrependingList;
import one.microstream.collections.types.XPreputtingSequence;
import one.microstream.collections.types.XProcessingCollection;
import one.microstream.collections.types.XProcessingList;
import one.microstream.collections.types.XProcessingSequence;
import one.microstream.collections.types.XPuttingList;
import one.microstream.collections.types.XRemovingCollection;
import one.microstream.collections.types.XSettingList;
import one.microstream.collections.types.XSortableSequence;
import one.microstream.equality.Equalator;
import one.microstream.functional.IndexedAcceptor;
import one.microstream.typing.Clearable;


/**
 * This wrapper list "type-upgrades" {@link ExtendedList} instances that implement only one or more partial
 * functionality types (like {@link XGettingList}, {@link XAddingList}, etc.) to the full-scale type
 * {@link XList}.
 * <p>
 * All method calls to an instance of this class will be relayed to the wrapped partial functionality type.
 * If a method is called that is not supported by the wrapped class, an {@link UnsupportedOperationException} will be
 * thrown.
 * <p>
 * Note that this class is meant more as a workaround tool than a proper architectural means.<br>
 * When designing an application or library, the primary target should be to properly identify the concerns and use
 * appropriate collection types for them.<br>
 * Still, there can be exceptional situations (like legacy or API code that cannot be adjusted or corner cases that
 * do not justify to upgrade actual types) where such a workaround wrapper can be an architectural reasonable means.<br>
 * That being said for the sake of good architectural design, use this class freely as you will.
 *
 * @author Thomas Muenz
 *
 */
public final class DownwrapList<E> implements XList<E>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final ExtendedList<E> subject;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Creates a new {@link DownwrapList} instance wrapping the passed {@link ExtendedList} instance to be usable as an
	 * {@link XList}.<br>
	 * The passed list must at least implement one of the following interfaces:
	 * <ul>
	 * <li>{@link XGettingList}</li>
	 * <li>{@link XAddingList}</li>
	 * <li>{@link XSettingList}</li>
	 * <li>{@link XInsertingList}</li>
	 * <li>{@link XProcessingList}</li>
	 * </ul>
	 * If none of these interfaces is implemented, an {@link IllegalArgumentException} is thrown.
	 *
	 * @param list the partial functionality list implementation that shall be "upgraded" to {@link XList}.
	 *
	 * @throws IllegalArgumentException
	 */
	public DownwrapList(final ExtendedList<E> list) throws IllegalArgumentException
	{
		super();
		this.subject = list;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	/**
	 * Returns the {@link ExtendedList} instance that has been passed to the constructor when creating this
	 * {@link DownwrapList} instance.
	 *
	 * @return the wrapped {@link ExtendedList} instance.
	 */
	public ExtendedList<E> getWrappedList()
	{
		return this.subject;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@SuppressWarnings("unchecked")
	@Override
	public Equalator<? super E> equality()
	{
		if(this.subject instanceof XGettingCollection<?>)
		{
			return ((XGettingCollection<E>)this.subject).equality();
		}
		
		throw new UnsupportedOperationException();
	}



	///////////////////////////////////////////////////////////////////////////
	// adding //
	///////////

	@SuppressWarnings("unchecked")
	@Override
	public void accept(final E element)
	{
		if(this.subject instanceof Consumer<?>)
		{
			((Consumer<E>)this.subject).accept(element);
		}
		
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean add(final E element)
	{
		if(this.subject instanceof XAddingList<?>)
		{
			return ((XAddingList<E>)this.subject).add(element);
		}
		
		throw new UnsupportedOperationException();
	}

	@SafeVarargs
	@Override
	public final DownwrapList<E> addAll(final E... elements)
	{
		if(this.subject instanceof XAddingList<?>)
		{
			((XAddingList<E>)this.subject).addAll(elements);
			return this;
		}
		
		throw new UnsupportedOperationException();
	}

	@Override
	public DownwrapList<E> addAll(final XGettingCollection<? extends E> elements)
	{
		if(this.subject instanceof XAddingList<?>)
		{
			((XAddingList<E>)this.subject).addAll(elements);
			return this;
		}
		
		throw new UnsupportedOperationException();
	}

	@Override
	public DownwrapList<E> addAll(final E[] elements, final int offset, final int length)
	{
		if(this.subject instanceof XAddingList<?>)
		{
			((XAddingList<E>)this.subject).addAll(elements, offset, length);
			return this;
		}
		
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean nullAdd()
	{
		if(this.subject instanceof XAddingList<?>)
		{
			return ((XAddingList<E>)this.subject).nullAdd();
		}
		
		throw new UnsupportedOperationException();
	}



	///////////////////////////////////////////////////////////////////////////
	// putting //
	////////////

	@Override
	public boolean put(final E e)
	{
		if(this.subject instanceof XPuttingList<?>)
		{
			return ((XPuttingList<E>)this.subject).put(e);
		}
		
		throw new UnsupportedOperationException();
	}

	@SafeVarargs
	@Override
	public final DownwrapList<E> putAll(final E... elements)
	{
		if(this.subject instanceof XPuttingList<?>)
		{
			((XPuttingList<E>)this.subject).putAll(elements);
			return this;
		}
		
		throw new UnsupportedOperationException();
	}

	@Override
	public DownwrapList<E> putAll(final XGettingCollection<? extends E> elements)
	{
		if(this.subject instanceof XPuttingList<?>)
		{
			((XPuttingList<E>)this.subject).putAll(elements);
			return this;
		}
		
		throw new UnsupportedOperationException();
	}

	@Override
	public DownwrapList<E> putAll(final E[] elements, final int offset, final int length)
	{
		if(this.subject instanceof XPuttingList<?>)
		{
			((XPuttingList<E>)this.subject).putAll(elements, offset, length);
			return this;
		}
		
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean nullPut()
	{
		if(this.subject instanceof XPuttingList<?>)
		{
			return ((XPuttingList<E>)this.subject).nullPut();
		}
		
		throw new UnsupportedOperationException();
	}



	///////////////////////////////////////////////////////////////////////////
	// prepending //
	///////////////

	@Override
	public boolean prepend(final E element)
	{
		if(this.subject instanceof XPrependingList<?>)
		{
			return ((XPrependingList<E>)this.subject).prepend(element);
		}
		
		throw new UnsupportedOperationException();
	}

	@SafeVarargs
	@Override
	public final DownwrapList<E> prependAll(final E... elements)
	{
		if(this.subject instanceof XPrependingList<?>)
		{
			((XPrependingList<E>)this.subject).prependAll(elements);
			return this;
		}
		
		throw new UnsupportedOperationException();
	}

	@Override
	public DownwrapList<E> prependAll(final E[] elements, final int offset, final int length)
	{
		if(this.subject instanceof XPrependingList<?>)
		{
			((XPrependingList<E>)this.subject).prependAll(elements, offset, length);
			return this;
		}
		
		throw new UnsupportedOperationException();
	}

	@Override
	public DownwrapList<E> prependAll(final XGettingCollection<? extends E> elements)
	{
		if(this.subject instanceof XPrependingList<?>)
		{
			((XPrependingList<E>)this.subject).prependAll(elements);
			return this;
		}
		
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean nullPrepend()
	{
		if(this.subject instanceof XPrependingList<?>)
		{
			return ((XPrependingList<E>)this.subject).nullPrepend();
		}
		
		throw new UnsupportedOperationException();
	}



	///////////////////////////////////////////////////////////////////////////
	// preputting //
	///////////////

	@SuppressWarnings("unchecked")
	@Override
	public boolean preput(final E element)
	{
		if(this.subject instanceof XPreputtingSequence<?>)
		{
			return ((XPreputtingSequence<E>)this.subject).preput(element);
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public DownwrapList<E> preputAll(final E... elements)
	{
		if(this.subject instanceof XPreputtingSequence<?>)
		{
			((XPreputtingSequence<E>)this.subject).preputAll(elements);
			return this;
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public DownwrapList<E> preputAll(final E[] elements, final int offset, final int length)
	{
		if(this.subject instanceof XPreputtingSequence<?>)
		{
			((XPreputtingSequence<E>)this.subject).preputAll(elements, offset, length);
			return this;
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public DownwrapList<E> preputAll(final XGettingCollection<? extends E> elements)
	{
		if(this.subject instanceof XPreputtingSequence<?>)
		{
			((XPreputtingSequence<E>)this.subject).preputAll(elements);
			return this;
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean nullPreput()
	{
		if(this.subject instanceof XPreputtingSequence<?>)
		{
			return ((XPreputtingSequence<E>)this.subject).nullPreput();
		}
		
		throw new UnsupportedOperationException();
	}



	///////////////////////////////////////////////////////////////////////////
	// inserting //
	//////////////

	@Override
	public boolean insert(final long index, final E element)
	{
		if(this.subject instanceof XInsertingList<?>)
		{
			return ((XInsertingList<E>)this.subject).insert(index, element);
		}
		
		throw new UnsupportedOperationException();
	}

	@SafeVarargs
	@Override
	public final long insertAll(final long index, final E... elements)
	{
		if(this.subject instanceof XInsertingList<?>)
		{
			return ((XInsertingList<E>)this.subject).insertAll(index, elements);
		}
		
		throw new UnsupportedOperationException();
	}

	@Override
	public long insertAll(final long index, final E[] elements, final int offset, final int length)
	{
		if(this.subject instanceof XInsertingList<?>)
		{
			return ((XInsertingList<E>)this.subject).insertAll(index, elements, offset, length);
		}
		
		throw new UnsupportedOperationException();
	}

	@Override
	public long insertAll(final long index, final XGettingCollection<? extends E> elements)
	{
		if(this.subject instanceof XInsertingList<?>)
		{
			return ((XInsertingList<E>)this.subject).insertAll(index, elements);
		}
		
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean nullInsert(final long index)
	{
		if(this.subject instanceof XInsertingList<?>)
		{
			return ((XInsertingList<E>)this.subject).nullInsert(index);
		}
		
		throw new UnsupportedOperationException();
	}



	///////////////////////////////////////////////////////////////////////////
	// inputting //
	//////////////

	@Override
	public boolean input(final long index, final E element)
	{
		if(this.subject instanceof XInputtingList<?>)
		{
			return ((XInputtingList<E>)this.subject).input(index, element);
		}
		
		throw new UnsupportedOperationException();
	}

	@SafeVarargs
	@Override
	public final long inputAll(final long index, final E... elements)
	{
		if(this.subject instanceof XInputtingList<?>)
		{
			return ((XInputtingList<E>)this.subject).inputAll(index, elements);
		}
		
		throw new UnsupportedOperationException();
	}

	@Override
	public long inputAll(final long index, final E[] elements, final int offset, final int length)
	{
		if(this.subject instanceof XInputtingList<?>)
		{
			return ((XInputtingList<E>)this.subject).inputAll(index, elements, offset, length);
		}
		
		throw new UnsupportedOperationException();
	}

	@Override
	public long inputAll(final long index, final XGettingCollection<? extends E> elements)
	{
		if(this.subject instanceof XInputtingList<?>)
		{
			return ((XInputtingList<E>)this.subject).inputAll(index, elements);
		}
		
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean nullInput(final long index)
	{
		if(this.subject instanceof XInputtingList<?>)
		{
			return ((XInputtingList<E>)this.subject).nullInput(index);
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean containsSearched(final Predicate<? super E> predicate)
	{
		if(this.subject instanceof XGettingCollection<?>)
		{
			return ((XGettingCollection<E>)this.subject).containsSearched(predicate);
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean applies(final Predicate<? super E> predicate)
	{
		if(this.subject instanceof XGettingCollection<?>)
		{
			return ((XGettingCollection<E>)this.subject).applies(predicate);
		}
		
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear()
	{
		if(this.subject instanceof Clearable)
		{
			((Clearable)this.subject).clear();
			return;
		}
		
		throw new UnsupportedOperationException();
	}

	@Override
	public long consolidate()
	{
		if(this.subject instanceof ConsolidatableCollection)
		{
			return ((ConsolidatableCollection)this.subject).consolidate();
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean contains(final E element)
	{
		if(this.subject instanceof XGettingCollection<?>)
		{
			return ((XGettingCollection<E>)this.subject).contains(element);
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean containsAll(final XGettingCollection<? extends E> elements)
	{
		if(this.subject instanceof XGettingCollection<?>)
		{
			return ((XGettingCollection<E>)this.subject).containsAll(elements);
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean containsId(final E element)
	{
		if(this.subject instanceof XGettingCollection<?>)
		{
			return ((XGettingCollection<E>)this.subject).containsId(element);
		}
		
		throw new UnsupportedOperationException();
	}

	@Override
	public DownwrapList<E> copy()
	{
		return new DownwrapList<>(this.subject);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <C extends Consumer<? super E>> C copySelection(final C target, final long... indices)
	{
		if(this.subject instanceof XGettingSequence<?>)
		{
			return ((XGettingSequence<E>)this.subject).copySelection(target, indices);
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <C extends Consumer<? super E>> C filterTo(final C target, final Predicate<? super E> predicate)
	{
		if(this.subject instanceof XGettingCollection<?>)
		{
			return ((XGettingCollection<E>)this.subject).filterTo(target, predicate);
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <C extends Consumer<? super E>> C copyTo(final C target)
	{
		if(this.subject instanceof XGettingCollection<?>)
		{
			return ((XGettingCollection<E>)this.subject).iterate(target);
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public long count(final E element)
	{
		if(this.subject instanceof XGettingCollection<?>)
		{
			return ((XGettingCollection<E>)this.subject).count(element);
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public long countBy(final Predicate<? super E> predicate)
	{
		if(this.subject instanceof XGettingCollection<?>)
		{
			return ((XGettingCollection<E>)this.subject).countBy(predicate);
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <C extends Consumer<? super E>> C distinct(final C target, final Equalator<? super E> equalator)
	{
		if(this.subject instanceof XGettingCollection<?>)
		{
			return ((XGettingCollection<E>)this.subject).distinct(target, equalator);
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <C extends Consumer<? super E>> C distinct(final C target)
	{
		if(this.subject instanceof XGettingCollection<?>)
		{
			return ((XGettingCollection<E>)this.subject).distinct(target);
		}
		
		throw new UnsupportedOperationException();
	}

	@Override
	public DownwrapList<E> ensureFreeCapacity(final long minimalFreeCapacity)
	{
		if(this.subject instanceof CapacityExtendable)
		{
			((CapacityExtendable)this.subject).ensureFreeCapacity(minimalFreeCapacity);
			return this;
		}
		
		throw new UnsupportedOperationException();
	}

	@Override
	public DownwrapList<E> ensureCapacity(final long minimalCapacity)
	{
		if(this.subject instanceof CapacityExtendable)
		{
			((CapacityExtendable)this.subject).ensureCapacity(minimalCapacity);
			return this;
		}
		
		throw new UnsupportedOperationException();
	}

	@Deprecated
	@Override
	public boolean equals(final Object o)
	{
		return this.subject.equals(o);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		if(this.subject instanceof XGettingCollection<?>)
		{
			return ((XGettingCollection<E>)this.subject).equals(samples, equalator);
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equalsContent(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		if(this.subject instanceof XGettingCollection<?>)
		{
			return ((XGettingCollection<E>)this.subject).equalsContent(samples, equalator);
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <C extends Consumer<? super E>> C except(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final C                               target
	)
	{
		if(this.subject instanceof XGettingCollection<?>)
		{
			return ((XGettingCollection<E>)this.subject).except(other, equalator, target);
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public final <P extends Consumer<? super E>> P iterate(final P procedure)
	{
		if(this.subject instanceof XGettingCollection<?>)
		{
			((XGettingCollection<E>)this.subject).iterate(procedure);
			return procedure;
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public final <P extends IndexedAcceptor<? super E>> P iterateIndexed(final P procedure)
	{
		if(this.subject instanceof XGettingSequence<?>)
		{
			((XGettingSequence<E>)this.subject).iterateIndexed(procedure);
			return procedure;
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public final <A> A join(final BiConsumer<? super E, ? super A> joiner, final A aggregate)
	{
		if(this.subject instanceof XJoinable<?>)
		{
			((XJoinable<E>)this.subject).join(joiner, aggregate);
			return aggregate;
		}
		
		throw new UnsupportedOperationException();
	}

	@Override
	public DownwrapList<E> fill(final long offset, final long length, final E element)
	{
		if(this.subject instanceof XSettingList<?>)
		{
			((XSettingList<E>)this.subject).fill(offset, length, element);
			return this;
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public E at(final long index)
	{
		if(this.subject instanceof XGettingSequence<?>)
		{
			return ((XGettingSequence<E>)this.subject).at(index);
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public E get()
	{
		if(this.subject instanceof XGettingSequence<?>)
		{
			return ((XGettingSequence<E>)this.subject).get();
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public E first()
	{
		if(this.subject instanceof XGettingSequence<?>)
		{
			return ((XGettingSequence<E>)this.subject).first();
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public E last()
	{
		if(this.subject instanceof XGettingSequence<?>)
		{
			return ((XGettingSequence<E>)this.subject).last();
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public E poll()
	{
		if(this.subject instanceof XGettingSequence<?>)
		{
			return ((XGettingSequence<E>)this.subject).poll();
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public E peek()
	{
		if(this.subject instanceof XGettingSequence<?>)
		{
			return ((XGettingSequence<E>)this.subject).peek();
		}
		
		throw new UnsupportedOperationException();
	}

	@Deprecated
	@Override
	public int hashCode()
	{
		return this.subject.hashCode();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean hasVolatileElements()
	{
		if(this.subject instanceof XGettingCollection<?>)
		{
			return ((XGettingCollection<E>)this.subject).hasVolatileElements();
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public long indexOf(final E element)
	{
		if(this.subject instanceof XGettingSequence<?>)
		{
			return ((XGettingSequence<E>)this.subject).indexOf(element);
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public long indexBy(final Predicate<? super E> predicate)
	{
		if(this.subject instanceof XGettingSequence<?>)
		{
			return ((XGettingSequence<E>)this.subject).indexBy(predicate);
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <C extends Consumer<? super E>> C intersect(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final C                               target
	)
	{
		if(this.subject instanceof XGettingCollection<?>)
		{
			return ((XGettingCollection<E>)this.subject).intersect(other, equalator, target);
		}
		
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isEmpty()
	{
		if(this.subject instanceof Sized)
		{
			return ((Sized)this.subject).isEmpty();
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean isSorted(final Comparator<? super E> comparator)
	{
		if(this.subject instanceof XGettingSequence<?>)
		{
			return ((XGettingSequence<E>)this.subject).isSorted(comparator);
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<E> iterator()
	{
		if(this.subject instanceof Iterable<?>)
		{
			return ((Iterable<E>)this.subject).iterator();
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public long lastIndexBy(final Predicate<? super E> predicate)
	{
		if(this.subject instanceof XGettingSequence<?>)
		{
			return ((XGettingSequence<E>)this.subject).lastIndexBy(predicate);
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public long lastIndexOf(final E element)
	{
		if(this.subject instanceof XGettingSequence<?>)
		{
			return ((XGettingSequence<E>)this.subject).lastIndexOf(element);
		}
		
		throw new UnsupportedOperationException();
	}

	@Override
	public ListIterator<E> listIterator()
	{
		if(this.subject instanceof XGettingList<?>)
		{
			return ((XGettingList<E>)this.subject).listIterator();
		}
		
		throw new UnsupportedOperationException();
	}

	@Override
	public ListIterator<E> listIterator(final long index)
	{
		if(this.subject instanceof XGettingList<?>)
		{
			return ((XGettingList<E>)this.subject).listIterator(index);
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public E max(final Comparator<? super E> comparator)
	{
		if(this.subject instanceof XGettingCollection<?>)
		{
			return ((XGettingCollection<E>)this.subject).max(comparator);
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public long maxIndex(final Comparator<? super E> comparator)
	{
		if(this.subject instanceof XGettingSequence<?>)
		{
			return ((XGettingSequence<E>)this.subject).maxIndex(comparator);
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public E min(final Comparator<? super E> comparator)
	{
		if(this.subject instanceof XGettingCollection<?>)
		{
			return ((XGettingCollection<E>)this.subject).min(comparator);
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public long minIndex(final Comparator<? super E> comparator)
	{
		if(this.subject instanceof XGettingSequence<?>)
		{
			return ((XGettingSequence<E>)this.subject).minIndex(comparator);
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <C extends Consumer<? super E>> C moveSelection(final C target, final long... indices)
	{
		if(this.subject instanceof XProcessingSequence<?>)
		{
			return ((XProcessingSequence<E>)this.subject).moveSelection(target, indices);
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <C extends Consumer<? super E>> C moveTo(final C target, final Predicate<? super E> predicate)
	{
		if(this.subject instanceof XProcessingCollection<?>)
		{
			return ((XProcessingCollection<E>)this.subject).moveTo(target, predicate);
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public final <P extends Consumer<? super E>> P process(final P procedure)
	{
		if(this.subject instanceof XProcessingCollection<?>)
		{
			((XProcessingCollection<E>)this.subject).process(procedure);
			return procedure;
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public long removeBy(final Predicate<? super E> predicate)
	{
		if(this.subject instanceof XProcessingCollection<?>)
		{
			return ((XProcessingCollection<E>)this.subject).removeBy(predicate);
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public long remove(final E element)
	{
		if(this.subject instanceof XProcessingCollection<?>)
		{
			return ((XProcessingCollection<E>)this.subject).remove(element);
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public E removeAt(final long index)
	{
		if(this.subject instanceof XProcessingSequence<?>)
		{
			return ((XProcessingSequence<E>)this.subject).removeAt(index);
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public long removeAll(final XGettingCollection<? extends E> elements)
	{
		if(this.subject instanceof XProcessingCollection<?>)
		{
			return ((XProcessingCollection<E>)this.subject).removeAll(elements);
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public long removeDuplicates(final Equalator<? super E> equalator)
	{
		if(this.subject instanceof XProcessingCollection<?>)
		{
			return ((XProcessingCollection<E>)this.subject).removeDuplicates(equalator);
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public long removeDuplicates()
	{
		if(this.subject instanceof XProcessingCollection<?>)
		{
			return ((XProcessingCollection<E>)this.subject).removeDuplicates();
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public E fetch()
	{
		if(this.subject instanceof XProcessingSequence<?>)
		{
			return ((XProcessingSequence<E>)this.subject).fetch();
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public E pop()
	{
		if(this.subject instanceof XProcessingSequence<?>)
		{
			return ((XProcessingSequence<E>)this.subject).pop();
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public E pinch()
	{
		if(this.subject instanceof XProcessingSequence<?>)
		{
			return ((XProcessingSequence<E>)this.subject).pinch();
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public E pick()
	{
		if(this.subject instanceof XProcessingSequence<?>)
		{
			return ((XProcessingSequence<E>)this.subject).pick();
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean removeOne(final E element)
	{
		if(this.subject instanceof XRemovingCollection<?>)
		{
			return ((XRemovingCollection<E>)this.subject).removeOne(element);
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public E retrieve(final E element)
	{
		if(this.subject instanceof XProcessingSequence<?>)
		{
			return ((XProcessingSequence<E>)this.subject).retrieve(element);
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public E retrieveBy(final Predicate<? super E> predicate)
	{
		if(this.subject instanceof XProcessingSequence<?>)
		{
			return ((XProcessingSequence<E>)this.subject).retrieveBy(predicate);
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public DownwrapList<E> removeRange(final long offset, final long length)
	{
		if(this.subject instanceof XProcessingSequence<?>)
		{
			((XProcessingSequence<E>)this.subject).removeRange(offset, length);
			return this;
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public DownwrapList<E> retainRange(final long offset, final long length)
	{
		if(this.subject instanceof XProcessingSequence<?>)
		{
			((XProcessingSequence<E>)this.subject).retainRange(offset, length);
			return this;
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public long removeSelection(final long[] indices)
	{
		if(this.subject instanceof XProcessingSequence<?>)
		{
			return ((XProcessingSequence<E>)this.subject).removeSelection(indices);
		}
		
		throw new UnsupportedOperationException();
	}

	@Override
	public long replace(final E element, final E replacement)
	{
		if(this.subject instanceof XSettingList<?>)
		{
			return ((XSettingList<E>)this.subject).replace(element, replacement);
		}
		
		throw new UnsupportedOperationException();
	}

	@Override
	public long replace(final Predicate<? super E> predicate, final E substitute)
	{
		if(this.subject instanceof XSettingList<?>)
		{
			return ((XSettingList<E>)this.subject).replace(predicate, substitute);
		}
		
		throw new UnsupportedOperationException();
	}

	@Override
	public long substitute(final Function<? super E, ? extends E> mapper)
	{
		if(this.subject instanceof XSettingList<?>)
		{
			return ((XSettingList<E>)this.subject).substitute(mapper);
		}
		
		throw new UnsupportedOperationException();
	}

	@Override
	public long substitute(final Predicate<? super E> predicate, final Function<E, E> mapper)
	{
		if(this.subject instanceof XSettingList<?>)
		{
			return ((XSettingList<E>)this.subject).substitute(predicate, mapper);
		}
		
		throw new UnsupportedOperationException();
	}

	@Override
	public long replaceAll(final XGettingCollection<? extends E> elements, final E replacement)
	{
		if(this.subject instanceof XSettingList<?>)
		{
			return ((XSettingList<E>)this.subject).replaceAll(elements, replacement);
		}
		
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean replaceOne(final E element, final E replacement)
	{
		if(this.subject instanceof XSettingList<?>)
		{
			return ((XSettingList<E>)this.subject).replaceOne(element, replacement);
		}
		
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean replaceOne(final Predicate<? super E> predicate, final E substitute)
	{
		if(this.subject instanceof XSettingList<?>)
		{
			return ((XSettingList<E>)this.subject).replaceOne(predicate, substitute);
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public long retainAll(final XGettingCollection<? extends E> elements)
	{
		if(this.subject instanceof XProcessingCollection<?>)
		{
			return ((XProcessingCollection<E>)this.subject).retainAll(elements);
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public DownwrapList<E> reverse()
	{
		if(this.subject instanceof XSortableSequence<?>)
		{
			((XSortableSequence<E>)this.subject).reverse();
			return this;
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public long scan(final Predicate<? super E> predicate)
	{
		if(this.subject instanceof XGettingSequence<?>)
		{
			return ((XGettingSequence<E>)this.subject).scan(predicate);
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public E seek(final E sample)
	{
		if(this.subject instanceof XGettingCollection<?>)
		{
			return ((XGettingCollection<E>)this.subject).seek(sample);
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public E search(final Predicate<? super E> predicate)
	{
		if(this.subject instanceof XGettingCollection<?>)
		{
			return ((XGettingCollection<E>)this.subject).search(predicate);
		}
		
		throw new UnsupportedOperationException();
	}

	@SafeVarargs
	@Override
	public final DownwrapList<E> setAll(final long offset, final E... elements)
	{
		if(this.subject instanceof XSettingList<?>)
		{
			((XSettingList<E>)this.subject).setAll(offset, elements);
			return this;
		}
		
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean set(final long index, final E element)
	{
		if(this.subject instanceof XSettingList<?>)
		{
			return ((XSettingList<E>)this.subject).set(index, element);
		}
		
		throw new UnsupportedOperationException();
	}

	@Override
	public E setGet(final long index, final E element)
	{
		if(this.subject instanceof XSettingList<?>)
		{
			return ((XSettingList<E>)this.subject).setGet(index, element);
		}
		
		throw new UnsupportedOperationException();
	}

	@Override
	public DownwrapList<E> set(final long offset, final E[] src, final int srcIndex, final int srcLength)
	{
		if(this.subject instanceof XSettingList<?>)
		{
			((XSettingList<E>)this.subject).set(offset, src, srcIndex, srcLength);
			return this;
		}
		
		throw new UnsupportedOperationException();
	}

	@Override
	public DownwrapList<E> set(final long offset, final XGettingSequence<? extends E> elements, final long elementsOffset, final long elementsLength)
	{
		if(this.subject instanceof XSettingList<?>)
		{
			 ((XSettingList<E>)this.subject).set(offset, elements, elementsOffset, elementsLength);
			return this;
		}
		
		throw new UnsupportedOperationException();
	}

	@Override
	public void setFirst(final E element)
	{
		if(this.subject instanceof XSettingList<?>)
		{
			((XSettingList<E>)this.subject).setFirst(element);
		}
		
		throw new UnsupportedOperationException();
	}

	@Override
	public void setLast(final E element)
	{
		if(this.subject instanceof XSettingList<?>)
		{
			((XSettingList<E>)this.subject).setLast(element);
		}
		
		throw new UnsupportedOperationException();
	}

	@Override
	public long optimize()
	{
		if(this.subject instanceof OptimizableCollection)
		{
			return ((OptimizableCollection)this.subject).optimize();
		}
		
		throw new UnsupportedOperationException();
	}

	@Override
	public long size()
	{
		if(this.subject instanceof Sized)
		{
			return ((Sized)this.subject).size();
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public DownwrapList<E> sort(final Comparator<? super E> comparator)
	{
		if(this.subject instanceof XSortableSequence<?>)
		{
			((XSortableSequence<E>)this.subject).sort(comparator);
			return this;
		}
		
		throw new UnsupportedOperationException();
	}

	@Override
	public XList<E> range(final long fromIndex, final long toIndex)
	{
		if(this.subject instanceof XList<?>)
		{
			return ((XList<E>)this.subject).range(fromIndex, toIndex);
		}
		if(this.subject instanceof XGettingList<?>)
		{
			return new DownwrapList<>(((XGettingList<E>)this.subject).range(fromIndex, toIndex));
		}
		
		throw new UnsupportedOperationException();
	}

	@Override
	public ListView<E> view()
	{
		return new ListView<>(this);
	}

	@Override
	public SubListView<E> view(final long fromIndex, final long toIndex)
	{
		return new SubListView<>(this, fromIndex, toIndex);
	}

	@SuppressWarnings("unchecked")
	@Override
	public DownwrapList<E> shiftTo(final long sourceIndex, final long targetIndex)
	{
		if(this.subject instanceof XSortableSequence<?>)
		{
			((XSortableSequence<E>)this.subject).shiftTo(sourceIndex, targetIndex);
			return this;
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public DownwrapList<E> shiftTo(final long sourceIndex, final long targetIndex, final long length)
	{
		if(this.subject instanceof XSortableSequence<?>)
		{
			((XSortableSequence<E>)this.subject).shiftTo(sourceIndex, targetIndex, length);
			return this;
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public DownwrapList<E> shiftBy(final long sourceIndex, final long distance)
	{
		if(this.subject instanceof XSortableSequence<?>)
		{
			((XSortableSequence<E>)this.subject).shiftBy(sourceIndex, distance);
			return this;
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public DownwrapList<E> shiftBy(final long sourceIndex, final long distance, final long length)
	{
		if(this.subject instanceof XSortableSequence<?>)
		{
			((XSortableSequence<E>)this.subject).shiftBy(sourceIndex, distance, length);
			return this;
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public DownwrapList<E> swap(final long indexA, final long indexB)
	{
		if(this.subject instanceof XSortableSequence<?>)
		{
			((XSortableSequence<E>)this.subject).swap(indexA, indexB);
			return this;
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public DownwrapList<E> swap(final long indexA, final long indexB, final long length)
	{
		if(this.subject instanceof XSortableSequence<?>)
		{
			((XSortableSequence<E>)this.subject).swap(indexA, indexB, length);
			return this;
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object[] toArray()
	{
		if(this.subject instanceof XGettingCollection<?>)
		{
			return ((XGettingCollection<E>)this.subject).toArray();
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public E[] toArray(final Class<E> type)
	{
		if(this.subject instanceof XGettingCollection<?>)
		{
			return ((XGettingCollection<E>)this.subject).toArray(type);
		}
		
		throw new UnsupportedOperationException();
	}

	@Override
	public XImmutableList<E> immure()
	{
		if(this.subject instanceof XGettingList<?>)
		{
			return ((XGettingList<E>)this.subject).immure();
		}
		
		throw new UnsupportedOperationException();
	}

	@Override
	public DownwrapList<E> toReversed()
	{
		if(this.subject instanceof XGettingList<?>)
		{
			return new DownwrapList<>(((XGettingList<E>)this.subject).toReversed());
		}
		
		throw new UnsupportedOperationException();
	}

	@Override
	public void truncate()
	{
		if(this.subject instanceof Truncateable)
		{
			((Truncateable)this.subject).truncate();
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <C extends Consumer<? super E>> C union(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final C                               target
	)
	{
		if(this.subject instanceof XGettingCollection<?>)
		{
			return ((XGettingCollection<E>)this.subject).union(other, equalator, target);
		}
		
		throw new UnsupportedOperationException();
	}

	@Override
	public long currentCapacity()
	{
		if(this.subject instanceof CapacityExtendable)
		{
			return ((CapacityExtendable)this.subject).currentCapacity();
		}
		
		throw new UnsupportedOperationException();
	}

	@Override
	public long maximumCapacity()
	{
		if(this.subject instanceof CapacityCarrying)
		{
			return ((CapacityCarrying)this.subject).maximumCapacity();
		}
		
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isFull()
	{
		if(this.subject instanceof CapacityCarrying)
		{
			return ((CapacityCarrying)this.subject).isFull();
		}
		
		throw new UnsupportedOperationException();
	}

	@Override
	public long remainingCapacity()
	{
		if(this.subject instanceof CapacityCarrying)
		{
			return ((CapacityCarrying)this.subject).remainingCapacity();
		}
		
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean nullAllowed()
	{
		return this.subject.nullAllowed();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean nullContained()
	{
		if(this.subject instanceof XGettingCollection<?>)
		{
			return ((XGettingCollection<E>)this.subject).nullContained();
		}
		
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public long nullRemove()
	{
		if(this.subject instanceof XProcessingCollection<?>)
		{
			return ((XProcessingCollection<E>)this.subject).nullRemove();
		}
		
		throw new UnsupportedOperationException();
	}

	@Override
	public OldWrapperList<E> old()
	{
		return new OldWrapperList<>(this);
	}

	public static final class OldWrapperList<E> extends AbstractBridgeXList<E>
	{
		OldWrapperList(final DownwrapList<E> list)
		{
			super(list);
		}

	@Override
	public DownwrapList<E> parent()
		{
			return (DownwrapList<E>)super.parent();
		}

	}

}
