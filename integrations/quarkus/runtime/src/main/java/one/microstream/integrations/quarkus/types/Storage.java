
package one.microstream.integrations.quarkus.types;

/*-
 * #%L
 * MicroStream Extension - Runtime
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


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * The Storage class in MicroStream indicates a class as a root instance.
 * Object instances can be stored as simple records.
 * One value after another as a trivial byte stream.
 * References between objects are mapped with unique numbers, called ObjectId, or short OID. + With both combined,
 * byte streams and OIDs, an object graph can be stored in a simple and quick way,
 * as well as loaded, as a whole or partially.
 * Ref: <a href="https://docs.microstream.one/manual/storage/root-instances.html">Root instances</a>
 * <p>
 * Each application must have a unique class with this annotation.
 * Note: To increase performance use immutable sub-graphs as often as possible.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface Storage {
}
