package net.jadoth.swizzling.types;

import net.jadoth.functional.Dispatcher;
import net.jadoth.util.AbstractInstanceDispatcher;
import net.jadoth.util.MissingAssemblyPartException;



public interface SwizzleFoundation
{
	public Dispatcher getInstanceDispatcher(); // (14.04.2013)XXX: move dispatching aspect to separate super type


	public SwizzleObjectIdProvider getObjectIdProvider();

	public SwizzleTypeIdProvider getTypeIdProvider();


	public SwizzleFoundation setObjectIdProvider(SwizzleObjectIdProvider oidProvider);

	public SwizzleFoundation setTypeIdProvider(SwizzleTypeIdProvider tidProvider);

	public <P extends SwizzleTypeIdProvider & SwizzleObjectIdProvider>
	SwizzleFoundation setSwizzleIdProvider(P swizzleTypeIdProvider);



	public class Implementation extends AbstractInstanceDispatcher implements SwizzleFoundation
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private SwizzleObjectIdProvider oidProvider;
		private SwizzleTypeIdProvider   tidProvider;



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		protected final void internalSetOidProvider(final SwizzleObjectIdProvider oidProvider)
		{
			this.oidProvider = oidProvider;
		}

		protected final void internalSetTidProvider(final SwizzleTypeIdProvider tidProvider)
		{
			this.tidProvider = tidProvider;
		}

		///////////////////////////////////////////////////////////////////////////
		// pseudo-abstract creators //
		/////////////////////////////

		/* Explanation:
		 * These methods are not actually abstract because it is not necessaryly required
		 * to create new instances of these types. Instead, apropriate instances can be set.
		 * These methods exist in order to allow sub classes to implement them optionally
		 * and throw an exception if neither implementation nor set instance is available.
		 */

		protected SwizzleObjectIdProvider createObjectIdProvider()
		{
			throw new MissingAssemblyPartException(SwizzleObjectIdProvider.class);
		}

		protected SwizzleTypeIdProvider createTypeIdProvider()
		{
			throw new MissingAssemblyPartException(SwizzleTypeIdProvider.class);
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public Dispatcher getInstanceDispatcher()
		{
			return this.internalGetDispatcher();
		}

		@Override
		public SwizzleObjectIdProvider getObjectIdProvider()
		{
			if(this.oidProvider == null)
			{
				this.oidProvider = this.dispatch(this.createObjectIdProvider());
			}
			return this.oidProvider;
		}

		@Override
		public SwizzleTypeIdProvider getTypeIdProvider()
		{
			if(this.tidProvider == null)
			{
				this.tidProvider = this.dispatch(this.createTypeIdProvider());
			}
			return this.tidProvider;
		}

		@Override
		public SwizzleFoundation.Implementation setObjectIdProvider(final SwizzleObjectIdProvider oidProvider)
		{
			this.internalSetOidProvider(oidProvider);
			return this;
		}

		@Override
		public SwizzleFoundation.Implementation setTypeIdProvider(final SwizzleTypeIdProvider tidProvider)
		{
			this.internalSetTidProvider(tidProvider);
			return this;
		}

		@Override
		public <P extends SwizzleTypeIdProvider & SwizzleObjectIdProvider>
		SwizzleFoundation.Implementation setSwizzleIdProvider(final P swizzleTypeIdProvider)
		{
			this.internalSetOidProvider(swizzleTypeIdProvider);
			this.internalSetTidProvider(swizzleTypeIdProvider);
			return this;
		}

	}

}
