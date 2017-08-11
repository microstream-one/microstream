package net.jadoth.traversal;

import static net.jadoth.Jadoth.coalesce;
import static net.jadoth.Jadoth.notNull;

import java.util.function.Consumer;
import java.util.function.Function;

import net.jadoth.Jadoth;
import net.jadoth.collections.X;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.collections.types.XSet;


public interface ObjectGraphTraverser
{
	public void traverse();
	
	public default void traverse(final Object instance)
	{
		this.traverseAll(Jadoth.array(instance));
	}
	
	public default void traverseAll(final Object[] instances)
	{
		throw new RuntimeException("No traversal logic specified"); // (17.07.2017 TM)EXCP: proper exception
	}
	
	public <A extends TraversalAcceptor> A traverse(A acceptor);
	
	public <M extends TraversalMutator> M traverse(M acceptor);

	public default <A extends TraversalAcceptor> A traverse(final Object instance, final A acceptor)
	{
		this.traverseAll(Jadoth.array(instance), acceptor);
		return acceptor;
	}
	
	public default <C extends Consumer<Object>> C traverse(final Object instance, final C logic)
	{
		this.traverse(instance, TraversalAcceptor.New(logic));
		return logic;
	}
	
	public default <M extends TraversalMutator> M traverse(final Object instance, final M acceptor)
	{
		this.traverseAll(Jadoth.array(instance), acceptor);
		return acceptor;
	}
	
	public default <F extends Function<Object, Object>> F traverse(final Object instance, final F logic)
	{
		this.traverse(instance, TraversalMutator.New(logic));
		return logic;
	}


	public <A extends TraversalAcceptor> A traverseAll(Object[] instances, A acceptor);
	
	public <M extends TraversalMutator> M traverseAll(Object[] instances, M mutator);
	
	
	public static void signalAbortTraversal() throws TraversalSignalAbort
	{
		TraversalSignalAbort.fire();
	}

	
	public static ObjectGraphTraverserBuilder Builder()
	{
		return new ObjectGraphTraverserBuilder.Implementation();
	}
	
	public static ObjectGraphTraverser New(
		final Object[]                                           roots                   ,
		final XGettingCollection<Object>                         skipped                 ,
		final Function<XGettingCollection<Object>, XSet<Object>> alreadyHandledProvider  ,
		final TypeTraverserProvider                              traverserProvider       ,
		final MutationListener.Provider                          mutationListenerProvider
	)
	{
		return new ObjectGraphTraverser.Implementation(
			roots                                                             ,
			coalesce(skipped, X.empty()).immure()                             ,
			coalesce(alreadyHandledProvider, s -> OpenAdressingMiniSet.New(s)),
			notNull(traverserProvider)                                        ,
			mutationListenerProvider
		);
	}
	
	public static ObjectGraphTraverser New(
		final Object[]                                           roots                   ,
		final XGettingCollection<Object>                         skipped                 ,
		final Function<XGettingCollection<Object>, XSet<Object>> alreadyHandledProvider  ,
		final TypeTraverserProvider                              traverserProvider       ,
		final MutationListener.Provider                          mutationListenerProvider,
		final TraversalAcceptor                                  acceptor
	)
	{
		return new ImplementationAccepting(
			roots                                                             ,
			coalesce(skipped, X.empty()).immure()                             ,
			coalesce(alreadyHandledProvider, s -> OpenAdressingMiniSet.New(s)),
			notNull(traverserProvider)                                        ,
			mutationListenerProvider                                          , // may be null
			acceptor
		);
	}
	
	public static ObjectGraphTraverser New(
		final Object[]                                           roots                   ,
		final XGettingCollection<Object>                         skipped                 ,
		final Function<XGettingCollection<Object>, XSet<Object>> alreadyHandledProvider  ,
		final TypeTraverserProvider                              traverserProvider       ,
		final MutationListener.Provider                          mutationListenerProvider,
		final TraversalMutator                                   mutator
	)
	{
		return new ImplementationMutating(
			roots                                                             ,
			coalesce(skipped, X.empty()).immure()                             ,
			coalesce(alreadyHandledProvider, s -> OpenAdressingMiniSet.New(s)),
			notNull(traverserProvider)                                        ,
			mutationListenerProvider                                          , // may be null
			mutator
		);
	}
	
