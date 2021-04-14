package one.microstream.collections.types;

import java.util.function.Consumer;


public interface XIterable<E>
{
	public <P extends Consumer<? super E>> P iterate(P procedure);

	

	/**
	 * Wrapper class that implements {@link XIterable} to wrap a subject of type E that procedures shall be
	 * executed on.
	 * <p>
	 * By using an executor instance, an instance not implementing {@link XIterable} can be passed to a context
	 * expecting an {@link XIterable} instance. Through this abstraction, logic can be written that can be
	 * equally executed on single objects (via this wrapper) or multiple objects (via X-collections).
	 * <p>
	 * <u>Example</u>:<code><pre> someRegistryLogic.register(persons);
	 * someRegistryLogic.register(new Exector<Person>(singlePerson));
	 * </pre></code>
	 *
	 * 
	 */
	public final class Executor<E> implements XIterable<E>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final E subject;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Executor(final E subject)
		{
			super();
			this.subject = subject;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public <P extends Consumer<? super E>> P iterate(final P procedure)
		{
			procedure.accept(this.subject);
			return procedure;
		}

	}

}
