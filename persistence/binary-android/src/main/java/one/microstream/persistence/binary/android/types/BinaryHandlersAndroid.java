package one.microstream.persistence.binary.android.types;

/*-
 * #%L
 * microstream-persistence-binary-android
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

import one.microstream.X;
import one.microstream.persistence.binary.android.java.time.BinaryHandlerDuration;
import one.microstream.persistence.binary.android.java.time.BinaryHandlerInstant;
import one.microstream.persistence.binary.android.java.time.BinaryHandlerLocalDate;
import one.microstream.persistence.binary.android.java.time.BinaryHandlerLocalDateTime;
import one.microstream.persistence.binary.android.java.time.BinaryHandlerLocalTime;
import one.microstream.persistence.binary.android.java.time.BinaryHandlerMonthDay;
import one.microstream.persistence.binary.android.java.time.BinaryHandlerOffsetDateTime;
import one.microstream.persistence.binary.android.java.time.BinaryHandlerOffsetTime;
import one.microstream.persistence.binary.android.java.time.BinaryHandlerPeriod;
import one.microstream.persistence.binary.android.java.time.BinaryHandlerYear;
import one.microstream.persistence.binary.android.java.time.BinaryHandlerYearMonth;
import one.microstream.persistence.binary.android.java.time.BinaryHandlerZonedDateTime;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceTypeHandlerRegistration;

/**
 * Registeres special type handlers written for Android.
 * Some of them have to sacrifice referencial integrity for funcionality.
 * <p>
 * See <a href="https://github.com/microstream-one/microstream/issues/245#issuecomment-921660371">Issue</a> for further details.
 */
public final class BinaryHandlersAndroid
{
	public static <F extends PersistenceTypeHandlerRegistration.Executor<Binary>> F registerAndroidTypeHandlers(final F executor)
	{
		executor.executeTypeHandlerRegistration((r, c) ->
			r.registerTypeHandlers(X.List(
				BinaryHandlerDuration.New(),
				BinaryHandlerInstant.New(),
				BinaryHandlerLocalDate.New(),
				BinaryHandlerLocalTime.New(),
				BinaryHandlerLocalDateTime.New(),
				BinaryHandlerMonthDay.New(),
				BinaryHandlerOffsetTime.New(),
				BinaryHandlerOffsetDateTime.New(),
				BinaryHandlerPeriod.New(),
				BinaryHandlerYear.New(),
				BinaryHandlerYearMonth.New(),
				BinaryHandlerZonedDateTime.New()
			))
		);
		
		return executor;
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 *
	 * @throws UnsupportedOperationException when called
	 */
	protected BinaryHandlersAndroid()
	{
		// static only
		throw new UnsupportedOperationException();
	}
	
}