	public class Implementation implements ObjectGraphTraverser
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final Object[]                                           roots                   ;
		private final XGettingCollection<Object>                         skipped                 ;
		private final Function<XGettingCollection<Object>, XSet<Object>> alreadyHandledProvider  ;
		private final TypeTraverserProvider                              traverserProvider       ;
		private final MutationListener.Provider                          mutationListenerProvider;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(
			final Object[]                                           roots                   ,
			final XGettingCollection<Object>                         skipped                 ,
			final Function<XGettingCollection<Object>, XSet<Object>> alreadyHandledProvider  ,
			final TypeTraverserProvider                              traverserProvider       ,
			final MutationListener.Provider                          mutationListenerProvider
		)
		{
			super();
			this.roots                    = roots                   ;
			this.skipped                  = skipped                 ;
			this.alreadyHandledProvider   = alreadyHandledProvider  ;
			this.traverserProvider        = traverserProvider       ;
			this.mutationListenerProvider = mutationListenerProvider;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		protected final synchronized void internalTraverseAllAccepting(
			final Object[]          instances,
			final TraversalAcceptor acceptor
		)
		{
			notNull(acceptor);
			
			final ReferenceHandlerAccepting referenceHandler = new ReferenceHandlerAccepting(
				this.traverserProvider,
				this.mutationListenerProvider,
				this.alreadyHandledProvider.apply(this.skipped),
				instances
			);

			try
			{
				while(true)
				{
					referenceHandler.handleNext(acceptor);
				}
			}
			catch(final TraversalSignalAbort s)
			{
				// some logic signaled to abort the traversal. So abort and fall through to returning.
				return;
			}
		}
		
		protected final synchronized void internalTraverseAllMutating(
			final Object[]         instances,
			final TraversalMutator mutator
		)
		{
			notNull(mutator);
			
			final ReferenceHandlerMutating referenceHandler = new ReferenceHandlerMutating(
				this.traverserProvider,
				this.mutationListenerProvider,
				this.alreadyHandledProvider.apply(this.skipped),
				instances
			);

			try
			{
				while(true)
				{
					referenceHandler.handleNext(mutator);
				}
			}
			catch(final TraversalSignalAbort s)
			{
				// some logic signaled to abort the traversal. So abort and fall through to returning.
				return;
			}
		}
		
		@Override
		public void traverse()
		{
			this.traverseAll(this.roots);
		}
		
		@Override
		public <A extends TraversalAcceptor> A traverse(final A acceptor)
		{
			this.internalTraverseAllAccepting(this.roots, acceptor);
			return acceptor;
		}
		
		@Override
		public <M extends TraversalMutator> M traverse(final M mutator)
		{
			this.internalTraverseAllMutating(this.roots, mutator);
			return mutator;
		}
		
		@Override
		public <A extends TraversalAcceptor> A traverseAll(final Object[] instances, final A acceptor)
		{
			this.internalTraverseAllAccepting(instances, acceptor);
			return acceptor;
		}
		
		@Override
		public <M extends TraversalMutator> M traverseAll(final Object[] instances, final M mutator)
		{
			this.internalTraverseAllMutating(instances, mutator);
			return mutator;
		}
		
		/**
		 * Must be a multiple of 2.
		 *
		 * Surprisingly, the exact value here doesn't matter much.
		 * The initial idea was to replace hundreds of Entry instances with one array of about one cache page size
		 * (~500 references assuming 4096 page size and no coops minus object header etc.).
		 * However, tests with graphs from 1000 to ~30 million handled instances showed:
		 * - the segment structure is only measurably faster for really big graphs (8 digit instance count)
		 * - a segment count of 100 is equally fast as 10000. Only unreasonably tiny sizes like <= 8 are slower.
		 *
		 * However, a slight performance gain is still better than none. Plus there is much less memory used
		 * for object header and chain-reference overhead.
		 */
		private static final int SEGMENT_SIZE = 500;



		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////

		static final Object[] createIterationSegment()
		{
			// one trailing slot as a pointer to the next segment array. Both hacky and elegant.
			return new Object[SEGMENT_SIZE + 1];
		}
		
		
		static abstract class AbstractReferenceHandler implements TraversalEnqueuer
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			final TypeTraverserProvider     traverserProvider;
			final MutationListener          mutationListener ;
			final XSet<Object>              alreadyHandled   ;

			Object[] iterationTail      = createIterationSegment();
			Object[] iterationHead      = this.iterationTail;
			boolean  tailIsHead         = true;
			int      iterationTailIndex;
			int      iterationHeadIndex;
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			AbstractReferenceHandler(
				final TypeTraverserProvider     traverserProvider       ,
				final MutationListener.Provider mutationListenerProvider,
				final XSet<Object>              alreadyHandled          ,
				final Object[]                  instances
			)
			{
				super();
				this.traverserProvider = traverserProvider;
				this.alreadyHandled    = alreadyHandled   ;
				this.mutationListener  = mutationListenerProvider != null
					? mutationListenerProvider.provideMutationListener(this)
					: null
				;

				for(final Object instance : instances)
				{
					this.enqueue(instance);
				}
			}
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////
			
			@Override
			public final boolean skip(final Object instance)
			{
				return this.alreadyHandled.add(instance);
			}
			
			final void increaseIterationQueue()
			{
				final Object[] nextIterationSegment = createIterationSegment();
				this.iterationHead[SEGMENT_SIZE]    = nextIterationSegment    ;
				this.iterationHead                  = nextIterationSegment    ;
				this.iterationHeadIndex             = 0                       ;
				this.tailIsHead                     = false                   ;
			}
			
			@Override
			public final void enqueue(final Object instance)
			{
				// must check for null as there is no control over what custom handler implementations might pass
				if(instance == null)
				{
					return;
				}
				
				if(!this.alreadyHandled.add(instance))
				{
					return;
				}
				
				/* this check causes a redundant lookup in the handler registry: one here, one later to
				 * actually get the handler.
				 * Nevertheless, this is considered the favorable strategy, as the alternatives would be:
				 * - no check at all, meaning to potentially enqueuing millions of leaf type instances,
				 *   bloating the queue, maybe even causing out of memory problems
				 * - co-enqueuing the handler, but at the price of doubled queue size, complicated dequeueing logic and
				 *   compromised type safety (every second item is actually a handler instance hacked into the queue)
				 * So in the end, it seems best to accept a slight performance overhead but keep the queue as
				 * small as possible.
				 * Other implementations can take different approaches to optimize runtime behavior to suit their needs.
				 */
				if(this.traverserProvider.isUnhandled(instance))
				{
					return;
				}
								
				if(this.iterationHeadIndex >= SEGMENT_SIZE)
				{
					this.increaseIterationQueue();
				}
				this.iterationHead[this.iterationHeadIndex++] = instance;
			}
						
			@SuppressWarnings("unchecked")
			final <T> T dequeue()
			{
				// (25.06.2017 TM)TODO: test performance of outsourced private methods
				if(this.tailIsHead)
				{
					this.checkForCompletion();
				}
				if(this.iterationTailIndex >= SEGMENT_SIZE)
				{
					this.advanceSegment();
				}
				
				return (T)this.iterationTail[this.iterationTailIndex++];
			}
			
			final void checkForCompletion()
			{
				if(this.iterationTailIndex >= this.iterationHeadIndex)
				{
					ObjectGraphTraverser.signalAbortTraversal();
				}
			}
			
			final void advanceSegment()
			{
				this.iterationTail      = (Object[])this.iterationTail[SEGMENT_SIZE];
				this.iterationTailIndex = 0;
				this.tailIsHead         = this.iterationTail == this.iterationHead;
			}
		}
		
