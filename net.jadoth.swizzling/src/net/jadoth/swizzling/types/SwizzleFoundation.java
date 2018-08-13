package net.jadoth.swizzling.types;

import net.jadoth.exceptions.MissingFoundationPartException;
import net.jadoth.functional.InstanceDispatcherLogic;
import net.jadoth.util.InstanceDispatcher;



public interface SwizzleFoundation
{
	public InstanceDispatcherLogic getInstanceDispatcherLogic(); // (14.04.2013)XXX: move dispatching aspect to separate super type


	public SwizzleObjectIdProvider getObjectIdProvider();

	public SwizzleTypeIdProvider getTypeIdProvider();


	public SwizzleFoundation setObjectIdProvider(SwizzleObjectIdProvider oidProvider);

	public SwizzleFoundation setTypeIdProvider(SwizzleTypeIdProvider tidProvider);

	public <P extends SwizzleTypeIdProvider & SwizzleObjectIdProvider>
	SwizzleFoundation setSwizzleIdProvider(P swizzleTypeIdProvider);



	public class Implementation extends InstanceDispatcher.Implementation implements SwizzleFoundation
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private SwizzleObjectIdProvider oidProvider;
		private SwizzleTypeIdProvider   tidProvider;


		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		/* Explanation:
		 * These methods are not actually abstract because it is not necessaryly required
		 * to create new instances of these types. Instead, apropriate instances can be set.
		 * These methods exist in order to allow sub classes to implement them optionally
		 * and throw an exception if neither implementation nor set instance is available.
		 */

		protected SwizzleObjectIdProvider createObjectIdProvider()
		{
			throw new MissingFoundationPartException(SwizzleObjectIdProvider.class);
		}

		protected SwizzleTypeIdProvider createTypeIdProvider()
		{
			throw new MissingFoundationPartException(SwizzleTypeIdProvider.class);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public InstanceDispatcherLogic getInstanceDispatcherLogic()
		{
			return this.getInstanceDispatcherLogic();
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
			this.oidProvider = oidProvider;
			return this;
		}

		@Override
		public SwizzleFoundation.Implementation setTypeIdProvider(final SwizzleTypeIdProvider tidProvider)
		{
			this.tidProvider = tidProvider;
			return this;
		}

		@Override
		public <P extends SwizzleTypeIdProvider & SwizzleObjectIdProvider>
		SwizzleFoundation.Implementation setSwizzleIdProvider(final P swizzleTypeIdProvider)
		{
			this.setObjectIdProvider(swizzleTypeIdProvider);
			this.setTypeIdProvider(swizzleTypeIdProvider);
			return this;
		}

	}

}
