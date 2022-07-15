package one.microstream.chars;

/*-
 * #%L
 * microstream-base
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


@FunctionalInterface
public interface ObjectStringAssembler<T>
{
	public VarString assemble(VarString vs, T subject);
	
	public default VarString provideAssemblyBuffer()
	{
		// cannot make any assumptions about the required capacity in a generic implementation.
		return VarString.New();
	}
	
	public default String assemble(final T subject)
	{
		final VarString vs = this.provideAssemblyBuffer();
		
		this.assemble(vs, subject);
		
		return vs.toString();
	}
	
}
