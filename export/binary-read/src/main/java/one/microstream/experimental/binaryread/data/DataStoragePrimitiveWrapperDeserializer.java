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

import one.microstream.experimental.binaryread.exception.InvalidObjectIdFoundException;
import one.microstream.experimental.binaryread.storage.ConstantRegistry;
import one.microstream.experimental.binaryread.structure.Entity;
import one.microstream.experimental.binaryread.structure.EntityMember;
import one.microstream.experimental.binaryread.structure.Storage;

public class DataStoragePrimitiveWrapperDeserializer implements DataStorageDeserializer
{

    private final Storage storage;
    private final DataConversion dataConversion;

    public DataStoragePrimitiveWrapperDeserializer(final Storage storage)
    {
        this.storage = storage;
        this.dataConversion = new DataConversion();
    }

    @Override
    public ConvertedData resolve(final EntityMember entityMember)
    {
        final Long reference = entityMember.getReader()
                .read();
        String typeName = entityMember.getTypeDefinitionMember()
                .typeName();
        return resolve(reference, new DeserializerOptionsBuilder().withTypeName(typeName)
                .build());
    }

    @Override
    public ConvertedData resolve(final Long reference, final DeserializerOptions options)
    {
        if (reference > START_CID_BASE)
        {
            // A constant
            final Object constantObject = ConstantRegistry.lookupObject(reference);
            if (constantObject == null)
            {
                throw new InvalidObjectIdFoundException("cached instance", reference);
            }
            return new ConvertedData(constantObject);
        }
        else
        {

            final Entity valueEntity = storage.getEntityByObjectId(reference);
            if (valueEntity == null || valueEntity.getMembers()
                    .isEmpty())
            {
                return new ConvertedData(null);
            }
            else
            {
                final String enumValue = storage.getEnumValue(valueEntity.getObjectId());
                if (enumValue != null)
                {
                    return new ConvertedData(enumValue);
                }
                final EntityMember valueEntityMember = valueEntity.getEntityMember("value");
                final Object value = valueEntityMember.getReader()
                        .read();
                String actualType = options.getTypeName();
                if ("java.lang.Object".equals(actualType))
                {
                    // If Object, get the actual type for the value entity.
                    actualType = valueEntity.getMembers()
                            .get(0)
                            .getTypeDefinitionMember()
                            .typeName();
                }
                return new ConvertedData(dataConversion.perform(value, actualType));
            }
        }
    }
}
