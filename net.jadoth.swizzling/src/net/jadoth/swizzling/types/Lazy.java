package net.jadoth.swizzling.types;

import java.util.function.Consumer;
import java.util.function.Predicate;

import net.jadoth.Jadoth;
import net.jadoth.reference.LazyReferencing;
import net.jadoth.traversal.ObjectGraphTraverserFactory;
import net.jadoth.traversal.TraversalHandler;
import net.jadoth.traversal.TraversalHandlerCustomProvider;
import net.jadoth.traversal.TraversalHandlingLogicProvider;


/**
 * A reference providing generic lazy-loading functionality.
 * <p>
 * Note that the shortened name has been chosen intentionally to optimize readability in class design.
 * <p>
 * Also note that a type like this is strongly required to implement lazy loading behaviour in an architectural
 * clean and proper way. I.e. the design has to define that a certain reference is meant be capable of lazy-loading.
 * If such a definition is not done, a loading logic is strictly required to always load the encountered reference,
 * as it is defined by the normal reference.
 * Any "tricks" of whatever framework to "sneak in" lazy loading behaviour where it hasn't actually been defined
 * are nothing more than dirty hacks and mess up if not destroy the program's consistency of state
 * (e.g. antipatterns like secretly replacing a well-defined collection instance with a framework-proprietary
 * proxy instance of a "similar" collection implementation).
 * In proper architectured sofware, if a reference does not define lazy loading capacity, it is not wanted to have
 * that capacity on the business logical level by design in the first place. Any attempts of saying
 * "but I want it anyway in a sneaky 'transparent' way" indicate ambivalent conflicting design errors and thus
 * in the end poor design.
 *
 * @author Thomas Muenz
 * @param <T>
 */
