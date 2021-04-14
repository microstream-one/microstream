package one.microstream.storage.embedded.types;

import one.microstream.collections.ArrayView;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.exceptions.PersistenceExceptionTransfer;
import one.microstream.persistence.types.PersistenceIdSet;
import one.microstream.persistence.types.PersistenceSource;
import one.microstream.storage.types.StorageRequestAcceptor;


public interface EmbeddedStorageBinarySource extends PersistenceSource<Binary>
{
	@Override
	public XGettingCollection<? extends Binary> read() throws PersistenceExceptionTransfer;

	@Override
	public XGettingCollection<? extends Binary> readByObjectIds(PersistenceIdSet[] oids)
		throws PersistenceExceptionTransfer;



	public final class Default implements EmbeddedStorageBinarySource
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final StorageRequestAcceptor requestAcceptor;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Default(final StorageRequestAcceptor requestAcceptor)
		{
			super();
			this.requestAcceptor = requestAcceptor;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public XGettingCollection<? extends Binary> read() throws PersistenceExceptionTransfer
		{
			try
			{
				return new ArrayView<>(this.requestAcceptor.recallRoots());
			}
			catch(final InterruptedException e)
			{
				throw new PersistenceExceptionTransfer(e);
				/* Not sure if this is the best way to handle the interruption, as it swallows the interruption
				 * on the semantical level and requires call site cause analysis to recognize it.
				 *
				 * Sadly, due to the stupid checked exception concept, the interruption cannot be propagated to the
				 * calling context, not even declared to be made visible to the using developer.
				 * Being able to be validly interrupted is an implementation detail that cannot be declared in the
				 * abstract interface declaring this method. If one would "cleanly" follow the concept of
				 * checked exceptions, in the end half of all methods would have to declare countless checked exceptions
				 * that won't occur in most implementation cases (e.g. see JDBC driver methods or reflection)
				 * The misconception can also be seen easily on unchecked JDK exceptions that are actually reasonably to
				 * recover from but still are not checked exceptions because they would mess up the whoe API, like
				 * IllegalArgumentException.
				 * The only proper way is to propagate unchecked exceptions of the API level and then
				 * handle them by design (not by compiler) where necessary, using exception declaration only as a
				 * hint, not as a rule.
				 */
			}
		}

		@Override
		public XGettingCollection<? extends Binary> readByObjectIds(final PersistenceIdSet[] oids)
			throws PersistenceExceptionTransfer
		{
			try
			{
				return new ArrayView<>(this.requestAcceptor.queryByObjectIds(oids));
			}
			catch(final InterruptedException e)
			{
				throw new PersistenceExceptionTransfer(e);
				/* Not sure if this is the best way to handle the interruption, as it swallows the interruption
				 * on the semantical level and requires call site cause analysis to recognize it.
				 *
				 * Sadly, due to the stupid checked exception concept, the interruption cannot be propagated to the
				 * calling context, not even declared to be made visible to the using developer.
				 * Being able to be validly interrupted is an implementation detail that cannot be declared in the
				 * abstract interface declaring this method. If one would "cleanly" follow the concept of
				 * checked exceptions, in the end half of all methods would have to declare countless checked exceptions
				 * that won't occur in most implementation cases (e.g. see JDBC driver methods or reflection)
				 * The misconception can also be seen easily on unchecked JDK exceptions that are actually reasonably to
				 * recover from but still are not checked exceptions because they would mess up the whoe API, like
				 * IllegalArgumentException.
				 * The only proper way is to propagate unchecked exceptions of the API level and then
				 * handle them by design (not by compiler) where necessary, using exception declaration only as a
				 * hint, not as a rule.
				 */
			}
		}

//		@Override
//		public XGettingCollection<? extends Binary> readByTypeId(final long typeId) throws PersistenceExceptionTransfer
//		{
//			try
//			{
//				return new ArrayView<>(this.requestAcceptor.queryByTypeId(typeId));
//			}
//			catch(final InterruptedException e)
//			{
//				throw new PersistenceExceptionTransfer(e);
//			}
//		}

	}

}
