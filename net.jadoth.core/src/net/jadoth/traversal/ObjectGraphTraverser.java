package net.jadoth.traversal;

import static net.jadoth.Jadoth.coalesce;
import static net.jadoth.Jadoth.notNull;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

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
		final TraversalReferenceHandlerProvider                  referenceHandlerProvider,
		final TypeTraverserProvider                              traverserProvider       ,
		final Predicate<Object>                                  handlingPredicate       ,
		final Predicate<Object>                                  handleAsFull            ,
		final Predicate<Object>                                  handleAsNode            ,
		final Predicate<Object>                                  handleAsLeaf            ,
		final TraversalAcceptor                                  traversalAcceptor       ,
		final TraversalMutator                                   traversalMutator        ,
		final MutationListener                                   mutationListener        ,
		final TraversalMode                                      traversalMode
	)
	{
		return new ObjectGraphTraverser.Implementation(
			roots                                                             ,
			coalesce(skipped, X.empty()).immure()                             ,
			coalesce(alreadyHandledProvider, s -> OpenAdressingMiniSet.New(s)),
			referenceHandlerProvider                                          ,
			notNull(traverserProvider)                                        ,
			handlingPredicate                                                 ,
			handleAsFull                                                      ,
			handleAsNode                                                      ,
			handleAsLeaf                                                      ,
			traversalAcceptor                                                 ,
			traversalMutator                                                  ,
			mutationListener                                                  ,
			traversalMode
		);
	}
	
	public final class Implementation implements ObjectGraphTraverser
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final Object[]                                           roots                   ;
		private final XGettingCollection<Object>                         skipped                 ;
		private final Function<XGettingCollection<Object>, XSet<Object>> alreadyHandledProvider  ;
		private final Predicate<Object>                                  handlingPredicate       ;
		private final Predicate<Object>                                  handleAsFull            ;
		private final Predicate<Object>                                  handleAsNode            ;
		private final Predicate<Object>                                  handleAsLeaf            ;
		private final TraversalAcceptor                                  traversalAcceptor       ;
		private final TraversalMutator                                   traversalMutator        ;
		private final TraversalReferenceHandlerProvider                  referenceHandlerProvider;
		private final TypeTraverserProvider                              traverserProvider       ;
		private final MutationListener                                   mutationListener        ;
		private final TraversalMode                                      traversalMode           ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(
			final Object[]                                           roots                   ,
			final XGettingCollection<Object>                         skipped                 ,
			final Function<XGettingCollection<Object>, XSet<Object>> alreadyHandledProvider  ,
			final TraversalReferenceHandlerProvider                  referenceHandlerProvider,
			final TypeTraverserProvider                              traverserProvider       ,
			final Predicate<Object>                                  handlingPredicate       ,
			final Predicate<Object>                                  handleAsFull            ,
			final Predicate<Object>                                  handleAsNode            ,
			final Predicate<Object>                                  handleAsLeaf            ,
			final TraversalAcceptor                                  traversalAcceptor       ,
			final TraversalMutator                                   traversalMutator        ,
			final MutationListener                                   mutationListener        ,
			final TraversalMode                                      traversalMode
		)
		{
			super();
			this.roots                    = roots                   ;
			this.skipped                  = skipped                 ;
			this.alreadyHandledProvider   = alreadyHandledProvider  ;
			this.handlingPredicate        = handlingPredicate       ;
			this.handleAsFull             = handleAsFull            ;
			this.handleAsNode             = handleAsNode            ;
			this.handleAsLeaf             = handleAsLeaf            ;
			this.traversalAcceptor        = traversalAcceptor       ;
			this.traversalMutator         = traversalMutator        ;
			this.referenceHandlerProvider = referenceHandlerProvider;
			this.traverserProvider        = traverserProvider       ;
			this.mutationListener         = mutationListener        ;
			this.traversalMode            = traversalMode           ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		protected final synchronized void internalTraverseAll(
			final Object[]          instances               ,
			final TraversalAcceptor traversalAcceptor       ,
			final TraversalMutator  traversalMutator        ,
			final MutationListener  mutationListener
		)
		{
			final AbstractReferenceHandler referenceHandler = this.referenceHandlerProvider.provideReferenceHandler(
				this.alreadyHandledProvider.apply(this.skipped),
				this.traverserProvider                         ,
				this.handlingPredicate                         ,
				this.handleAsFull                              ,
				this.handleAsNode                              ,
				this.handleAsLeaf                              ,
				traversalAcceptor                              ,
				traversalMutator                               ,
				mutationListener
			);
			this.traversalMode.handle(instances, referenceHandler);
		}
		
		
		@Override
		public void traverse()
		{
			this.internalTraverseAll(
				this.roots            ,
				this.traversalAcceptor,
				this.traversalMutator ,
				this.mutationListener
			);
		}
		
		@Override
		public <A extends TraversalAcceptor> A traverse(final A acceptor)
		{
			this.internalTraverseAll(this.roots,  acceptor, null, null);
			return acceptor;
		}
		
		@Override
		public <M extends TraversalMutator> M traverse(final M mutator)
		{
			this.internalTraverseAll(this.roots, null, mutator, null);
			return mutator;
		}
		
		@Override
		public <A extends TraversalAcceptor> A traverseAll(final Object[] instances, final A acceptor)
		{
			this.internalTraverseAll(instances, acceptor, null, null);
			return acceptor;
		}
		
		@Override
		public <M extends TraversalMutator> M traverseAll(final Object[] instances, final M mutator)
		{
			this.internalTraverseAll(instances, null, mutator, null);
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
		static final int SEGMENT_SIZE = 500;



		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////

		static final Object[] createIterationSegment()
		{
			// one trailing slot as a pointer to the next segment array. Both hacky and elegant.
			return new Object[SEGMENT_SIZE + 1];
		}
				
	}
			
}