public final class Lazy<T> implements LazyReferencing<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings("all")
	public static final Class<Lazy<?>> genericType()
	{
		// no idea how to get ".class" to work otherwise in conjunction with generics.
		return (Class)Lazy.class;
	}

	public static final <T> T get(final Lazy<T> reference)
	{
		return reference == null ? null : reference.get();
	}

	public static final <T> T peek(final Lazy<T> reference)
	{
		return reference == null ? null : reference.peek();
	}

	public static final void clear(final Lazy<?> reference)
	{
		if(reference == null)
		{
			return; // debug-hook
		}
		reference.clear();
	}

	public static final <T> Lazy<T> Reference(final T subject)
	{
		return register(new Lazy<>(subject));
	}

	public static final <T> Lazy<T> New(final long objectId)
	{
		return register(new Lazy<>(null, objectId, null));
	}

	public static final <T> Lazy<T> New(final long objectId, final SwizzleObjectSupplier loader)
	{
		return register(new Lazy<>(null, objectId, loader));
	}

	public static final <T> Lazy<T> New(final T subject, final long objectId, final SwizzleObjectSupplier loader)
	{
		return register(new Lazy<>(subject, objectId, loader));
	}

	public static final Checker Checker(final long millisecondTimeout)
	{
		return new Checker(millisecondTimeout);
	}

	static <T> Lazy<T> register(final Lazy<T> lazyReference)
	{
//		JadothConsole.debugln("Registering " + Jadoth.systemString(lazyReference) + " " + lazyReference.objectId);
		LazyReferenceManager.get().register(lazyReference);
		return lazyReference;
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	/**
	 * The actual subject to be referenced.
	 */
	private T subject;

	/**
	 * The timestamp in milliseconds when this reference has last been touched (created or queried).
	 * If an instance is deemed timed out by a {@link LazyReferenceManager} based on the current time
	 * and some arbitrary timeout threshold, its subject gets cleared.
	 */
	transient long lastTouched;

	/**
	 * The cached object id of the not loaded actual instance to later load it lazily.
	 * Although this value never changes logically during the lifetime of an instance,
	 * it might be delayed initialized. See {@link #link(long, SwizzleObjectSupplier)} and its use site(s).
	 */
	// CHECKSTYLE.OFF: VisibilityModifier CheckStyle false positive for samge package in another project
	transient long objectId;
	// CHECKSTYLE.ON: VisibilityModifier

	/**
	 * The loader to be used for loading the actual subject via the deposited object id.
	 * This should typically be the loader instance that should have loaded the actual instance
	 * in the first place but did not to do its work later lazyely. Apart from this idea,
	 * there is no "hard" contract on what the loader instance should specifically be.
	 */
	private transient SwizzleObjectSupplier loader;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Standard constructor used by normal logic to instantiate a reference.
	 *
	 * @param subject the subject to be referenced.
	 */
	private Lazy(final T subject)
	{
		this(subject, Swizzle.nullId(), null);
	}

	/**
	 * Special constructor used by logic that lazily skips the actual instance (e.g. internal loading logic)
	 * but instead provides means to get the instance at a later point in time.
	 *
	 * @param subject the potentially already present subject to be referenced or null.
	 * @param objectId the subject's object id under which it can be reconstructed by the provided loader
	 * @param loader the loader used to reconstruct the actual instance originally referenced
	 */
	private Lazy(final T subject, final long objectId, final SwizzleObjectSupplier loader)
	{
		super();
		this.subject  = subject ;
		this.objectId = objectId;
		this.loader   = loader  ;
		this.touch();
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	private void touch()
	{
		this.lastTouched = this.subject == null ? Long.MAX_VALUE : System.currentTimeMillis();
	}

	/**
	 * Returns the wrapped reference in its current state without loading it on demand.
	 *
	 * @return the current reference withouth on-demand loading.
	 */
	@Override
	public final synchronized T peek()
	{
		return this.subject;
	}

	/**
	 * Clears the reference, leaving the option to re-load it again intact, and returns the subject that was
	 * referenced prior to clearing.
	 *
	 * @return the subject referenced prior to clearing the reference.
	 */
	public final synchronized T clear()
	{
		final T subject = this.subject;
		this.internalClear();
		return subject;
	}

	private void validateObjectIdToBeSet(final long objectId)
	{
		if(this.objectId != Swizzle.nullId() && this.objectId != objectId)
		{
			// (22.10.2014)TODO: proper exception
			throw new RuntimeException("ObjectId already set: " + this.objectId);
		}
	}

	final synchronized void setLoader(final SwizzleObjectSupplier loader)
	{
		/*
		 * this method might be called when storing or building to/from different sources
		 * (e.g. client-server communication instead of database storing).
		 * To keep the logic simple, only the first non-null passing call will be considered.
		 * It is assumed that the application logic is tailored in such a way that the "primary" or "actual"
		 * loader (probably for a database) will be passed here first.
		 * if this is not sufficient for certain mor complex demands, this class (and its binary handler etc.)
		 * can always be replaced by a copy derived from it. Nothing magical about it (e.g. hardcoded somewhere in the
		 * persisting logic).
		 */
		if(this.loader != null)
		{
			return;
		}
		this.loader = loader;
	}

	final synchronized void link(final long objectId, final SwizzleObjectSupplier loader)
	{
		this.validateObjectIdToBeSet(objectId);
		this.objectId = objectId;
		this.setLoader(loader);
	}

	private void internalClear()
	{
//		JadothConsole.debugln("Clearing " + Lazy.class.getSimpleName() + " " + this.subject);
		this.subject = null;
		this.touch();
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	/**
	 * Returns the original subject referenced by this reference instance.
	 * If the subject has (lazily) not been loaded, an attempt to do so now is made.
	 * Any exception occuring during the loading attempt will be passed along without currupting this
	 * reference instance's internal state.
	 *
	 * @return the originally referenced subject, either already-known or lazy-loaded.
	 */
	@Override
	public final synchronized T get()
	{
		if(this.subject == null && this.objectId != Swizzle.nullId())
		{
			this.load();
		}
		/* There are 3 possible cases at this point:
		 * 1.) subject is not null because it has been set via the normal constructor directly and is simply returned
		 * 2.) subject was lazily null but has been successfully thread-safely loaded, set and can now be returned
		 * 3.) subject was null in the first place (one way or another) and null gets returned.
		 */
		this.touch();
		return this.subject;
	}

	@SuppressWarnings("unchecked") // safety of cast guaranteed by logic
	private synchronized void load()
	{
		// this context doesn't have to do anything on an exception inside the get(), just pass it along
		this.subject = (T)this.loader.get(this.objectId);
	}

	final synchronized void clearIfTimedout(final long millisecondThreshold)
	{
//		JadothConsole.debugln("Checking " + this.subject + ": " + this.lastTouched + " vs " + millisecondThreshold);

		// time check implicitely covers already cleared reference. May of course not clear if there is no loader (yet).
		if(this.lastTouched >= millisecondThreshold || this.loader == null)
		{
			return;
		}

//		JadothConsole.debugln("timeout-clearing " + this.objectId + ": " + Jadoth.systemString(this.subject));
		this.internalClear();
	}

	public final long objectId()
	{
		return this.objectId;
	}

	@Override
	public String toString()
	{
		return this.subject == null
			? "(" + this.objectId + ")"
			: this.objectId + " " + Jadoth.systemString(this.subject)
		;
	}



	public static final class Checker implements LazyReferenceManager.Checker
	{
		private final long millisecondTimeout  ;
		private       long millisecondThreshold;


		Checker(final long millisecondTimeout)
		{
			super();
			this.millisecondTimeout = millisecondTimeout;
		}


		@Override
		public void beginCheckCycle()
		{
			this.millisecondThreshold = System.currentTimeMillis() - this.millisecondTimeout;
		}

		@Override
		public void accept(final LazyReferencing<?> lazyReference)
		{
			if(!(lazyReference instanceof Lazy))
			{
				return;
			}
			((Lazy<?>)lazyReference).clearIfTimedout(this.millisecondThreshold);
		}

	}


	public static <F extends ObjectGraphTraverserFactory> F registerWith(final F objectGraphTraverserFactory)
	{
		objectGraphTraverserFactory.registerTraversalHandlerProviderByType(
			Lazy.genericType(),
			new TraversalHandlerLazy.Provider()
		);

		return objectGraphTraverserFactory;
	}



	public static final TraversalHandlerCustomProvider<Lazy<?>> TraversalHandlerCustomProvider()
	{
		return new TraversalHandlerLazy.Provider();
	}


	// not sure if it's particularly clean or particularly unclean to nest that here instead of in its own file.
	public static final class TraversalHandlerLazy extends TraversalHandler.AbstractImplementation<Lazy<?>>
	{
		protected TraversalHandlerLazy(final Predicate<? super Lazy<?>> logic)
		{
			super(logic);
		}

		@Override
		public final Class<Lazy<?>> handledType()
		{
			return Lazy.genericType();
		}

		@Override
		public void traverseReferences(final Lazy<?> instance, final Consumer<Object> referenceHandler)
		{
//			if(instance.peek() == null)
//			{
//				debugln("loading " + instance.objectId);
//			}

			// the loader reference is a meta helper that is no actual entity, so it is ignored.
			referenceHandler.accept(instance.get());
		}

		public static final class Provider implements TraversalHandlerCustomProvider<Lazy<?>>
		{
			@Override
			public final Class<Lazy<?>> handledType()
			{
				return Lazy.genericType();
			}

			@Override
			public TraversalHandler<Lazy<?>> provideTraversalHandler(
				final Class<? extends Lazy<?>>       type         ,
				final TraversalHandlingLogicProvider logicProvider
			)
			{
				/*
				 * this is guaranteed by the using logic, but just in case.
				 * Performance doesn't matter in one-time analyzing logic.
				 */
				// (06.07.2016 TM)NOTE: javac reports an error here. Probably one of several bugs encountered when trying to use it.
				if(type != Lazy.class)
				{
					throw new IllegalArgumentException();
				}

				return new TraversalHandlerLazy(logicProvider.provideHandlingLogic(type));
			}

		}

	}

}
