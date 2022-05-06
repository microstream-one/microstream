
package one.microstream.integrations.cdi.types.extension;

/*-
 * #%L
 * microstream-integrations-cdi
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

import one.microstream.exceptions.NoSuchMethodRuntimeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import one.microstream.reflect.XReflect;


class ConstructorUtilTest
{
	@Test
	@DisplayName("Should return NPE when it uses")
	public void shouldReturnNPEWhenThereIsNull()
	{
		Assertions.assertThrows(NullPointerException.class, () -> XReflect.defaultInstantiate(null));
	}
	
	@Test
	public void shouldReturnErrorWhenThereIsInterface()
	{
		Assertions.assertThrows(NoSuchMethodRuntimeException.class, () -> XReflect.defaultInstantiate(Animal.class));
	}
	
	@Test
	public void shouldReturnErrorWhenThereNoDefaultConstructor()
	{
		Assertions.assertThrows(NoSuchMethodRuntimeException.class, () -> XReflect.defaultInstantiate(Lion.class));
	}
	
	@Test
	public void shouldReturnConstructor()
	{
		final Tiger tiger = XReflect.defaultInstantiate(Tiger.class);
		Assertions.assertNotNull(tiger);
	}
	
	@Test
	public void shouldCreateDefaultConstructor()
	{
		final Cat cat = XReflect.defaultInstantiate(Cat.class);
		Assertions.assertNotNull(cat);
	}
}
