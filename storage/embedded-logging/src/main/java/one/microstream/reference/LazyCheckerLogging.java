package one.microstream.reference;

import static one.microstream.X.notNull;

import one.microstream.reference.Lazy.Checker;
import one.microstream.storage.types.StorageLoggingWrapper;

public interface LazyCheckerLogging extends Checker, StorageLoggingWrapper<Checker>
{
	public static LazyCheckerLogging New(final Checker wrapped)
	{
		return new Default(notNull(wrapped));
	}
	
	public static class Default
	    extends StorageLoggingWrapper.Abstract<Checker>
		implements LazyCheckerLogging
	{
		Default(final Checker wrapped)
		{
			super(wrapped);
		}

		@Override
		public void beginCheckCycle()
		{
			this.logger().lazyChecker_beginCheckCycle();
			
			this.wrapped().beginCheckCycle();
		}

		@Override
		public boolean check(final Lazy<?> lazyReference)
		{
			this.logger().lazyChecker_beginCheck(lazyReference);
			
			final boolean checkResult = this.wrapped().check(lazyReference);
			
			this.logger().lazyChecker_afterCheck(lazyReference, checkResult);
			
			return checkResult;
		}

		@Override
		public void endCheckCycle()
		{
			this.logger().lazyChecker_endCheckCycle();
			
			this.wrapped().endCheckCycle();
		}
	}
}
