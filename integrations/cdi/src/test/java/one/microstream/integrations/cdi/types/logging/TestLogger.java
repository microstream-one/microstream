package one.microstream.integrations.cdi.types.logging;

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

import org.slf4j.event.EventRecodingLogger;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;
import org.slf4j.event.SubstituteLoggingEvent;
import org.slf4j.helpers.SubstituteLogger;

import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * SLF4J Test Logger and has static management methods.  Call {@code  reset()} in the {@code  BeforeEach} annotated method so
 * that you are sure that previous messages are cleared (since all messages during all tests are accumulated in this class)
 */
public class TestLogger extends EventRecodingLogger
{

    private static final Queue<SubstituteLoggingEvent> eventQueue = new ArrayBlockingQueue<>(100);

    public TestLogger(final String name)
    {
        super(new SubstituteLogger(name, eventQueue, true), eventQueue);
    }

    /**
     * Clear all messages
     */
    public static void reset()
    {
        eventQueue.clear();
    }

    /**
     * Returns the logging events (messages) for a specified {@link Level} or all messages. The order is as how the
     * logging events are created.
     *
     * @param level The {@link Level} of the messages you are interested or null if you want them all.
     * @return List with logging events for the specified {@link Level} or all.
     */
    public static List<LoggingEvent> getMessagesOfLevel(final Level level)
    {
        Stream<LoggingEvent> stream = Arrays.stream(eventQueue.toArray(new LoggingEvent[0]));
        if (level != null)
        {
            stream = stream.filter(le -> le.getLevel()
                    .equals(level));
        }
        return stream.collect(Collectors.toList());
    }
}