		static final class ReferenceHandlerAccepting extends AbstractReferenceHandler
		{
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			ReferenceHandlerAccepting(
				final TypeTraverserProvider     traverserProvider       ,
				final MutationListener.Provider mutationListenerProvider,
				final XSet<Object>              alreadyHandled          ,
				final Object[]                  instances
			)
			{
				super(traverserProvider, mutationListenerProvider, alreadyHandled, instances);
			}
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////
												
			final <T> void handleNext(final TraversalAcceptor acceptor) throws TraversalSignalAbort
			{
				final T                instance  = this.dequeue();
				final TypeTraverser<T> traverser = this.traverserProvider.provide(instance);
				
//				JadothConsole.debugln("Traversing " + Jadoth.systemString(instance) + " via " + Jadoth.systemString(handler));
				traverser.traverseReferences(instance, acceptor, this);
			}
			
		}
		
		static final class ReferenceHandlerMutating extends AbstractReferenceHandler
		{
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			ReferenceHandlerMutating(
				final TypeTraverserProvider     traverserProvider       ,
				final MutationListener.Provider mutationListenerProvider,
				final XSet<Object>              alreadyHandled          ,
				final Object[]                  instances
			)
			{
				super(traverserProvider, mutationListenerProvider, alreadyHandled, instances);
			}
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////
												
