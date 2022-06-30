package one.microstream.integrations.cdi.types;

/*-
 * #%L
 * MicroStream Integrations CDI
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

/**
 * Definition of the CDI bean that is capable of marking instances as dirty. The method
 * signature of the `mark` method is designed to allow a fluent API alike experience.
 *
 * Examples <p/>
 * <code>
 * @Inject DirtyMarker dirtyMarker;
 *
 * @Inject DataRoot root;
 *
 * @Store
 * public void doSomething() {
 *     ...
 *     dirtyMarker.mark(root.getProducts()).add(product);
 *     ...
 *     dirtyMarker.mark(product).setName(newName);
 * }
 * </code>
 */
public interface DirtyMarker
{

    <T> T mark(T instance);
}
