package one.microstream.graalvm.types;

/*-
 * #%L
 * microstream-integrations-graalvm
 * %%
 * Copyright (C) 2019 - 2021 MicroStream Software
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

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.RecomputeFieldValue;
import com.oracle.svm.core.annotate.RecomputeFieldValue.Kind;
import com.oracle.svm.core.annotate.TargetClass;

@TargetClass(className = "one.microstream.persistence.binary.one.microstream.collections.XCollectionsInternals")
public final class XCollectionsInternalsSubstitutions 
{
	@Alias @RecomputeFieldValue(kind = Kind.FieldOffset, declClassName = "one.microstream.collections.BulkList", name = "data")
    private static long OFFSET_BulkList_data;

	@Alias @RecomputeFieldValue(kind = Kind.FieldOffset, declClassName = "one.microstream.collections.BulkList", name = "size")
    private static long OFFSET_BulkList_size;

	@Alias @RecomputeFieldValue(kind = Kind.FieldOffset, declClassName = "one.microstream.collections.ConstHashEnum", name = "size")
    private static long OFFSET_ConstHashEnum_size;

	@Alias @RecomputeFieldValue(kind = Kind.FieldOffset, declClassName = "one.microstream.collections.ConstList", name = "data")
    private static long OFFSET_ConstList_data;

	@Alias @RecomputeFieldValue(kind = Kind.FieldOffset, declClassName = "one.microstream.collections.EqBulkList", name = "data")
    private static long OFFSET_EqBulkList_data;

	@Alias @RecomputeFieldValue(kind = Kind.FieldOffset, declClassName = "one.microstream.collections.EqBulkList", name = "size")
    private static long OFFSET_EqBulkList_size;

	@Alias @RecomputeFieldValue(kind = Kind.FieldOffset, declClassName = "one.microstream.collections.EqConstHashEnum", name = "size")
    private static long OFFSET_EqConstHashEnum_size;

	@Alias @RecomputeFieldValue(kind = Kind.FieldOffset, declClassName = "one.microstream.collections.EqConstHashTable", name = "size")
    private static long OFFSET_EqConstHashTable_size;

	@Alias @RecomputeFieldValue(kind = Kind.FieldOffset, declClassName = "one.microstream.collections.EqHashEnum", name = "size")
    private static long OFFSET_EqHashEnum_size;

	@Alias @RecomputeFieldValue(kind = Kind.FieldOffset, declClassName = "one.microstream.collections.EqHashTable", name = "size")
    private static long OFFSET_EqHashTable_size;

	@Alias @RecomputeFieldValue(kind = Kind.FieldOffset, declClassName = "one.microstream.collections.EqHashTable", name = "hashEqualator")
    private static long OFFSET_EqHashTable_hashEqualator;

	@Alias @RecomputeFieldValue(kind = Kind.FieldOffset, declClassName = "one.microstream.collections.EqHashTable", name = "keys")
    private static long OFFSET_EqHashTable_keys;

	@Alias @RecomputeFieldValue(kind = Kind.FieldOffset, declClassName = "one.microstream.collections.EqHashTable", name = "values")
    private static long OFFSET_EqHashTable_values;

	@Alias @RecomputeFieldValue(kind = Kind.FieldOffset, declClassName = "one.microstream.collections.FixedList", name = "data")
    private static long OFFSET_FixedList_data;

	@Alias @RecomputeFieldValue(kind = Kind.FieldOffset, declClassName = "one.microstream.collections.HashEnum", name = "size")
    private static long OFFSET_HashEnum_size;

	@Alias @RecomputeFieldValue(kind = Kind.FieldOffset, declClassName = "one.microstream.collections.EqHashTable", name = "size")
    private static long OFFSET_HashTable_size;

	@Alias @RecomputeFieldValue(kind = Kind.FieldOffset, declClassName = "one.microstream.collections.EqHashTable", name = "keys")
    private static long OFFSET_HashTable_keys;

	@Alias @RecomputeFieldValue(kind = Kind.FieldOffset, declClassName = "one.microstream.collections.EqHashTable", name = "values")
    private static long OFFSET_HashTable_values;

	@Alias @RecomputeFieldValue(kind = Kind.FieldOffset, declClassName = "one.microstream.collections.LimitList", name = "data")
    private static long OFFSET_LimitList_data;

	@Alias @RecomputeFieldValue(kind = Kind.FieldOffset, declClassName = "one.microstream.collections.LimitList", name = "size")
    private static long OFFSET_LimitList_size;

	@Alias @RecomputeFieldValue(kind = Kind.FieldOffset, declClassName = "one.microstream.collections.Singleton", name = "element")
    private static long OFFSET_Singleton_element;
}