			final <T> void handleNext(final TraversalMutator mutator) throws TraversalSignalAbort
			{
				final T                instance  = this.dequeue();
				final TypeTraverser<T> traverser = this.traverserProvider.provide(instance);
				
//				JadothConsole.debugln("Traversing " + Jadoth.systemString(instance) + " via " + Jadoth.systemString(handler));
				traverser.traverseReferences(instance, mutator, this, this.mutationListener);
			}
			
		}
				
	}
	
	
	public final class ImplementationAccepting extends ObjectGraphTraverser.Implementation
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final TraversalAcceptor acceptor;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		ImplementationAccepting(
			final Object[]                                           roots                   ,
			final XGettingCollection<Object>                         skipped                 ,
			final Function<XGettingCollection<Object>, XSet<Object>> alreadyHandledProvider  ,
			final TypeTraverserProvider                              traverserProvider       ,
			final MutationListener.Provider                          mutationListenerProvider,
			final TraversalAcceptor                                  acceptor
		)
		{
			super(roots, skipped, alreadyHandledProvider, traverserProvider, mutationListenerProvider);
			this.acceptor = acceptor;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public void traverseAll(final Object[] instances)
		{
			this.internalTraverseAllAccepting(instances, this.acceptor);
		}
		
	}
	
	
	
	public final class ImplementationMutating extends ObjectGraphTraverser.Implementation
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final TraversalMutator mutator;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		ImplementationMutating(
			final Object[]                                           roots                   ,
			final XGettingCollection<Object>                         skipped                 ,
			final Function<XGettingCollection<Object>, XSet<Object>> alreadyHandledProvider  ,
			final TypeTraverserProvider                              traverserProvider       ,
			final MutationListener.Provider                          mutationListenerProvider,
			final TraversalMutator                                   mutator
		)
		{
			super(roots, skipped, alreadyHandledProvider, traverserProvider, mutationListenerProvider);
			this.mutator = mutator;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public void traverseAll(final Object[] instances)
		{
			this.internalTraverseAllMutating(instances, this.mutator);
		}
		
	}
	
}