/*
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

- handle(Predicate): ob überhaupt handeln oder nicht, also sowohl referenzen an Logik übergeben, als auch referenzen enqueuen/traversieren

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

/!\
"skip" und "leaf" ist logisch dasselbe, denn beim Referenzen durchlaufen wird eine Instanz sowieso einmal an die custom Logik (Acceptor und/oder Mutator) übergeben.
Ausfiltern danach, also im enqueue, macht eh schon das "skip"

Aber wäre nicht noch folgendes nötig?:
- Alle Referenzen einer Instanz (/ von einem Typ / von einem Typ polymorph) an die Logik geben, aber nicht enqueuen (= leaf)
- Alle Referenzen einer Instanz (/ von einem Typ / von einem Typ polymorph) nicht an die Logik geben, aber enqueuen (= node)

Evtl. auch noch Grundmodus setzen:
- Eager: alles wird als Full mit logik + enqueue angesehen, sofern nicht per leaf/node anders gesteuert.
- Lazy: alles wird als Node mit nur enqueue angesehen, sondern nicht per leaf/full anders gesteuert.
Modus is simpler boolean und lookups (node vs. full) unterscheiden sich je nach Wert dieses booleans.

Grundmodus ist wichtig, weil:
- Wenn man nur bestimmte Instanzen verarbeiten will, ist es ungeschickt, den ganzen Graph full durchzuarbeiten (-> lazy)
- Wenn man alle Instanzen im Graph als potenzielle Entities ansieht (z.B. für String Deduplizierung), dann ist es ungeschickt, sie defaultmäßig nur als nodes zu behanedln (-> eager)


Prüfung auf node:
- in handleAll():
- eager ist true und es gibt ein nodePredicate, dann prüfen. Falls zutreffend: TypeTraverser suchen und traverseReferences() mit nur enqueuer aufrufen

Prüfung auf leaf:
- in handle(Acceptor/Mutator/beide)
- TypeTraverser suchen
- eager ist egal(!) und es gibt in leafPredicate, dann prüfen. Falls zutreffend: traverseReferences ohne enqueuer aufrufen, ansonsten mit

Prüfung auf entity:
- in handleAll():
- eager ist false und es gibt ein fullPredicate, dann prüfen. Falls zutreffend: spezifisches handle(T) aufrufen, ansonsten nur die Variante mit dem Enqueuer
 

 
 
Genauer gesagt so:
- skip: gar nicht handeln (weder enqueue noch logic, vorher schon abbrechen)
- node: Referenzen handeln nur mit enqueue, nicht mit logic
- leaf: Referenzen handeln nur mit logic, nicht mit enqueue
- full: Referenzen handeln mit enqueue und logic

Macht es Sinn, im TraverserProvider auch nochmal ausschlußlogik drin zu haben?
-> Nein, denn für eine volle Unterscheidung müsste es je expliziter Traverserimplementierung mehrere geben und nur für skip allein braucht man den aufwand nicht treiben

Vereinfachen zu:
- traverseReferences: enqueuen
- handleReferences: logic
- skip: gar nicht handeln (weder enqueue noch logic, vorher schon abbrechen). Oder sollte das über den TraverserProvider gemacht werden, weil der lookup da eh drin ist?

eager mode:
- Standardmäßig enqueuen und logic
- isHandleable sagt nein, dann nur enqueuen
- isTraversable sagt nein, dann nur logic

lazy mode:
- Standardmäßig nur enqueuen
- isHandleable sagt ja, dann auch logic
- isTraversable sagt nein, dann nur logic

Was ist denn genau der Unterschied?
Kann es sein, dass der Unterschied nur ist, was das composite Predicate als default zurückgibt?
Es muss ja letztendlich immer eins geben.

Und kompletter skip über TraverserProvider durch Dummy?



||||||||||||||||||||||||||||||||||||||||||||||||
eager mode:
- prüft node und leaf
- ignoriert full, weil Defaultverhalten

lazy mode:
- prüft full und leaf
- ignoriert node, weil Defaultverhalten


ODER

nur ein mode:
- prüft traverseReferences
- prüft handleReferences
- je nach ergebnis skip, node, leaf oder full


Zweite Variante ist besser, denn:
- Macht auch immer nur zwei lookups
- in der ersten Variante widersprechen sich node und leaf. In der zweiten ist das nicht der Fall

Zweite Variante hat aber auch Nachteile:
- Muss mode als Default in das composite predicate stecken, anstatt einmalige Unterscheidung zu machen
- braucht für simple Instanz- und Typangaben positiv- und negativlogik: eager + NICHT traversen und lazy + SCHON traversen
- Nogo: damit müsste man sich "leaf" Logik selbst bauen: Positiveintrag in handleReferences und Negativeintrag in traverseReferences


Oh Mann!!!

Also doch erste Variante?
Widerspruch könnte in builder gelöst werden: Registrierung für leaf wirft aus node und full raus, usw.

Problem mit skip handling über TraverserProvider ist: Es soll auch ein custom isHandleable Predicate geben, das müsste trotzdem erhalten bleiben.
Außerdem: Exception bei fehlendem Traverser vermeidet unbeabsichtigte Fehler: Wenn was geskippt werden soll, muss das explizit als skipped registriert werden

||||||||||||||||||||||||||||||||||||||||||||||||




Braucht man vielleicht drei modes?
Full: prüft node und leaf
Node: prüft full und leaf
Leaf: prüft full und node

Skip ist kein eigener Mode, denn sonst müsste das enqueuing drei predicates abprüfen.
Außerdem wär es etwas dämlich, einen Traverser zu haben, der als Defaultverhalten gar nichts macht.

Genauer gesagt Mode wird ein interface und die Implementierung davon entscheidet, welche ReferenceHandler Methode aufgerufen werden soll (handleFull/Node/Leaf)

 
 

Wird im TraversalHandlerProvider verwendet:
- registerHandler(Object, TraversalHandler)
- registerHandlerForType(Class<?>, TraversalHandler)
- registerHandlerPolymorphic(Class<?>, TraversalHandler)


Nötige Signals:
- TraversalSignalSkipInstance
- TraversalSignalAbort
*/