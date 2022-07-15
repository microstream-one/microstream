
package one.microstream.integrations.cdi.types.interceptor;

/*-
 * #%L
 * microstream-integrations-cdi
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

import one.microstream.integrations.cdi.types.Agenda;
import one.microstream.integrations.cdi.types.dirty.DirtyInstanceCollector;
import one.microstream.integrations.cdi.types.dirty.DirtyMarkerImpl;
import one.microstream.integrations.cdi.types.extension.StorageExtension;
import one.microstream.integrations.cdi.types.extension.StoreInterceptor;
import one.microstream.integrations.cdi.types.logging.TestLogger;
import one.microstream.reference.Lazy;
import one.microstream.reference.ObjectSwizzling;
import one.microstream.storage.types.StorageManager;
import org.jboss.weld.junit5.auto.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.mockito.verification.VerificationMode;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;


@EnableAutoWeld  // So that Weld container is started
@ExtendWith(MockitoExtension.class)  // for the @Captor
@ActivateScopes(RequestScoped.class)
@AddExtensions(StorageExtension.class)
@AddEnabledInterceptors(StoreInterceptor.class)
@AddBeanClasses(DirtyMarkerImpl.class)  // JUnit 5 Weld extension doesn't pick up bean with @Typed
class StoreInterceptorTest
{
	@Inject
	private AgendaService lazyService;

	@Captor
	private ArgumentCaptor<Agenda> rootCaptor;

	@Captor
	private ArgumentCaptor<Object> objectCaptor;

	@ApplicationScoped
	@Produces
	private StorageManager storageManagerMock = Mockito.mock(StorageManager.class);

	@Mock
	private ObjectSwizzling objectSwizzlingMock;

	@BeforeEach
	public void setup()
	{
		TestLogger.reset();
	}

	@Test
	void storeAsynchronous() throws InterruptedException
	{
		this.lazyService.addName("Poliana");
		final Agenda agenda = CDI.current()
				.select(Agenda.class)
				.get();

		TimeUnit.SECONDS.sleep(1);  // Make sure the Pump has some time to process the store

		Assertions.assertEquals(1, agenda.getNames()
				.size());
		Assertions.assertEquals("Poliana", agenda.getNames()
				.iterator()
				.next());

		Mockito.verify(this.storageManagerMock, justOnce())
				.setRoot(this.rootCaptor.capture());
		Assertions.assertInstanceOf(Agenda.class, this.rootCaptor.getValue());

		Mockito.verify(this.storageManagerMock, justOnce())
				.storeRoot();

		Mockito.verify(this.storageManagerMock, justOnce())
				.store(this.objectCaptor.capture());
		Assertions.assertInstanceOf(ConcurrentSkipListSet.class, this.objectCaptor.getValue());

		// Check if we have cleared the list of dirty instances.
		final DirtyInstanceCollector collector = CDI.current()
				.select(DirtyInstanceCollector.class)
				.get();
		Assertions.assertTrue(collector.getDirtyInstances().isEmpty());

		// No problem message
		List<LoggingEvent> messages = TestLogger.getMessagesOfLevel(Level.WARN);
		Assertions.assertTrue(messages.isEmpty());

	}

	@Test
	void storeAsynchronousWithFailure() throws InterruptedException
	{
		// We throw exception when we try to store. But since we do it asynchronously the call to the service method will
		// be successful.
		Mockito.doThrow(new RuntimeException("Store Failed"))
				.when(storageManagerMock)
				.store(Mockito.any());

		this.lazyService.addName("JUnit");
		final Agenda agenda = CDI.current()
				.select(Agenda.class)
				.get();

		TimeUnit.SECONDS.sleep(1);  // Make sure the Pump has some time to process the store

		Assertions.assertEquals(1, agenda.getNames()
				.size());
		Assertions.assertEquals("JUnit", agenda.getNames()
				.iterator()
				.next());

		Mockito.verify(this.storageManagerMock, justOnce())
				.setRoot(this.rootCaptor.capture());
		Assertions.assertInstanceOf(Agenda.class, this.rootCaptor.getValue());

		Mockito.verify(this.storageManagerMock, justOnce())
				.storeRoot();

		Mockito.verify(this.storageManagerMock, justOnce())
				.store(this.objectCaptor.capture());
		Assertions.assertInstanceOf(ConcurrentSkipListSet.class, this.objectCaptor.getValue());

		// Check if we have cleared the list of dirty instances.
		final DirtyInstanceCollector collector = CDI.current()
				.select(DirtyInstanceCollector.class)
				.get();
		Assertions.assertTrue(collector.getDirtyInstances().isEmpty());

		// Do we have the Error message in the log that happened during `store()`?
		List<LoggingEvent> messages = TestLogger.getMessagesOfLevel(Level.WARN);
		Assertions.assertEquals(1, messages.size());
		Assertions.assertEquals("Problem during Asynchronous storage : Store Failed", messages.get(0)
				.getMessage());
	}

	@Test
	public void storeAsynchronousWithLazy() throws InterruptedException
	{
		// Some logic that needs to be done to handle Lazy properly. (so that we can check .isLoaded)
		Mockito.doAnswer((Answer<Void>) invocationOnMock ->
				{
					Object argument = invocationOnMock.getArguments()[0];
					if (argument instanceof Lazy)
					{
						Lazy.Default lazy = (Lazy.Default) argument;
						lazy.$link(1L, objectSwizzlingMock);
					}
					return null;
				})
				.when(storageManagerMock)
				.store(Mockito.any());

		this.lazyService.addNameLazy("Rudy");

		final Agenda agenda = CDI.current()
				.select(Agenda.class)
				.get();

		TimeUnit.SECONDS.sleep(1);  // Make sure the Pump has some time to process the store

		Assertions.assertFalse(agenda.isLazySetLoaded());

		// Check if we have cleared the list of dirty instances.
		final DirtyInstanceCollector collector = CDI.current()
				.select(DirtyInstanceCollector.class)
				.get();
		Assertions.assertTrue(collector.getDirtyInstances().isEmpty());

		// No problem message
		List<LoggingEvent> messages = TestLogger.getMessagesOfLevel(Level.WARN);
		Assertions.assertTrue(messages.isEmpty());

	}

	@Test
	void storeSynchronous()
	{
		this.lazyService.addNameSynchro("Poliana");
		final Agenda agenda = CDI.current()
				.select(Agenda.class)
				.get();

		Assertions.assertEquals(1, agenda.getNames()
				.size());
		Assertions.assertEquals("Poliana", agenda.getNames()
				.iterator()
				.next());

		Mockito.verify(this.storageManagerMock, justOnce())
				.setRoot(this.rootCaptor.capture());
		Assertions.assertInstanceOf(Agenda.class, this.rootCaptor.getValue());

		Mockito.verify(this.storageManagerMock, justOnce())
				.storeRoot();

		Mockito.verify(this.storageManagerMock, justOnce())
				.store(this.objectCaptor.capture());
		Assertions.assertInstanceOf(ConcurrentSkipListSet.class, this.objectCaptor.getValue());

		// Check if we have cleared the list of dirty instances.
		final DirtyInstanceCollector collector = CDI.current()
				.select(DirtyInstanceCollector.class)
				.get();
		Assertions.assertTrue(collector.getDirtyInstances().isEmpty());

		// No problem message
		List<LoggingEvent> messages = TestLogger.getMessagesOfLevel(Level.WARN);
		Assertions.assertTrue(messages.isEmpty());

	}

	@Test
	void storeSynchronousWithFailure()
	{
		// We throw exception when we try to store. But since we do it asynchronously the call to the service method will
		// be successful.
		Mockito.doThrow(new RuntimeException("Store Failed"))
				.when(storageManagerMock)
				.store(Mockito.any());

		RuntimeException runtimeException = Assertions.assertThrows(RuntimeException.class, () -> this.lazyService.addNameSynchro("JUnit"));
		Assertions.assertEquals("Store Failed", runtimeException.getMessage());

		// The store into the root object still happened !!
		final Agenda agenda = CDI.current()
				.select(Agenda.class)
				.get();

		Assertions.assertEquals(1, agenda.getNames()
				.size());
		Assertions.assertEquals("JUnit", agenda.getNames()
				.iterator()
				.next());

		Mockito.verify(this.storageManagerMock, justOnce())
				.setRoot(this.rootCaptor.capture());
		Assertions.assertInstanceOf(Agenda.class, this.rootCaptor.getValue());

		Mockito.verify(this.storageManagerMock, justOnce())
				.storeRoot();

		Mockito.verify(this.storageManagerMock, justOnce())
				.store(this.objectCaptor.capture());
		Assertions.assertInstanceOf(ConcurrentSkipListSet.class, this.objectCaptor.getValue());

		// Due to the exception, clearing the collected dirty instances did not happen.
		final DirtyInstanceCollector collector = CDI.current()
				.select(DirtyInstanceCollector.class)
				.get();
		Assertions.assertFalse(collector.getDirtyInstances().isEmpty());

		// No message in log (but we have an Exception that is thrown to indicate the problem)
		List<LoggingEvent> messages = TestLogger.getMessagesOfLevel(Level.WARN);
		Assertions.assertTrue(messages.isEmpty());

	}

	@Test
	public void storeSynchronousWithLazy() throws InterruptedException
	{
		// Some logic that needs to be done to handle Lazy properly. (so that we can check .isLoaded)
		Mockito.doAnswer((Answer<Void>) invocationOnMock ->
				{
					Object argument = invocationOnMock.getArguments()[0];
					if (argument instanceof Lazy)
					{
						Lazy.Default lazy = (Lazy.Default) argument;
						lazy.$link(1L, objectSwizzlingMock);
					}
					return null;
				})
				.when(storageManagerMock)
				.store(Mockito.any());

		this.lazyService.addNameLazySynchro("Rudy");

		final Agenda agenda = CDI.current()
				.select(Agenda.class)
				.get();

		TimeUnit.SECONDS.sleep(1);  // Make sure the Pump has some time to process the store

		Assertions.assertFalse(agenda.isLazySetLoaded());

		// Check if we have cleared the list of dirty instances.
		final DirtyInstanceCollector collector = CDI.current()
				.select(DirtyInstanceCollector.class)
				.get();
		Assertions.assertTrue(collector.getDirtyInstances().isEmpty());

		// No problem message
		List<LoggingEvent> messages = TestLogger.getMessagesOfLevel(Level.WARN);
		Assertions.assertTrue(messages.isEmpty());

	}

	@Test
	void storeOnlyOnce() throws InterruptedException
	{
		this.lazyService.addNames("Otavio","Rudy");

		final Agenda agenda = CDI.current()
				.select(Agenda.class)
				.get();

		TimeUnit.SECONDS.sleep(1);  // Make sure the Pump has some time to process the store

		Assertions.assertEquals(2, agenda.getNames()
				.size());

		Mockito.verify(this.storageManagerMock, justOnce())
				.setRoot(this.rootCaptor.capture());
		Assertions.assertInstanceOf(Agenda.class, this.rootCaptor.getValue());

		Mockito.verify(this.storageManagerMock, justOnce())
				.storeRoot();

		Mockito.verify(this.storageManagerMock, justOnce())
				.store(this.objectCaptor.capture());
		Assertions.assertInstanceOf(ConcurrentSkipListSet.class, this.objectCaptor.getValue());

		Mockito.verify(this.storageManagerMock, Mockito.atMostOnce())
				.close();

		// Check if we have cleared the list of dirty instances.
		final DirtyInstanceCollector collector = CDI.current()
				.select(DirtyInstanceCollector.class)
				.get();
		Assertions.assertTrue(collector.getDirtyInstances().isEmpty());

		// No problem message
		List<LoggingEvent> messages = TestLogger.getMessagesOfLevel(Level.WARN);
		Assertions.assertTrue(messages.isEmpty());

	}

	private VerificationMode justOnce()
	{
		return Mockito.times(1);
	}

}