/* (11.08.2017 TM)NOTE:

1.) Instanzen sollen standardmäßig gehandelt und traversiert werden
2.) Manche Instanzen sollen nur gehandelt, aber nicht traversiert werden ("leaf instances" und "leaf types")
3.) Eine Instanz, die traversiert wird, soll immer auch gehandelt werden. Ausnahme: unshared Instanzen, gesteuert durch explizite Traverser.
4.) Manche Instanzen sollen weder gehandelt noch traversiert werden.
    Aus unterschiedlichen Gründen:
	- alreadyHandled
	- per Filterbedingung (komplexe externe Logik, ggf. abhängig von bisheriger Traversierung)
	- aufgrund eines skips (instance, type, polymorphic)

Es muss zwei Arten von Filterbedingung geben:
1.) Überhaupt nicht handeln und damit auch nicht traversen ("handle()")
2.) Zwar nicht an die Acceptor Logik übergeben, aber trotzdem traversen. ("select()")


aktuell:
- dequeue instance
- traverser lookup für instance
- traverseReferences der instance
  - reference accepten
  - reference enqueuen
    - alreadyHandled check
	- isUnhandled check (traverser blind-lookup)
	

	
Geplant:

enqueue macht die internen handling checks machen (alreadyHandled und skip type lookups)

Zu Beginn des Traversierens werden alle root instanzen ganz normal enqueue()t.

Dann in Schleife:

- dequeue()
- optionales custom handle() predicate. Falls man eine custom vorab-logik ausführen lassen will (z.B. logging), kann man die in ein predicate stecken und true zurückgeben

Wegen diesem predicate ist es sinnvoll, instanzen zu enqueuen, auch wenn es keinen handler dafür gibt. Der handling check findet im enqueue ja statt.

- handler lookup
- falls kein handler gefunden, abrechen und mit nächstem dequeue() weitermachen
- falls handler gefunden, verschiedene möglichkeiten:
  1.) nur traversieren (iterieren und referenzen enqueuen)
  2.) iterieren und dabei acceptor logik auf referenz anwenden und dann enqueuen
  3.) iterieren und dabei mutator logik auf referenz anwenden und dann enqueuen
  4.) iterieren und nur acceptor logik auf referenz anwenden
  5.) iterieren und nur mutator logik auf referenz anwenden

Sind ein bisschen viele varianten.
Vielleicht so besser:
- Predicate gibt an, ob enqueuet werden soll
- Falls acceptor vorhanden, dann acceptor anwenden
- Falls mutator vorhanden, dann mutator anwenden

Falls eine instanz gar nicht gehandelt werden soll, kann das predicate ein AbortInstance Signal werfen.

Das heißt aber trotzdem, dass jeder Handler 7 verschiedene Methoden implementieren müsste
1.) enq
2.) eng + acc
3.) enq + mut
4.) enq + acc + mut
5.) acc
6.) mut
7.) acc + mut

Jede Variante mit mut hat auch einen MutationListener.

Der Acceptor und Mutator müssen niemals enqueuen, weil das aufgrund ihrer rückgabewerte entschieden wird

Aufrufreihenfolge:
- if(acc) enqueue
- mut -> enqueue rückgabewert


Acc gibt einen boolean zurück, ob die aktuelle referenz enqueuet werden soll. Dadurch kann das als mechanik zum enqueuen des vorherigen werts verwendet werden.
Außerdem könnte so ein acc noch feiner steuern, welche referenzen überhaupt enqueuet werden sollen.
Das einzige Steuerproblem wäre dann noch, zu verhindern, dass ein vom Mut zurückgegebener Wert enqueuet wird, falls man das möchte.

Das könnte gelöst werden mit:
Zurückgegebene Referenz wird trotzdem enqueuet, aber der Mut steckt die in ein Set rein und das handling predicate checkt dieses Set und wirft bei bedarf ein skipInstance Signal.
Ist ein bisschen workaroundig, aber ist akzeptabel dafür, dass der normalfall dafür einfach und performant bleibt.



Wenn predicate null ist, dann per default traversieren.
Auch dann, wenn acc und mut null sind, denn vielleicht ist ja ein custom handler registriert, der wichtige Logik ausführen soll.

Es gibt also nur eine top-level traverse() Methode (Methodenfamilie) und es kommt darauf an, ob handling predicate, acc und mut gesetzt sind, um zu bestimmen, was der Traverser tatsächlich ausführt.


Nötige Registriermethoden:

- TraversableFieldSelector: welche fields bei der generischen reflection analyse berücksichtigt werden sollen.

- handle(Predicate): ob überhaupt handeln oder nicht

- select(Predicate): Komponente für synthetischen Acceptor oder Mutator: ob externe Logik die Instanz bekommen soll

- apply(Consumer): Komponente für synthetischen Acceptor: externe Logik, mit der eine Instanz gehandelt werden soll
- apply(Predicate, Consumer): beiden Komponente für synthetischen Acceptor spezifisch
- apply(TraversalAcceptor): Acceptor spezifisch

- mutateBy(Function): Komponente für synthetischen Mutator: externe Logik, die eine Referenz durch eine andere ersetzt
- mutateBy(Predicate, Function): beiden Komponente für synthetischen Mutator spezifisch
- mutateBy(TraversalMutator): Mutator spezifisch

Wird in enqueue geprüft, falls vorhanden:
- skip(Object): Instanz nicht handeln
- skipType(Class<?>): alle Instanzen nicht handeln von genau diesem Typ
- skipPolymorphic(Class<?>): alle Instanzen nicht handeln, deren typ ein Subtyp des übergebenen Typs ist

Wird, falls vorhanden, in handling predicate geprüft und erst danach an externes predicate weiterdelegiert
- leaf(Object): Instanz handeln, aber nicht traversieren
- leafType(Class<?>): alle Instanzen von genau diesem Typ handeln, aber nicht traversieren
- leafPolymorphic(Class<?>): alle Instanzen, deren Typ ein Subtyp des übergebenen Typs ist, handeln, aber nicht traversieren

Wird im TraversalHandlerProvider verwendet:
- registerHandler(Object, TraversalHandler)
- registerHandlerForType(Class<?>, TraversalHandler)
- registerHandlerPolymorphic(Class<?>, TraversalHandler)


Nötige Signals:
- TraversalSignalSkipInstance
- TraversalSignalAbort
*/