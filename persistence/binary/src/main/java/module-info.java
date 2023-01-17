/*-
 * #%L
 * microstream-persistence-binary
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
module microstream.persistence.binary
{
	exports one.microstream.persistence.binary.java.util.regex;
	exports one.microstream.persistence.binary.java.util;
	exports one.microstream.persistence.binary.java.time;
	exports one.microstream.persistence.binary.types;
	exports one.microstream.persistence.binary.java.math;
	exports one.microstream.persistence.binary.internal;
	exports one.microstream.persistence.binary.java.sql;
	exports one.microstream.persistence.binary.one.microstream.util;
	exports one.microstream.persistence.binary.exceptions;
	exports one.microstream.persistence.binary.java.lang;
	exports one.microstream.persistence.binary.one.microstream.reference;
	exports one.microstream.persistence.binary.java.io;
	exports one.microstream.persistence.binary.java.nio.file;
	exports one.microstream.persistence.binary.java.net;
	exports one.microstream.persistence.binary.java.util.concurrent;
	exports one.microstream.persistence.binary.one.microstream.entity;
	exports one.microstream.persistence.binary.one.microstream.persistence.types;
	exports one.microstream.persistence.binary.util;
	exports one.microstream.persistence.binary.one.microstream.collections;
	
	requires transitive microstream.persistence;
	requires java.sql;
	requires microstream.base; // for type handlers
}
