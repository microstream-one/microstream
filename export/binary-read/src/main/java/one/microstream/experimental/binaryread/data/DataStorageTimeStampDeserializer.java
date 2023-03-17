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

import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

public class DataStorageTimeStampDeserializer implements DataStorageDeserializer
{

    private final Storage storage;

    public DataStorageTimeStampDeserializer(final Storage storage)
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
        final Entity valueEntity = storage.getEntityByObjectId(reference);
        if (valueEntity == null)
        {
            // null  value
            return new ConvertedData(null);
        }
        else
        {
            final String names = valueEntity.getMembers()
                    .stream()
                    .map(em -> em.getName())
                    .collect(Collectors.joining(","));
            Object value = null;
            if ("timestamp".equals(names))
            {
                // Java util Date, sql TimeStamp
                value = valueEntity.getEntityMember("timestamp")
                        .getReader()
                        .read();
            }
            if ("year,month,day".equals(names))
            {
                // LocalDate -> year:month:day
                final List<Number> allValues = valueEntity.getMembers()
                        .stream()
                        .map(em -> (Number) em.getReader()
                                .read())
                        .collect(Collectors.toList());
                final Calendar calendar = Calendar.getInstance();
                calendar.set(allValues.get(0)
                                     .intValue(), allValues.get(1)
                                     .intValue() - 1, allValues.get(2)
                                     .intValue());
                // Note: Subtract 1 from month since it's zero-indexed

                value = calendar.getTimeInMillis();
            }
            if ("date,time".equals(names))
            {
                // LocalDateTime ->
                final Long dateReference = valueEntity.getEntityMember("date")
                        .getReader()
                        .read();
                final Long timeReference = valueEntity.getEntityMember("time")
                        .getReader()
                        .read();
                final Entity dateEntity = storage.getEntityByObjectId(dateReference);
                final Entity timeEntity = storage.getEntityByObjectId(timeReference);

                //year:month:day
                final List<Number> allValues = dateEntity.getMembers()
                        .stream()
                        .map(em -> (Number) em.getReader()
                                .read())
                        .collect(Collectors.toList());


                //hour:min:sec:nanoseconds
                allValues.addAll(timeEntity.getMembers()
                                         .stream()
                                         .map(em -> (Number) em.getReader()
                                                 .read())
                                         .collect(Collectors.toList()));
                final Calendar calendar = Calendar.getInstance();
                calendar.set(allValues.get(0)
                                     .intValue(), allValues.get(1)
                                     .intValue() - 1, allValues.get(2)
                                     .intValue(), allValues.get(3)
                                     .intValue(), allValues.get(4)
                                     .intValue(), allValues.get(5)
                                     .intValue());
                calendar.set(Calendar.MILLISECOND, allValues.get(6)
                        .intValue() / 1_000_000);

                // divided by  1_000_000 since value was nanoseconds.
                value = calendar.getTimeInMillis();
            }
            if ("seconds,nanos".equals(names))
            {
                // Instant ->
                final Long secondsValue = valueEntity.getEntityMember("seconds")
                        .getReader()
                        .read();
                final Integer nanosValue = valueEntity.getEntityMember("nanos")
                        .getReader()
                        .read();
                // convert to epoch
                value = secondsValue * 1000 + nanosValue / 1_000_000;
            }

            return new ConvertedData(value);
        }
    }
}
