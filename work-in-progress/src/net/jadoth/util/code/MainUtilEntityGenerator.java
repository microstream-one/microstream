package net.jadoth.util.code;

public class MainUtilEntityGenerator extends Code
{
	public static void main(final String[] args)
	{
		System.out.println(generateEntity(
			"ObjectGraphTraverserBuilder"                                                           ,
//			null,
//			"is",
//			"get",
//			"set",
			Final("HashTable<Object, TypeTraverser<?>>",                "traversersPerInstance"     ),
			Final("HashTable<Class<?>, TypeTraverser<?>>",              "traversersPerConcreteType" ),
			Final("HashTable<Class<?>, TypeTraverser<?>>",              "traversersPerPolymorphType"),
			Field("Predicate<? super Field>",                           "traversableFieldSelector"  ),
			Field("Function<XGettingCollection<Object>, XSet<Object>>", "alreadyHandledProvider"    ),
			Field("TypeTraverser.Creator",                              "typeTraverserCreator"      ),
			Field("TraversalAcceptor",                                  "acceptor"                  ),
			Field("Predicate<Object>",                                  "acceptorPredicate"         ),
			Field("Consumer<Object>",                                   "acceptorLogic"             ),
			Field("TraversalMutator",                                   "mutator"                   ),
			Field("Predicate<Object>",                                  "mutatorPredicate"          ),
			Field("Function<Object, Object>",                           "mutatorLogic"              ),
			Field("MutationListener",                                   "mutationListener"          ),
			Field("TraversalMode",                                      "traversalMode"             ),
			Field("TraversalReferenceHandlerProvider",                  "referenceHandlerProvider"  ),
			Field("Object[]",                                           "roots"                     )
		));
	}
}
