package one.microstream.experimental.binaryread.data;

/*-
 * #%L
 * binary-read
 * %%
 * Copyright (C) 2019 - 2023 MicroStream Software
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

import one.microstream.experimental.binaryread.structure.Entity;
import one.microstream.experimental.binaryread.structure.EntityMember;
import one.microstream.experimental.binaryread.structure.Storage;

import java.util.List;
import java.util.stream.Collectors;

public class DataStorageEnumArrayDeserializer implements DataStorageDeserializer
{

    private final Storage storage;

    public DataStorageEnumArrayDeserializer(final Storage storage)
    {
        this.storage = storage;
    }


    @Override
    public ConvertedData resolve(final EntityMember entityMember)
    {
        return resolve(entityMember.getReader()
                               .read(), DeserializerOptions.EMPTY);
    }

    @Override
    public ConvertedData resolve(final Long reference, final DeserializerOptions options)
    {
        Entity enumArrayEntity = storage.getEntityByObjectId(reference);
        if (enumArrayEntity == null)
        {
            return new ConvertedData(null);
        }
        else
        {
            final List<Long> enumRefs = enumArrayEntity.getMembers()
                    .get(0)
                    .getReader()
                    .read();
            return new ConvertedData(enumRefs.stream()
                                             .map(ref -> storage.getEnumValue(ref))
                                             .collect(Collectors.toList()));
        }
    }
}
