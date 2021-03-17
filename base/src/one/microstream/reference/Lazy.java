package one.microstream.reference;

import static one.microstream.X.mayNull;

import one.microstream.chars.VarString;
import one.microstream.chars.XChars;
import one.microstream.memory.MemoryStatistics;
import one.microstream.memory.MemoryStatisticsProvider;
import one.microstream.meta.XDebug;


/**
 * A reference providing generic lazy-loading functionality.
 * <p>
 * Note that the shortened name has been chosen intentionally to optimize readability in class design.
 * <p>
 * Also note that a type like this is strongly required in order to implement lazy loading behavior in an application
 * in an architecturally clean and proper way. I.e. the application's data model design has to define that a certain
 * reference is meant to be capable of lazy-loading.
 * If such a definition is not done, a loading logic is strictly required to always load the encountered reference,
 * as it is defined by the "normal" way of how references work.
 * Any "tricks" of whatever framework to "sneak in" lazy loading behavior where it hasn't actually been defined
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
public interface Lazy<T> extends Referencing<T>
{
	/**
	 * Returns the referenced object, loading it if required.
	 * @return the lazily loaded referenced object.
	 */
	@Override
	public T get();

	/**
	 * Returns the local reference without loading the referenced object if it is not present.
	 * The value returned by {@link #lastTouched()} will not be changed by calling this method.
	 * 
	 * @return the currently present reference.
	 */
	public T peek();
	
	public T clear();

	public boolean isStored();

	public boolean isLoaded();
	
	/**
	 * Returns the timestamp (corresponding to {@link System#currentTimeMillis()}) when this instance has last been
	 * "touched", meaning having its reference modified or queried.
	 * 
	 * @return the time this instance has last been significantly used.
	 */
	public long lastTouched();
	
	public boolean clear(Lazy.ClearingEvaluator clearingEvaluator);
	
	

	public static <T> T get(final Lazy<T> reference)
	{
		if(reference == null)
		{
			return null; // debug-hook
		}
		
		return reference.get();
	}

	public static <T> T peek(final Lazy<T> reference)
	{
		if(reference == null)
		{
			return null; // debug-hook
		}
		
		return reference.peek();
	}

	public static void clear(final Lazy<?> reference)
	{
		if(reference == null)
		{
			return; // debug-hook
		}
		
		reference.clear();
	}
	
	public static boolean isStored(final Lazy<?> reference)
	{
		if(reference == null)
		{
			return false; // debug-hook
		}
		
		return reference.isStored();
	}
	
	public static boolean isLoaded(final Lazy<?> reference)
	{
		if(reference == null)
		{
			// philosophical question: is a non-existent reference implicitely always loaded or never loaded?
			return false; // debug-hook
		}
		
		return reference.isLoaded();
	}

	public static <T> Lazy<T> Reference(final T subject)
	{
		return register(new Lazy.Default<>(subject));
	}

	public static <T> Lazy<T> New(final long objectId)
	{
		return register(new Lazy.Default<>(null, objectId, null));
	}

	public static <T> Lazy<T> New(final long objectId, final ObjectSwizzling loader)
	{
		return register(new Lazy.Default<>(null, objectId, loader));
	}

	public static <T> Lazy<T> New(final T subject, final long objectId, final ObjectSwizzling loader)
	{
		return register(new Lazy.Default<>(subject, objectId, loader));
	}

	public static <T, L extends Lazy<T>> L register(final L lazyReference)
	{
		LazyReferenceManager.get().register(lazyReference);
		return lazyReference;
	}
	
	public final class Default<T> implements Lazy<T>
	{
		@SuppressWarnings("all")
		public static final Class<Lazy.Default<?>> genericType()
		{
			// no idea how to get ".class" to work otherwise in conjunction with generics.
			return (Class)Lazy.Default.class;
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
		 * it might be delayed initialized. See {@link #link(long, ObjectSwizzling)} and its use site(s).
		 * 
		 * A "not found" id (id < 0) here means not yet persisted (the id assigned via persisting is not yet present).
		 */
		// CHECKSTYLE.OFF: VisibilityModifier CheckStyle false positive for same package in another project
		transient long objectId;
		// CHECKSTYLE.ON: VisibilityModifier

		/**
		 * The loader to be used for loading the actual subject via the deposited object id.
		 * This should typically be the loader instance that should have loaded the actual instance
		 * in the first place but did not to do its work later lazyely. Apart from this idea,
		 * there is no "hard" contract on what the loader instance should specifically be.
		 */
		private transient ObjectSwizzling loader;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		/**
		 * Standard constructor used by normal logic to instantiate a reference.
		 *
		 * @param subject the subject to be referenced.
		 */
		Default(final T subject)
		{
			this(subject, Swizzling.toUnmappedObjectId(subject), null);
		}

		/**
		 * Special constructor used by logic that lazily skips the actual instance (e.g. internal loading logic)
		 * but instead provides means to get the instance at a later point in time.
		 *
		 * @param subject the potentially already present subject to be referenced or null.
		 * @param objectId the subject's object id under which it can be reconstructed by the provided loader
		 * @param loader the loader used to reconstruct the actual instance originally referenced
		 */
		Default(final T subject, final long objectId, final ObjectSwizzling loader)
		{
			super();
			this.subject  = subject ;
			this.objectId = objectId;
			this.loader   = loader  ;
			this.touch();
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		public final long objectId()
		{
			return this.objectId;
		}
		
		@Override
		public final long lastTouched()
		{
			return this.lastTouched;
		}
		
		@Override
		public final synchronized boolean isStored()
		{
			// A "not found" id (id < 0) here means not yet persisted (the id assigned via persisting is not yet present).
			return Swizzling.isFoundId(this.objectId);
		}
		
		@Override
		public final synchronized boolean isLoaded()
		{
			/* Sounds trivial, but there are a lot of cases, here:
			 * 1.) Not yet persisted cases (id < 0) are implicitely always "loaded".
			 * 2.) A null-reference (id == 0) is always "loaded"
			 * 3.) Otherwise, the subject must be present (truly a state of having been "loaded")
			 */
			return Swizzling.isNotProperId(this.objectId) || this.subject != null;
		}

		/**
		 * Returns the wrapped reference without loading it on demand.
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
		@Override
		public final synchronized T clear()
		{
			final T subject = this.subject;
			this.internalClear();
			return subject;
		}
		
		@Override
		public final synchronized boolean clear(final ClearingEvaluator clearingEvaluator)
		{
			// must be stored and not already cleared to even consider asking the evaluator
			if(this.isStored() && this.subject != null && clearingEvaluator.needsClearing(this))
			{
				this.internalClear();
				return true;
			}

			// otherwise, no clearing
			return false;
		}

		private void touch()
		{
			this.lastTouched = this.subject != null
				? System.currentTimeMillis()
				: Long.MAX_VALUE
			;
		}

		private void validateObjectIdToBeSet(final long objectId)
		{
			if(Swizzling.isFoundId(this.objectId) && this.objectId != objectId)
			{
				// (22.10.2014 TM)TODO: proper exception
				throw new RuntimeException("ObjectId already set: " + this.objectId);
			}
		}

		final synchronized void setLoader(final ObjectSwizzling loader)
		{
			/*
			 * This method might be called when storing or building to/from different sources
			 * (e.g. client-server communication instead of database storing).
			 * To keep the logic simple, only the first non-null passing call will be considered.
			 * It is assumed that the application logic is tailored in such a way that the "primary" or "actual"
			 * loader (probably for a database) will be passed here first.
			 * If this is not sufficient for certain more complex demands, this class (and its binary handler etc.)
			 * can always be replaced by a copy derived from it. Nothing magical about it (nothing hardcoded
			 * somewhere in the persisting logic or such).
			 */
			if(this.loader != null)
			{
				return;
			}
			this.loader = loader;
		}

		final synchronized void link(final long objectId, final ObjectSwizzling loader)
		{
			this.validateObjectIdToBeSet(objectId);
			this.setLoader(loader);
			this.objectId = objectId;
		}

		void internalClear()
		{
			// (03.09.2019 TM)NOTE: may never clear an unstored reference
			if(!this.isStored())
			{
				// (03.09.2019 TM)EXCP: proper exception
				throw new RuntimeException("Cannot clear an unstored lazy reference.");
			}
			
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
			// no need to "load" a persisted null value (id == 0) or a not yet persisted null value (id < 0)
			if(this.subject == null && Swizzling.isProperId(this.objectId))
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
			this.subject = (T)this.loader.getObject(this.objectId);
		}

		final synchronized boolean clearIfTimedout(final long millisecondThreshold)
		{
//			XDebug.debugln("Checking " + this.subject + ": " + this.lastTouched + " vs " + millisecondThreshold);

			// time check implicitely covers already cleared reference. May of course not clear unstored references.
			if(this.lastTouched >= millisecondThreshold || !this.isStored())
			{
				return false;
			}

//			XDebug.debugln("timeout-clearing " + this.objectId + ": " + XChars.systemString(this.subject));
			this.internalClear();
			return true;
		}

		@Override
		public String toString()
		{
			return this.subject == null
				? "(" + this.objectId + " not loaded)"
				: this.objectId + " " + XChars.systemString(this.subject)
			;
		}
		
	}
	
	

	
	@FunctionalInterface
	public interface ClearingEvaluator
	{
		public boolean needsClearing(Lazy<?> lazyReference);
	}
	
	@FunctionalInterface
	public interface Check
	{
		public Boolean test(Lazy<?> lazyReference, MemoryStatistics memoryStatistics, long millisecondTimeout);
	}

	
	public static Lazy.Checker Checker()
	{
		return Checker(null);
	}
	
	public static Lazy.Checker Checker(
		final long millisecondTimeout
	)
	{
		return Checker(millisecondTimeout, Checker.Defaults.defaultMemoryQuota());
	}
	
	public static Lazy.Checker Checker(
		final double memoryQuota
	)
	{
		return Checker(Checker.Defaults.defaultTimeout(), memoryQuota);
	}
	
	public static Lazy.Checker Checker(
		final long   millisecondTimeout,
		final double memoryQuota
	)
	{
		return Checker(millisecondTimeout, memoryQuota, null, null);
	}
	
	public static Lazy.Checker Checker(
		final Check customCheck
	)
	{
		return Checker(
			Checker.Defaults.defaultTimeout(),
			Checker.Defaults.defaultMemoryQuota(),
			customCheck,
			null
		);
	}
	
	public static Lazy.Checker Checker(
		final long                                millisecondTimeout,
		final double                              memoryQuota       ,
		final Check                               customCheck       ,
		final LazyReferenceManager.CycleEvaluator cycleEvaluator
	)
	{
		// note: at least timeout is validated to always be a usable value. The other two can be null/0.
		return new Checker.Default(
			Checker.validateTimeout(millisecondTimeout),
			Checker.validateMemoryQuota(memoryQuota),
			mayNull(customCheck),
			mayNull(cycleEvaluator)
		);
	}
	
	public static Lazy.Checker CheckerTimeout(
		final long millisecondTimeout
	)
	{
		return Checker(millisecondTimeout, 0.0);
	}
	
	public static Lazy.Checker CheckerMemory(
		final double memoryQuota
	)
	{
		// kind of dumb, but well ...
		return Checker(Long.MAX_VALUE, memoryQuota);
	}
	
	@FunctionalInterface
	public interface Checker
	{
		public default void beginCheckCycle()
		{
			// no-op by default
		}

		/**
		 * 
		 * @param lazyReference the lazy reference to check against
		 * @return if additional checks should be prevented
		 */
		public boolean check(Lazy<?> lazyReference);

		public default void endCheckCycle()
		{
			// no-op by default
		}
		
		
		public interface Defaults
		{
			public static long defaultTimeout()
			{
				// 1_000_000 ms are about 15 minutes.
				return 1_000_000;
			}
			
			public static double defaultMemoryQuota()
			{
				// all avaiable memory is used
				return 1.0;
			}
			
		}
		
		public static boolean isValidTimeout(final long millisecondTimeout)
		{
			return millisecondTimeout > 0;
		}
		
		public static boolean isValidMemoryQuota(final double memoryQuota)
		{
			return memoryQuota >= 0.0 && memoryQuota <= 1.0;
		}
		
		public static long validateTimeout(final long millisecondTimeout)
		{
			if(isValidTimeout(millisecondTimeout))
			{
				return millisecondTimeout;
			}
			
			// (28.01.2020 TM)EXCP: proper exception
			throw new RuntimeException("Timeout must be greater than 0.");
		}
		
		public static double validateMemoryQuota(final double memoryQuota)
		{
			if(isValidMemoryQuota(memoryQuota))
			{
				return memoryQuota;
			}
			
			// (28.01.2020 TM)EXCP: proper exception
			throw new RuntimeException("Memory quota must be in the range [0.0; 1.0].");
		}
				

				
		/**
		 * This implementation uses two dimensions to evaluate if a lazy reference will be cleared:<br>
		 * - time: a ref's "age" in terms of {@link Lazy#lastTouched()} compared to {@link System#currentTimeMillis()}<br>
		 * - memory: the amount of used memory compared to the permitted quota of total available memory.
		 * <p>
		 * Either dimension can be deactivated by setting its configuration value to 0.<br>
		 * If both are non-zero, a arithmetically combined check will make clearing of a certain reference
		 * more like the older it gets as free memory shrinks.<br>
		 * So, as free memory gets lower, older/passive references are cleared sooner, newer/active ones later.
		 * 
		 * @author TM
		 */
		public final class Default implements Lazy.Checker, Lazy.ClearingEvaluator
		{
			///////////////////////////////////////////////////////////////////////////
			// constants //
			//////////////
			
			public static double memoryQuotaNoCheck()
			{
				return 0.0;
			}
			
			public static long graceTimeMinimum()
			{
				return 1000;
			}
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			// configuration values (final) //

			
			/**
			 * An optional custom checking logic that overrides the generic logic.<br>
			 * May be <code>null</code>.<br>
			 * If it returns <code>null</code>, the generic logic is used.
			 */
			private final Check customCheck;
			
			private final LazyReferenceManager.CycleEvaluator cycleEvaluator;
			
			/**
			 * The timeout in milliseconds after which a reference is cleared regardless of memory consumption.<br>
			 * May be 0 to be deactivated.<br>
			 * Negative values are invalid and must be checked before the constructor is called.
			 */
			private final long timeoutMs;
			
			private final long graceTimeMs;
			
			/**
			 * The quota of total available memory (= min(committed, max)) the check has to comply with.<br>
			 * May be 0.0 to be deactivated.<br>
			 * 1.0 means all of the available memory can be used.<br>
			 * Anything outside of [0.0; 1.0] is invalid and must be checked before the constructor is called.
			 * 
			 */
			private final double memoryQuota;
			
			// cycle working variables //
			
			private MemoryStatistics cycleMemoryStatistics;

			private long cycleStartMs;
			
			/**
			 * The {@link System#currentTimeMillis()}-compliant timestamp value below which a reference
			 * is considered to be timed out and will be cleared regardless of memory consumption.
			 */
			private long cycleTimeoutThresholdMs;
			
			private long cycleGraceTimeThresholdMs;
			
			/**
			 * The maximum number of bytes that may be used before
			 */
			private long cycleMemoryLimit;
			private long cycleMemoryUsed;
			private long cycleClearCount;
			
			// derive working variables for fast integer arithmetic //
			
			private long sh10MemoryLimit;
			private long sh10MemoryUsed;

			
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////

			Default(
				final long                                timeoutMs     ,
				final double                              memoryQuota   ,
				final Check                               customCheck   ,
				final LazyReferenceManager.CycleEvaluator cycleEvaluator
			)
			{
				super();
				this.timeoutMs      = timeoutMs     ;
				this.memoryQuota    = memoryQuota   ;
				this.customCheck    = customCheck   ;
				this.cycleEvaluator = cycleEvaluator;
				
				this.graceTimeMs = deriveGraceTime(timeoutMs);
			}
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////
			
			private static long deriveGraceTime(final long timeoutMs)
			{
				return Math.min(graceTimeMinimum(), timeoutMs / 2);
			}
			
			private static long shift10(final long value)
			{
				// equals *1024, which is roughly *1000, but significantly faster and the precise factor doesn't matter.
				return value << 10;
			}
			
			private boolean isMemoryCheckEnabled()
			{
				return this.memoryQuota != memoryQuotaNoCheck();
			}

			@Override
			public final void beginCheckCycle()
			{
				this.cycleStartMs = System.currentTimeMillis();
				
				// timeout is guaranteed to be > 0.
				this.cycleTimeoutThresholdMs   = this.cycleStartMs - this.timeoutMs;
				this.cycleGraceTimeThresholdMs = this.cycleStartMs - this.graceTimeMs;

				// querying a MemoryUsage instance takes about 500 ns to query, so it is only done occasionally.
				this.updateMemoryUsage();
				this.cycleClearCount = 0;

//				this.DEBUG_printCycleState();
			}
			
			@Override
			public void endCheckCycle()
			{
				if(this.cycleEvaluator != null)
				{
					this.cycleEvaluator.evaluateCycle(this.cycleMemoryStatistics, this.cycleClearCount, this.memoryQuota);
				}
			}
			
			private void updateMemoryUsage()
			{
				this.cycleMemoryStatistics = MemoryStatisticsProvider.get().heapMemoryUsage();
				this.cycleMemoryLimit      = this.calculateMemoryLimit(this.cycleMemoryStatistics);
				this.cycleMemoryUsed       = this.cycleMemoryStatistics.used();
				
				// derived values for fast integer arithmetic for every check
				this.sh10MemoryLimit = shift10(this.cycleMemoryLimit);
				this.sh10MemoryUsed  = shift10(this.cycleMemoryUsed);
			}
			
			final void DEBUG_printCycleState()
			{
				final java.text.DecimalFormat format = new java.text.DecimalFormat("00,000,000,000");
				final VarString vs = VarString.New()
					.lf().add("Timeout          = " + this.timeoutMs                       + " ms"   )
					.lf().add("GraceTime        = " + this.graceTimeMs                     + " ms"   )
					.lf().add("memory maximum   = " + format.format(this.cycleMemoryStatistics.max()) + " bytes")
					.lf().add("memory committed = " + format.format(this.cycleMemoryStatistics.committed()) + " bytes")
					.lf().add("cycleMemoryLimit = " + format.format(this.cycleMemoryLimit) + " bytes")
					.lf().add("cycleMemoryUsed  = " + format.format(this.cycleMemoryUsed ) + " bytes")
				;
				XDebug.println(vs.toString());
			}
			
			private void registerClearing()
			{
				/*
				 * Rationale for this logic:
				 * After every clearing of a lazy reference, there is a chance that a JVM GC run will
				 * dramatically free up occupied memory. So the MemoryUsage instance would have to be updated.
				 * However, querying such an instance takes a considerable amount of time, so doint it on
				 * every clear would be a performance overkill.
				 * To ease that overhead, it is only queried every certain number of clears.
				 * Every 100th (modulo 100) would be an apropriate strategy. However, modulo 128 can be
				 * performed much, much faster by a bit operation. Hence the "% 127L" below.
				 * 
				 * Also:
				 * Updating the current memory usage has hardly any effect if the JVM GC did not run in the mean time.
				 * But calling the JVM GC explicitely and repeatedly in a generic framework logic is a very bad idea.
				 * However, there is at least the chance that the GC will be executed in between.
				 * It will definitely, eventually.
				 * And an explicit call can still be done by passing an appropriate custom check function that always
				 * returns null but calls the JVM GC every 1000th or so call.
				 */
				if((++this.cycleClearCount & 127L) == 0)
				{
					this.updateMemoryUsage();
				}
			}
			
			private boolean clear(final boolean decision)
			{
				if(decision)
				{
					this.registerClearing();
					return true;
				}
				
				return false;
			}
			
			private long calculateMemoryLimit(final MemoryStatistics memoryStatistics)
			{
				if(!this.isMemoryCheckEnabled())
				{
					return Long.MAX_VALUE;
				}
				
				// committed heap is guaranteed. Max might be unsupported or not providable by the OS.
				return (long)(memoryStatistics.committed() * this.memoryQuota);
			}

			@Override
			public final boolean check(final Lazy<?> lazyReference)
			{
				return lazyReference.clear(this);
			}
						
			private Boolean performCustomCheck(final Lazy<?> lazyReference)
			{
				return this.customCheck.test(lazyReference, this.cycleMemoryStatistics, this.timeoutMs);
			}
			
			@Override
			public final boolean needsClearing(final Lazy<?> lazyReference)
			{
				final Boolean check;
				if(this.customCheck != null && (check = this.performCustomCheck(lazyReference)) != null)
				{
					return this.clear(check.booleanValue());
				}
				// custom check is not present or was indecisive and defers to the generic logic.
				
				// simple time-based checks: never clear inside grace time, always clear beyond timeout.
				final long lastTouched = lazyReference.lastTouched();
				if(lastTouched >= this.cycleGraceTimeThresholdMs)
				{
					return false;
				}
				if(lastTouched < this.cycleTimeoutThresholdMs)
				{
//					XDebug.println("Timeout-clearing lazy " + XChars.systemString(lazyReference.peek()));
					this.registerClearing();
					return true;
				}
				
				// no simple case, so a more sophisticated check combining age and memory is required
				return this.checkByMemoryWithAgePenalty(lastTouched/*, lazyReference*/);
			}
						
			private boolean checkByMemoryWithAgePenalty(final long lastTouched/*, final Lazy<?> lazyReference*/)
			{
				// if memory check is disabled, return right away
				if(!this.isMemoryCheckEnabled())
				{
					return false;
				}
				
				final long age = this.cycleStartMs - lastTouched;
				final long sh10Weight = shift10(age) / this.timeoutMs;

				// used memory times weightSh10 is a kind of "age penalty" towards the actually used memory
				final boolean clearingDecision =
					this.sh10MemoryUsed + this.cycleMemoryUsed * sh10Weight >= this.sh10MemoryLimit
				;
					
//				this.DEBUG_printAgePenaltyInfo(lazyReference, age, sh10Weight, clearingDecision ? "CLEARED:" : "Is kept:");
				
				return this.clear(clearingDecision);
			}
			
			final void DEBUG_printAgePenaltyInfo(
				final Lazy<?> lazyReference,
				final long    age          ,
				final long    sh10Weight   ,
				final String  label
			)
			{
				final java.text.DecimalFormat format = new java.text.DecimalFormat("000,000,000,000");
				final VarString vs = VarString.New()
					.add(label)
					.add(" age = ").add(age)
					.add(" (").padLeft(Integer.toString((int)(100.0d * age / this.timeoutMs)), 2, ' ').add("%)")
					.add(": ").add(format.format(this.sh10MemoryUsed + this.cycleMemoryUsed * sh10Weight))
					.add(" <> ").add(format.format(this.sh10MemoryLimit))
					.add("  ").add(XChars.systemString(lazyReference.peek()))
				;
				XDebug.println(vs.toString());
			}

		}

		
	}

}
