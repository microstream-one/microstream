package one.microstream.reference;

/*-
 * #%L
 * MicroStream Base
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import org.slf4j.Logger;

import one.microstream.util.logging.Logging;

/**
 * An extension of the {@link Lazy} interface that is intended to intercept
 * calls to the clear method of the lazy reference and deny unloading the lazy data if required.
 *
 * @param <T> type of the lazy loaded object
 */
public interface ControlledLazyReference<T> extends Lazy<T>
{
	public void setLazyClearController(LazyClearController lazyClearController);
	
	/**
	 * This implementation of the {@link ControlledLazyReference}
	 * lets a {@link LazyClearController} decide if the lazy reference can be cleared.
	 *
	 * @param <T> type parameter
	 */
	public final class Default<T> extends Lazy.Default<T> implements ControlledLazyReference<T>
	{
		private final static Logger logger = Logging.getLogger(ControlledLazyReference.Default.class);
				
		private LazyClearController lazyClearController;
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		/**
		* Standard constructor used by normal logic to instantiate a reference.
		*
		* @param subject the subject to be referenced.
		* @param lazyClearController the lazy reference clear controller.
		*/
		public Default(final T subject, final LazyClearController lazyClearController)
		{
			this(subject, Swizzling.toUnmappedObjectId(subject), null);
			this.lazyClearController = lazyClearController;
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
			super(subject, objectId, loader);
		}

		@Override
		public void setLazyClearController(final LazyClearController lazyClearController)
		{
			this.lazyClearController = lazyClearController;
		}
		
		@Override
		public final synchronized T clear()
		{
			if(this.lazyClearController.allowClear())
			{
				return super.clear();
			}
			
			logger.trace("Denied unloading Lazy reference {}", this);
			return this.peek();
		}
		
		@Override
		public final synchronized boolean clear(final ClearingEvaluator clearingEvaluator)
		{
			if(this.lazyClearController.allowClear())
			{
				return super.clear(clearingEvaluator);
			}
			
			logger.trace("Denied unloading Lazy reference {}", this);
			return false;
		}
	}
}
