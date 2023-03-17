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

import one.microstream.experimental.binaryread.ReadingContext;
import one.microstream.experimental.binaryread.storage.ConstantRegistry;
import one.microstream.experimental.binaryread.structure.Entity;
import one.microstream.experimental.binaryread.structure.EntityMember;
import one.microstream.experimental.binaryread.structure.EntityMemberType;
import one.microstream.experimental.binaryread.structure.Storage;

/**
 * This tries to resolve the Optional but when the optional does not hold a 'simple' value, it doesn't resolve it
 * and returns the referenced value as not resolved value.
 */
public class DataStorageOptionalDeserializer implements DataStorageDeserializer
{

    private final ReadingContext readingContext;

    private final Storage storage;

    public DataStorageOptionalDeserializer(final ReadingContext readingContext)
    {
        this.readingContext = readingContext;
        this.storage = readingContext.getStorage();
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
        final Entity optionalEntity = storage.getEntityByObjectId(reference);
        if (optionalEntity == null)
        {
            // null instead of Optional instance
            return new ConvertedData(null);
        }
        else
        {
            // This is the pointer to the value within the optional
            final Long optionalReference = optionalEntity.getEntityMember("value")
                    .getReader()
                    .read();
            final Object cachedInstance = ConstantRegistry.lookupObject(optionalReference);
            if (cachedInstance != null)
            {
                // We have a cached primitive instance
                return new ConvertedData(cachedInstance);
            }
            else
            {
                final Entity optionalValueEntity = storage.getEntityByObjectId(optionalReference);

                if (optionalValueEntity == null)
                {
                    // Optional.empty
                    return new ConvertedData(null);
                }
                else
                {
                    ConvertedData result = null;
                    if (optionalValueEntity.getMembers()
                            .size() == 1)
                    {
                        final EntityMember wrappedEntityMember = optionalValueEntity.getMembers()
                                .get(0);
                        final EntityMemberType entityMemberType = wrappedEntityMember.getEntityMemberType();

                        if (entityMemberType == EntityMemberType.PRIMITIVE
                                || entityMemberType == EntityMemberType.PRIMITIVE_WRAPPER)
                        {
                            result = readingContext.getDataStorageDeserializerFactory()
                                    .getDataStorageDeserializer(wrappedEntityMember.getEntityMemberType())
                                    .resolve(wrappedEntityMember);

                        }
                        if (entityMemberType == EntityMemberType.ARRAY)
                        {
                            // FIXME Is this only String and BigInteger?
                            // They are handled correctly, other ARRAY types might not
                            result = readingContext.getDataStorageDeserializerFactory()
                                    .getDataStorageDeserializer(EntityMemberType.PRIMITIVE)
                                    .resolve(wrappedEntityMember);

                        }

                        if (result == null)
                        {
                            // At the end, handle it as a reference
                            result = new ConvertedData(reference, false);
                        }
                    }
                    else
                    {
                        String enumValue = storage.getEnumValue(optionalReference);
                        if (enumValue == null)
                        {
                            // A POJO, so we cannot handle the Optional 'inline'
                            result = new ConvertedData(reference, false);
                        }
                        else
                        {
                            result = new ConvertedData(enumValue);
                        }
                    }
                    return result;
                }
            }
        }
    }
}
