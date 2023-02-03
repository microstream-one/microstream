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
module microstream.base
{
	exports one.microstream.bytes;
	exports one.microstream.memory.android;
	exports one.microstream.hashing;
	exports one.microstream.util.xcsv;
	exports one.microstream.wrapping.codegen;
	exports one.microstream.reference;
	exports one.microstream.reflect;
	exports one.microstream.wrapping;
	exports one.microstream;
	exports one.microstream.typing;
	exports one.microstream.concurrency;
	exports one.microstream.functional;
	exports one.microstream.util.config;
	exports one.microstream.chars;
	exports one.microstream.collections;
	exports one.microstream.com;
	exports one.microstream.entity.codegen;
	exports one.microstream.branching;
	exports one.microstream.equality;
	exports one.microstream.entity;
	exports one.microstream.util.similarity;
	exports one.microstream.util.logging;
	exports one.microstream.util.iterables;
	exports one.microstream.collections.types;
	exports one.microstream.util.traversing;
	exports one.microstream.memory;
	exports one.microstream.io;
	exports one.microstream.util;
	exports one.microstream.collections.interfaces;
	exports one.microstream.collections.sorting;
	exports one.microstream.memory.sun;
	exports one.microstream.collections.old;
	exports one.microstream.collections.lazy;
	exports one.microstream.meta;
	exports one.microstream.exceptions;
	exports one.microstream.math;
	exports one.microstream.util.cql;
	exports one.microstream.time;
	
	provides javax.annotation.processing.Processor
	    with one.microstream.entity.codegen.EntityProcessor,
	         one.microstream.wrapping.codegen.WrapperProcessor
	;

	requires java.compiler;
	requires transitive java.management;
	requires transitive jdk.unsupported;
	requires transitive org.slf4j;
}
