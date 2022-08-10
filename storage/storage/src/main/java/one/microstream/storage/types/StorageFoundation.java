
package one.microstream.storage.types;

import static one.microstream.X.notNull;

/*-
 * #%L
 * microstream-storage
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

import java.nio.ByteOrder;

import one.microstream.exceptions.MissingFoundationPartException;
import one.microstream.persistence.binary.types.BinaryEntityRawDataIterator;
import one.microstream.persistence.types.ObjectIdsSelector;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceLiveStorerRegistry;
import one.microstream.persistence.types.Unpersistable;
import one.microstream.reference.Reference;
import one.microstream.storage.types.StorageDataChunkValidator.Provider2;
import one.microstream.storage.types.StorageFileWriter.Provider;
import one.microstream.util.InstanceDispatcher;
import one.microstream.util.ProcessIdentityProvider;


/**
 * A kind of factory type that holds and creates on demand all the parts that form a {@link StorageSystem} instance,
 * i.e. a functional database handling logic.
 * <p>
 * Additionally to the services of a mere factory type, a foundation type also keeps references to all parts
 * after a {@link StorageSystem} instance has been created. This is useful if some internal logic parts shall be
 * accessed while the {@link StorageSystem} logic is already running. Therefore, this type can best be thought of
 * as a {@literal foundation} on which the running database handling logic stands.
 * <p>
 * All {@literal set~} methods are simple setter methods without any additional logic worth mentioning.<br>
 * All {@literal set~} methods return {@literal this} to allow for easy method chaining to improve readability.<br>
 * All {@literal get~} methods return a logic part instance, if present or otherwise creates and sets one beforehand
 * via a default creation logic.
 * 
 * 
 *
 * @param <F> the "self-type" of the  {@link StorageFoundation} implementation.
 */
public interface StorageFoundation<F extends StorageFoundation<?>> extends InstanceDispatcher
{
	/* (11.06.2019 TM)NOTE:
	 * JavaDoc-Note: all setters and getters use the same text with only the type name inserted.
	 * The DocLink linking mechanism does not (yet) provide enough functionality to make parts
	 * of a general description reusable, so it has to be copy&pasted nevertheless.
	 * 
	 * Sufficient functionality should be added in the future. E.g. allowing to link a set of paragraphs, like:
	 * StorageFoundation#getConfiguration()§2
	 * or even
	 * StorageFoundation#getConfiguration()§2-4,6,8-11
	 * 
	 * Until then, maintenance of these JavaDocs must happen by modifying one and re-copy&pasting to all others.
	 * Or by text / regExp replacements, of course.
	 */
	
	/**
	 * Returns the currently set {@link StorageConfiguration} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageSystem()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return the currently set instance, potentially created on-demand if required.
	 * 
	 * @throws MissingFoundationPartException if a returnable instance is required but cannot be created by default.
	 */
	public StorageConfiguration getConfiguration();
	
	/**
	 * Returns the currently set {@link StorageOperationController.Creator} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageSystem()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return the currently set instance, potentially created on-demand if required.
	 * 
	 * @throws MissingFoundationPartException if a returnable instance is required but cannot be created by default.
	 */
	public StorageOperationController.Creator getOperationControllerCreator();
	
	/**
	 * Returns the currently set {@link StorageInitialDataFileNumberProvider} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageSystem()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return the currently set instance, potentially created on-demand if required.
	 * 
	 * @throws MissingFoundationPartException if a returnable instance is required but cannot be created by default.
	 */
	public StorageInitialDataFileNumberProvider getInitialDataFileNumberProvider();
	
	/**
	 * Returns the currently set {@link StorageRequestAcceptor.Creator} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageSystem()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return the currently set instance, potentially created on-demand if required.
	 * 
	 * @throws MissingFoundationPartException if a returnable instance is required but cannot be created by default.
	 */
	public StorageRequestAcceptor.Creator getRequestAcceptorCreator();
	
	/**
	 * Returns the currently set {@link StorageTaskBroker.Creator} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageSystem()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return the currently set instance, potentially created on-demand if required.
	 * 
	 * @throws MissingFoundationPartException if a returnable instance is required but cannot be created by default.
	 */
	public StorageTaskBroker.Creator getTaskBrokerCreator();
	
	/**
	 * Returns the currently set {@link StorageDataChunkValidator.Provider} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageSystem()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return the currently set instance, potentially created on-demand if required.
	 * 
	 * @throws MissingFoundationPartException if a returnable instance is required but cannot be created by default.
	 */
	public StorageDataChunkValidator.Provider getDataChunkValidatorProvider();
	
	/**
	 * Returns the currently set {@link StorageDataChunkValidator.Provider2} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageSystem()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return the currently set instance, potentially created on-demand if required.
	 * 
	 * @throws MissingFoundationPartException if a returnable instance is required but cannot be created by default.
	 */
	public StorageDataChunkValidator.Provider2 getDataChunkValidatorProvider2();
	
	/**
	 * Returns the currently set {@link StorageChannelsCreator} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageSystem()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return the currently set instance, potentially created on-demand if required.
	 * 
	 * @throws MissingFoundationPartException if a returnable instance is required but cannot be created by default.
	 */
	public StorageChannelsCreator getChannelCreator();

	/**
	 * Returns the currently set {@link StorageThreadNameProvider} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageSystem()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return the currently set instance, potentially created on-demand if required.
	 * 
	 * @throws MissingFoundationPartException if a returnable instance is required but cannot be created by default.
	 */
	public StorageThreadNameProvider getThreadNameProvider();
	
	/**
	 * Returns the currently set {@link StorageChannelThreadProvider} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageSystem()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return the currently set instance, potentially created on-demand if required.
	 * 
	 * @throws MissingFoundationPartException if a returnable instance is required but cannot be created by default.
	 */
	public StorageChannelThreadProvider getChannelThreadProvider();
		
	/**
	 * Returns the currently set {@link StorageBackupThreadProvider} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageSystem()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return the currently set instance, potentially created on-demand if required.
	 * 
	 * @throws MissingFoundationPartException if a returnable instance is required but cannot be created by default.
	 */
	public StorageBackupThreadProvider getBackupThreadProvider();
	
	/**
	 * Returns the currently set {@link StorageLockFileManagerThreadProvider} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageSystem()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return the currently set instance, potentially created on-demand if required.
	 * 
	 * @throws MissingFoundationPartException if a returnable instance is required but cannot be created by default.
	 */
	public StorageLockFileManagerThreadProvider getLockFileManagerThreadProvider();
	
	/**
	 * Returns the currently set {@link StorageThreadProvider} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageSystem()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return the currently set instance, potentially created on-demand if required.
	 * 
	 * @throws MissingFoundationPartException if a returnable instance is required but cannot be created by default.
	 */
	public StorageThreadProvider getThreadProvider();
	
	/**
	 * Returns the currently set {@link StorageRequestTaskCreator} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageSystem()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return the currently set instance, potentially created on-demand if required.
	 * 
	 * @throws MissingFoundationPartException if a returnable instance is required but cannot be created by default.
	 */
	public StorageRequestTaskCreator getRequestTaskCreator();
	
	/**
	 * Returns the currently set {@link StorageTypeDictionary} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageSystem()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return the currently set instance, potentially created on-demand if required.
	 * 
	 * @throws MissingFoundationPartException if a returnable instance is required but cannot be created by default.
	 */
	public StorageTypeDictionary getTypeDictionary();
	
	/**
	 * Returns the currently set {@link StorageRootTypeIdProvider} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageSystem()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return the currently set instance, potentially created on-demand if required.
	 * 
	 * @throws MissingFoundationPartException if a returnable instance is required but cannot be created by default.
	 */
	public StorageRootTypeIdProvider getRootTypeIdProvider();
	
	/**
	 * Returns the currently set {@link StorageTimestampProvider} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageSystem()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return the currently set instance, potentially created on-demand if required.
	 * 
	 * @throws MissingFoundationPartException if a returnable instance is required but cannot be created by default.
	 */
	public StorageTimestampProvider getTimestampProvider();
	
	/**
	 * Returns the currently set {@link StorageObjectIdRangeEvaluator} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageSystem()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return the currently set instance, potentially created on-demand if required.
	 * 
	 * @throws MissingFoundationPartException if a returnable instance is required but cannot be created by default.
	 */
	public StorageObjectIdRangeEvaluator getObjectIdRangeEvaluator();
		
	/**
	 * Returns the currently set {@link StorageFileWriter.Provider} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageSystem()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return the currently set instance, potentially created on-demand if required.
	 * 
	 * @throws MissingFoundationPartException if a returnable instance is required but cannot be created by default.
	 */
	public StorageFileWriter.Provider getWriterProvider();
	
	/**
	 * Returns the currently set {@link StorageGCZombieOidHandler} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageSystem()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return the currently set instance, potentially created on-demand if required.
	 * 
	 * @throws MissingFoundationPartException if a returnable instance is required but cannot be created by default.
	 */
	public StorageGCZombieOidHandler getGCZombieOidHandler();
	
	/**
	 * Returns the currently set {@link StorageRootOidSelector.Provider} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageSystem()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return the currently set instance, potentially created on-demand if required.
	 * 
	 * @throws MissingFoundationPartException if a returnable instance is required but cannot be created by default.
	 */
	public StorageRootOidSelector.Provider getRootOidSelectorProvider();
	
	/**
	 * Returns the currently set {@link StorageObjectIdMarkQueue.Creator} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageSystem()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return the currently set instance, potentially created on-demand if required.
	 * 
	 * @throws MissingFoundationPartException if a returnable instance is required but cannot be created by default.
	 */
	public StorageObjectIdMarkQueue.Creator getOidMarkQueueCreator();
	
	/**
	 * Returns the currently set {@link StorageEntityMarkMonitor.Creator} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageSystem()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return the currently set instance, potentially created on-demand if required.
	 * 
	 * @throws MissingFoundationPartException if a returnable instance is required but cannot be created by default.
	 */
	public StorageEntityMarkMonitor.Creator getEntityMarkMonitorCreator();
	
	/**
	 * Returns the currently set {@link StorageDataFileValidator.Creator} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageSystem()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return the currently set instance, potentially created on-demand if required.
	 * 
	 * @throws MissingFoundationPartException if a returnable instance is required but cannot be created by default.
	 */
	public StorageDataFileValidator.Creator getDataFileValidatorCreator();
	
	/**
	 * Returns the currently set {@link BinaryEntityRawDataIterator.Provider} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageSystem()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return the currently set instance, potentially created on-demand if required.
	 * 
	 * @throws MissingFoundationPartException if a returnable instance is required but cannot be created by default.
	 */
	public BinaryEntityRawDataIterator.Provider getEntityDataIteratorProvider();
	
	/**
	 * Returns the currently set {@link StorageEntityDataValidator.Creator} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageSystem()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return the currently set instance, potentially created on-demand if required.
	 * 
	 * @throws MissingFoundationPartException if a returnable instance is required but cannot be created by default.
	 */
	public StorageEntityDataValidator.Creator getEntityDataValidatorCreator();
	
	/**
	 * Returns the currently set {@link ProcessIdentityProvider} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageSystem()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return the currently set instance, potentially created on-demand if required.
	 * 
	 * @throws MissingFoundationPartException if a returnable instance is required but cannot be created by default.
	 */
	public ProcessIdentityProvider getProcessIdentityProvider();
	
	/**
	 * Returns the currently set {@link StorageLockFileSetup} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageSystem()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return the currently set instance, potentially created on-demand if required.
	 * 
	 * @throws MissingFoundationPartException if a returnable instance is required but cannot be created by default.
	 */
	public StorageLockFileSetup getLockFileSetup();
	
	/**
	 * Returns the currently set {@link StorageLockFileSetup.Provider} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageSystem()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return the currently set instance, potentially created on-demand if required.
	 * 
	 * @throws MissingFoundationPartException if a returnable instance is required but cannot be created by default.
	 */
	public StorageLockFileSetup.Provider getLockFileSetupProvider();
	
	/**
	 * Returns the currently set {@link StorageLockFileManager.Creator} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageSystem()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return the currently set instance, potentially created on-demand if required.
	 * 
	 * @throws MissingFoundationPartException if a returnable instance is required but cannot be created by default.
	 */
	public StorageLockFileManager.Creator getLockFileManagerCreator();
	
	/**
	 * Returns the currently set {@link StorageExceptionHandler} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageSystem()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return the currently set instance, potentially created on-demand if required.
	 * 
	 * @throws MissingFoundationPartException if a returnable instance is required but cannot be created by default.
	 */
	public StorageExceptionHandler getExceptionHandler();
	
	public StorageEventLogger getEventLogger();

	public StorageWriteController writeController();
	
	public StorageWriteController getWriteController();

	public StorageHousekeepingBroker housekeepingBroker();
	
	public StorageHousekeepingBroker getHousekeepingBroker();
	
	public ObjectIdsSelector getLiveObjectIdChecker();

	public Reference<PersistenceLiveStorerRegistry> getLiveStorerRegistryReference();

	/**
	 * Returns the currently set {@link StorageStructureValidator} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * execution of {@link #createStorageSystem()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return the currently set instance, potentially created on-demand if required.
	 * 
	 * @throws MissingFoundationPartException if a returnable instance is required but cannot be created by default.
	 */
	public StorageStructureValidator getStorageStructureValidator();
	
	/**
	 * Sets the {@link StorageConfiguration} instance to be used for the assembly.
	 * 
	 * @param configuration the instance to be used.
	 * 
	 * @return {@literal this} to allow method chaining.
	 */
	public F setConfiguration(StorageConfiguration configuration);
	
	/**
	 * Sets the {@link StorageOperationController.Creator} instance to be used for the assembly.
	 * 
	 * @param operationControllerCreator the instance to be used.
	 * 
	 * @return {@literal this} to allow method chaining.
	 */
	public F setOperationControllerCreator(StorageOperationController.Creator operationControllerCreator);
	
	/**
	 * Sets the {@link StorageInitialDataFileNumberProvider} instance to be used for the assembly.
	 * 
	 * @param initDataFileNumberProvider the instance to be used.
	 * 
	 * @return {@literal this} to allow method chaining.
	 */
	public F setInitialDataFileNumberProvider(StorageInitialDataFileNumberProvider initDataFileNumberProvider);
	
	/**
	 * Sets the {@link StorageRequestAcceptor.Creator} instance to be used for the assembly.
	 * 
	 * @param requestAcceptorCreator the instance to be used.
	 * 
	 * @return {@literal this} to allow method chaining.
	 */
	public F setRequestAcceptorCreator(StorageRequestAcceptor.Creator requestAcceptorCreator);
	
	/**
	 * Sets the {@link StorageTaskBroker.Creator} instance to be used for the assembly.
	 * 
	 * @param taskBrokerCreator the instance to be used.
	 * 
	 * @return {@literal this} to allow method chaining.
	 */
	public F setTaskBrokerCreator(StorageTaskBroker.Creator taskBrokerCreator);
	
	/**
	 * Sets the {@link StorageDataChunkValidator.Provider} instance to be used for the assembly.
	 * 
	 * @param dataChunkValidatorProvider the instance to be used.
	 * 
	 * @return {@literal this} to allow method chaining.
	 */
	public F setDataChunkValidatorProvider(StorageDataChunkValidator.Provider dataChunkValidatorProvider);
	
	/**
	 * Sets the {@link StorageDataChunkValidator.Provider2} instance to be used for the assembly.
	 * 
	 * @param dataChunkValidatorProvider2 the instance to be used.
	 * 
	 * @return {@literal this} to allow method chaining.
	 */
	public F setDataChunkValidatorProvider2(StorageDataChunkValidator.Provider2 dataChunkValidatorProvider2);
	
	/**
	 * Sets the {@link StorageChannelsCreator} instance to be used for the assembly.
	 * 
	 * @param channelCreator the instance to be used.
	 * 
	 * @return {@literal this} to allow method chaining.
	 */
	public F setChannelCreator(StorageChannelsCreator channelCreator);
	
	/**
	 * Sets the {@link StorageThreadNameProvider} instance to be used for the assembly.
	 * 
	 * @param threadNameProvider the instance to be used.
	 * 
	 * @return {@literal this} to allow method chaining.
	 */
	public F setThreadNameProvider(StorageThreadNameProvider threadNameProvider);
	
	/**
	 * Sets the {@link StorageChannelThreadProvider} instance to be used for the assembly.
	 * 
	 * @param channelThreadProvider the instance to be used.
	 * 
	 * @return {@literal this} to allow method chaining.
	 */
	public F setChannelThreadProvider(StorageChannelThreadProvider channelThreadProvider);
	
	/**
	 * Sets the {@link StorageBackupThreadProvider} instance to be used for the assembly.
	 * 
	 * @param backupThreadProvider the instance to be used.
	 * 
	 * @return {@literal this} to allow method chaining.
	 */
	public F setBackupThreadProvider(StorageBackupThreadProvider backupThreadProvider);
	
	/**
	 * Sets the {@link StorageLockFileManagerThreadProvider} instance to be used for the assembly.
	 * 
	 * @param lockFileManagerThreadProvider the instance to be used.
	 * 
	 * @return {@literal this} to allow method chaining.
	 */
	public F setLockFileManagerThreadProvider(StorageLockFileManagerThreadProvider lockFileManagerThreadProvider);
	
	/**
	 * Sets the {@link StorageThreadProvider} instance to be used for the assembly.
	 * 
	 * @param threadProvider the instance to be used.
	 * 
	 * @return {@literal this} to allow method chaining.
	 */
	public F setThreadProvider(StorageThreadProvider threadProvider);
	
	/**
	 * Sets the {@link StorageRequestTaskCreator} instance to be used for the assembly.
	 * 
	 * @param taskCreator the instance to be used.
	 * 
	 * @return {@literal this} to allow method chaining.
	 */
	public F setTaskCreator(StorageRequestTaskCreator taskCreator);
	
	/**
	 * Sets the {@link StorageTypeDictionary} instance to be used for the assembly.
	 * 
	 * @param typeDictionary the instance to be used.
	 * 
	 * @return {@literal this} to allow method chaining.
	 */
	public F setTypeDictionary(StorageTypeDictionary typeDictionary);
	
	/**
	 * Sets the {@link StorageRootTypeIdProvider} instance to be used for the assembly.
	 * 
	 * @param rootTypeIdProvider the instance to be used.
	 * 
	 * @return {@literal this} to allow method chaining.
	 */
	public F setRootTypeIdProvider(StorageRootTypeIdProvider rootTypeIdProvider);
	
	/**
	 * Sets the {@link StorageTimestampProvider} instance to be used for the assembly.
	 * 
	 * @param timestampProvider the instance to be used.
	 * 
	 * @return {@literal this} to allow method chaining.
	 */
	public F setTimestampProvider(StorageTimestampProvider timestampProvider);
	
	/**
	 * Sets the {@link StorageObjectIdRangeEvaluator} instance to be used for the assembly.
	 * 
	 * @param objectIdRangeEvaluator the instance to be used.
	 * 
	 * @return {@literal this} to allow method chaining.
	 */
	public F setObjectIdRangeEvaluator(StorageObjectIdRangeEvaluator objectIdRangeEvaluator);
		
	/**
	 * Sets the {@link StorageFileWriter.Provider} instance to be used for the assembly.
	 * 
	 * @param writerProvider the instance to be used.
	 * 
	 * @return {@literal this} to allow method chaining.
	 */
	public F setWriterProvider(StorageFileWriter.Provider writerProvider);
	
	/**
	 * Sets the {@link StorageGCZombieOidHandler} instance to be used for the assembly.
	 * 
	 * @param gCZombieOidHandler the instance to be used.
	 * 
	 * @return {@literal this} to allow method chaining.
	 */
	public F setGCZombieOidHandler(StorageGCZombieOidHandler gCZombieOidHandler);
	
	/**
	 * Sets the {@link StorageRootOidSelector.Provider} instance to be used for the assembly.
	 * 
	 * @param rootOidSelectorProvider the instance to be used.
	 * 
	 * @return {@literal this} to allow method chaining.
	 */
	public F setRootOidSelectorProvider(StorageRootOidSelector.Provider rootOidSelectorProvider);
	
	/**
	 * Sets the {@link StorageObjectIdMarkQueue.Creator} instance to be used for the assembly.
	 * 
	 * @param oidMarkQueueCreator the instance to be used.
	 * 
	 * @return {@literal this} to allow method chaining.
	 */
	public F setOidMarkQueueCreator(StorageObjectIdMarkQueue.Creator oidMarkQueueCreator);
	
	/**
	 * Sets the {@link StorageEntityMarkMonitor.Creator} instance to be used for the assembly.
	 * 
	 * @param entityMarkMonitorCreator the instance to be used.
	 * 
	 * @return {@literal this} to allow method chaining.
	 */
	public F setEntityMarkMonitorCreator(StorageEntityMarkMonitor.Creator entityMarkMonitorCreator);
	
	/**
	 * Sets the {@link StorageDataFileValidator.Creator} instance to be used for the assembly.
	 * 
	 * @param dataFileValidatorCreator the instance to be used.
	 * 
	 * @return {@literal this} to allow method chaining.
	 */
	public F setDataFileValidatorCreator(StorageDataFileValidator.Creator dataFileValidatorCreator);
	
	/**
	 * Sets the {@link BinaryEntityRawDataIterator.Provider} instance to be used for the assembly.
	 * 
	 * @param entityRawDataIteratorProvider the instance to be used.
	 * 
	 * @return {@literal this} to allow method chaining.
	 */
	public F setEntityDataIteratorProvider(BinaryEntityRawDataIterator.Provider entityRawDataIteratorProvider);
	
	/**
	 * Sets the {@link StorageEntityDataValidator.Creator} instance to be used for the assembly.
	 * 
	 * @param entityDataValidatorCreator the instance to be used.
	 * 
	 * @return {@literal this} to allow method chaining.
	 */
	public F setEntityDataValidatorCreator(StorageEntityDataValidator.Creator entityDataValidatorCreator);
	
	/**
	 * Sets the {@link ProcessIdentityProvider} instance to be used for the assembly.
	 * 
	 * @param processIdentityProvider the instance to be used.
	 * 
	 * @return {@literal this} to allow method chaining.
	 */
	public F setProcessIdentityProvider(ProcessIdentityProvider processIdentityProvider);
	
	/**
	 * Sets the {@link StorageLockFileSetup} instance to be used for the assembly.
	 * 
	 * @param lockFileSetup the instance to be used.
	 * 
	 * @return {@literal this} to allow method chaining.
	 */
	public F setLockFileSetup(StorageLockFileSetup lockFileSetup);
	
	/**
	 * Sets the {@link StorageLockFileSetup.Provider} instance to be used for the assembly.
	 * 
	 * @param lockFileSetupProvider the instance to be used.
	 * 
	 * @return {@literal this} to allow method chaining.
	 */
	public F setLockFileSetupProvider(StorageLockFileSetup.Provider lockFileSetupProvider);
	
	/**
	 * Sets the {@link StorageLockFileManager.Creator} instance to be used for the assembly.
	 * 
	 * @param lockFileManagerCreator the instance to be used.
	 * 
	 * @return {@literal this} to allow method chaining.
	 */
	public F setLockFileManagerCreator(StorageLockFileManager.Creator lockFileManagerCreator);
	
	/**
	 * Sets the {@link StorageExceptionHandler} instance to be used for the assembly.
	 * 
	 * @param exceptionHandler the instance to be used.
	 * 
	 * @return {@literal this} to allow method chaining.
	 */
	public F setExceptionHandler(StorageExceptionHandler exceptionHandler);
	
	/**
	 * Use {@link #addEventLogger(StorageEventLogger)} instead, multiple event loggers are supported now
	 * 
	 * @deprecated replaced by {@link #addEventLogger(StorageEventLogger)}
	 */
	@Deprecated
	public default F setEventLogger(final StorageEventLogger eventLogger)
	{
		return this.addEventLogger(eventLogger);
	}
	
	public F addEventLogger(StorageEventLogger eventLogger);
	

	public F setWriteController(StorageWriteController writeController);

	public F setHousekeepingBroker(StorageHousekeepingBroker housekeepingBroker);
	
	public F setLiveObjectIdChecker(ObjectIdsSelector liveObjectIdChecker);

	public F setLiveStorerRegistryReference(Reference<PersistenceLiveStorerRegistry> LiveStorerRegistryReference);
	
	/**
	 * Sets the {@link StorageStructureValidator} instance to be used for the assembly.
	 * 
	 * @param storageStructureValidator the instance to be used.
	 * 
	 * @return {@literal this} to allow method chaining.
	 */
	public F setStorageStructureValidator(final StorageStructureValidator storageStructureValidator);
	
	/**
	 * Creates and returns a new {@link StorageSystem} instance by using the current state of all registered
	 * logic part instances and by on-demand creating missing ones via a default logic.
	 * <p>
	 * The returned {@link StorageSystem} instance will NOT yet be started.
	 * 
	 * @return a new {@link StorageSystem} instance.
	 */
	public StorageSystem createStorageSystem();



	public class Default<F extends StorageFoundation.Default<?>>
	extends InstanceDispatcher.Default
	implements StorageFoundation<F>, Unpersistable
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		/* (06.08.2020 TM)TODO: enlarge configuration
		 * Some of these parts should be moved into the configuration.
		 * E.g.
		 * - StorageInitialDataFileNumberProvider
		 * - StorageThreadNameProvider
		 */

		private StorageConfiguration                     configuration                ;
		private StorageOperationController.Creator       operationControllerCreator   ;
		private StorageInitialDataFileNumberProvider     initialDataFileNumberProvider;
		private StorageRequestAcceptor.Creator           requestAcceptorCreator       ;
		private StorageTaskBroker.Creator                taskBrokerCreator            ;
		private StorageDataChunkValidator.Provider       dataChunkValidatorProvider   ;
		private StorageDataChunkValidator.Provider2      dataChunkValidatorProvider2  ;
		private StorageChannelsCreator                   channelCreator               ;
		private StorageThreadNameProvider                threadNameProvider           ;
		private StorageChannelThreadProvider             channelThreadProvider        ;
		private StorageBackupThreadProvider              backupThreadProvider         ;
		private ProcessIdentityProvider                  processIdentityProvider      ;
		private StorageLockFileManagerThreadProvider     lockFileManagerThreadProvider;
		private StorageThreadProvider                    threadProvider               ;
		private StorageRequestTaskCreator                requestTaskCreator           ;
		private StorageTypeDictionary                    typeDictionary               ;
		private StorageRootTypeIdProvider                rootTypeIdProvider           ;
		private StorageTimestampProvider                 timestampProvider            ;
		private StorageObjectIdRangeEvaluator            objectIdRangeEvaluator       ;
		private StorageFileWriter.Provider               writerProvider               ;
		private StorageGCZombieOidHandler                gCZombieOidHandler           ;
		private StorageRootOidSelector.Provider          rootOidSelectorProvider      ;
		private StorageObjectIdMarkQueue.Creator         oidMarkQueueCreator          ;
		private StorageEntityMarkMonitor.Creator         entityMarkMonitorCreator     ;
		private StorageDataFileValidator.Creator         dataFileValidatorCreator     ;
		private BinaryEntityRawDataIterator.Provider     entityDataIteratorProvider   ;
		private StorageEntityDataValidator.Creator       entityDataValidatorCreator   ;
		private StorageLockFileSetup                     lockFileSetup                ;
		private StorageLockFileSetup.Provider            lockFileSetupProvider        ;
		private StorageLockFileManager.Creator           lockFileManagerCreator       ;
		private StorageExceptionHandler                  exceptionHandler             ;
		private StorageEventLogger                       eventLogger                  ;
		private StorageWriteController                   writeController              ;
		private StorageHousekeepingBroker                housekeepingBroker           ;
		private ObjectIdsSelector                        liveObjectIdChecker          ;
		private Reference<PersistenceLiveStorerRegistry> storerRegistryReference      ;
		private StorageStructureValidator                storageStructureValidator    ;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		public Default()
		{
			super();
		}

		

		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@SuppressWarnings("unchecked") // magic self-type.
		protected final F $()
		{
			return (F)this;
		}
		
		

		protected StorageGCZombieOidHandler ensureStorageGCZombieOidHandler()
		{
			return new StorageGCZombieOidHandler.Default();
		}

		protected StorageConfiguration ensureConfiguration()
		{
			return Storage.Configuration();
		}
		
		protected StorageOperationController.Creator ensureOperationControllerCreator()
		{
			return StorageOperationController.Provider();
		}
		
		

		protected StorageInitialDataFileNumberProvider ensureInitialDataFileNumberProvider()
		{
			return new StorageInitialDataFileNumberProvider.Default(1); // constant 1 by default
		}

		protected StorageDataFileEvaluator ensureStorageConfiguration()
		{
			return this.getConfiguration().dataFileEvaluator();
		}

		protected StorageRequestAcceptor.Creator ensureRequestAcceptorCreator()
		{
			return new StorageRequestAcceptor.Creator.Default();
		}

		protected StorageTaskBroker.Creator ensureTaskBrokerCreator()
		{
			return new StorageTaskBroker.Creator.Default();
		}

		protected StorageDataChunkValidator.Provider ensureDataChunkValidatorProvider()
		{
			return this.getDataChunkValidatorProvider2().provideDataChunkValidatorProvider(this);
		}

		protected StorageDataChunkValidator.Provider2 ensureDataChunkValidatorProvider2()
		{
			return new StorageDataChunkValidator.NoOp();
		}

		protected StorageChannelsCreator ensureChannelCreator()
		{
			return new StorageChannelsCreator.Default();
		}

		protected StorageThreadNameProvider ensureThreadNameProvider()
		{
			return StorageThreadNameProvider.Prefixer(Persistence.engineName() + '-');
		}

		protected StorageChannelThreadProvider ensureChannelThreadProvider()
		{
			return new StorageChannelThreadProvider.Default();
		}
		
		protected StorageBackupThreadProvider ensureBackupThreadProvider()
		{
			return StorageBackupThreadProvider.New();
		}
		
		protected ProcessIdentityProvider ensureProcessIdentityProvider()
		{
			return ProcessIdentityProvider.New();
		}
		
		protected StorageLockFileManagerThreadProvider ensureLockFileManagerThreadProvider()
		{
			return StorageLockFileManagerThreadProvider.New();
		}
		
		protected StorageThreadProvider ensureThreadProvider()
		{
			return StorageThreadProvider.New(
				this.getThreadNameProvider(),
				this.getChannelThreadProvider(),
				this.getBackupThreadProvider(),
				this.getLockFileManagerThreadProvider()
			);
		}

		protected StorageRequestTaskCreator ensureRequestTaskCreator()
		{
			return new StorageRequestTaskCreator.Default(
				this.getTimestampProvider()
			);
		}

		protected StorageTypeDictionary ensureTypeDictionary()
		{
			return new StorageTypeDictionary.Default(this.isByteOrderMismatch());
		}

		protected StorageChannelCountProvider ensureChannelCountProvider(final int channelCount)
		{
			return new StorageChannelCountProvider.Default(channelCount);
		}

		protected StorageRootTypeIdProvider ensureRootTypeIdProvider()
		{
			throw new MissingFoundationPartException(StorageRootTypeIdProvider.class);
		}

		protected StorageTimestampProvider ensureTimestampProvider()
		{
			return new StorageTimestampProvider.Default();
		}

		protected StorageObjectIdRangeEvaluator ensureObjectIdRangeEvaluator()
		{
			return new StorageObjectIdRangeEvaluator.Default();
		}

		protected StorageFileWriter.Provider ensureWriterProvider()
		{
			return new StorageFileWriter.Provider.Default();
		}

		protected StorageRootOidSelector.Provider ensureRootOidSelectorProvider()
		{
			return new StorageRootOidSelector.Provider.Default();
		}

		protected StorageObjectIdMarkQueue.Creator ensureOidMarkQueueCreator()
		{
			return new StorageObjectIdMarkQueue.Creator.Default();
		}

		protected StorageEntityMarkMonitor.Creator ensureEntityMarkMonitorCreator()
		{
			return StorageEntityMarkMonitor.Creator();
		}

		protected StorageDataFileValidator.Creator ensureDataFileValidatorCreator()
		{
			return StorageDataFileValidator.Creator(
				this.getEntityDataIteratorProvider(),
				this.getEntityDataValidatorCreator(),
				this.getTypeDictionary()
			);
		}
		
		protected BinaryEntityRawDataIterator.Provider ensureEntityDataIteratorProvider()
		{
			return BinaryEntityRawDataIterator.Provider();
		}
		
		protected StorageEntityDataValidator.Creator ensureEntityDataValidatorCreator()
		{
			return StorageEntityDataValidator.Creator();
		}
		
		
		
		

		protected StorageExceptionHandler ensureExceptionHandler()
		{
			return StorageExceptionHandler.New();
		}
		
		protected StorageEventLogger ensureEventLogger()
		{
			return StorageEventLogger.Default();
		}
		
		// provide instead of ensure because the instance may be null (meaning no lock file)
		protected StorageLockFileSetup provideLockFileSetup()
		{
			final StorageLockFileSetup.Provider lockFileSetupProvider = this.getLockFileSetupProvider();
			
			return lockFileSetupProvider == null
				? null
				: lockFileSetupProvider.provideLockFileSetup(this)
			;
		}
		
		protected StorageLockFileManager.Creator ensureLockFileManagerCreator()
		{
			return StorageLockFileManager.Creator();
		}
		
		protected ByteOrder ensureTargetByteOrder()
		{
			return ByteOrder.nativeOrder();
		}
		
		protected StorageWriteController ensureWriteController()
		{
			return StorageWriteController.Wrap(
				this.getConfiguration().fileProvider().fileSystem()
			);
		}
		
		protected StorageHousekeepingBroker ensureHousekeepingBroker()
		{
			return StorageHousekeepingBroker.New();
		}
		
		protected ObjectIdsSelector ensureObjectIdsSelector()
		{
			throw new MissingFoundationPartException(ObjectIdsSelector.class);
		}

		protected Reference<PersistenceLiveStorerRegistry> ensureLiveStorerRegistryReference()
		{
			throw new MissingFoundationPartException(Reference.class, "to " + PersistenceLiveStorerRegistry.class.getSimpleName());
		}
		
		protected StorageStructureValidator ensureStorageStructureValidator()
		{
			return StorageStructureValidator.New(
				this.getConfiguration().fileProvider(),
				this.getConfiguration().channelCountProvider()
			);
		}
		

		@Override
		public StorageOperationController.Creator getOperationControllerCreator()
		{
			if(this.operationControllerCreator == null)
			{
				this.operationControllerCreator = this.dispatch(this.ensureOperationControllerCreator());
			}
			return this.operationControllerCreator;
		}

		@Override
		public StorageInitialDataFileNumberProvider getInitialDataFileNumberProvider()
		{
			if(this.initialDataFileNumberProvider == null)
			{
				this.initialDataFileNumberProvider = this.dispatch(this.ensureInitialDataFileNumberProvider());
			}
			return this.initialDataFileNumberProvider;
		}

		@Override
		public StorageRequestAcceptor.Creator getRequestAcceptorCreator()
		{
			if(this.requestAcceptorCreator == null)
			{
				this.requestAcceptorCreator = this.dispatch(this.ensureRequestAcceptorCreator());
			}
			return this.requestAcceptorCreator;
		}

		@Override
		public StorageTaskBroker.Creator getTaskBrokerCreator()
		{
			if(this.taskBrokerCreator == null)
			{
				this.taskBrokerCreator = this.dispatch(this.ensureTaskBrokerCreator());
			}
			return this.taskBrokerCreator;
		}

		@Override
		public StorageDataChunkValidator.Provider getDataChunkValidatorProvider()
		{
			if(this.dataChunkValidatorProvider == null)
			{
				this.dataChunkValidatorProvider = this.dispatch(this.ensureDataChunkValidatorProvider());
			}
			return this.dataChunkValidatorProvider;
		}
		
		@Override
		public Provider2 getDataChunkValidatorProvider2()
		{
			if(this.dataChunkValidatorProvider2 == null)
			{
				this.dataChunkValidatorProvider2 = this.dispatch(this.ensureDataChunkValidatorProvider2());
			}
			return this.dataChunkValidatorProvider2;
		}

		@Override
		public StorageChannelsCreator getChannelCreator()
		{
			if(this.channelCreator == null)
			{
				this.channelCreator = this.dispatch(this.ensureChannelCreator());
			}
			return this.channelCreator;
		}
		
		@Override
		public StorageThreadNameProvider getThreadNameProvider()
		{
			if(this.threadNameProvider == null)
			{
				this.threadNameProvider = this.dispatch(this.ensureThreadNameProvider());
			}
			return this.threadNameProvider;
		}

		@Override
		public StorageChannelThreadProvider getChannelThreadProvider()
		{
			if(this.channelThreadProvider == null)
			{
				this.channelThreadProvider = this.dispatch(this.ensureChannelThreadProvider());
			}
			return this.channelThreadProvider;
		}
		
		@Override
		public StorageBackupThreadProvider getBackupThreadProvider()
		{
			if(this.backupThreadProvider == null)
			{
				this.backupThreadProvider = this.dispatch(this.ensureBackupThreadProvider());
			}
			return this.backupThreadProvider;
		}
		
		@Override
		public ProcessIdentityProvider getProcessIdentityProvider()
		{
			if(this.processIdentityProvider == null)
			{
				this.processIdentityProvider = this.dispatch(this.ensureProcessIdentityProvider());
			}
			return this.processIdentityProvider;
		}
		
		@Override
		public StorageLockFileManagerThreadProvider getLockFileManagerThreadProvider()
		{
			if(this.lockFileManagerThreadProvider == null)
			{
				this.lockFileManagerThreadProvider = this.dispatch(this.ensureLockFileManagerThreadProvider());
			}
			return this.lockFileManagerThreadProvider;
		}
		
		@Override
		public StorageThreadProvider getThreadProvider()
		{
			if(this.threadProvider == null)
			{
				this.threadProvider = this.dispatch(this.ensureThreadProvider());
			}
			return this.threadProvider;
		}

		@Override
		public StorageRequestTaskCreator getRequestTaskCreator()
		{
			if(this.requestTaskCreator == null)
			{
				this.requestTaskCreator = this.dispatch(this.ensureRequestTaskCreator());
			}
			return this.requestTaskCreator;
		}

		@Override
		public StorageTypeDictionary getTypeDictionary()
		{
			if(this.typeDictionary == null)
			{
				this.typeDictionary = this.dispatch(this.ensureTypeDictionary());
			}
			return this.typeDictionary;
		}

		@Override
		public StorageRootTypeIdProvider getRootTypeIdProvider()
		{
			if(this.rootTypeIdProvider == null)
			{
				this.rootTypeIdProvider = this.dispatch(this.ensureRootTypeIdProvider());
			}
			return this.rootTypeIdProvider;
		}

		@Override
		public StorageConfiguration getConfiguration()
		{
			if(this.configuration == null)
			{
				this.configuration = this.dispatch(this.ensureConfiguration());
			}
			return this.configuration;
		}

		@Override
		public StorageTimestampProvider getTimestampProvider()
		{
			if(this.timestampProvider == null)
			{
				this.timestampProvider = this.dispatch(this.ensureTimestampProvider());
			}
			return this.timestampProvider;
		}

		@Override
		public StorageObjectIdRangeEvaluator getObjectIdRangeEvaluator()
		{
			if(this.objectIdRangeEvaluator == null)
			{
				this.objectIdRangeEvaluator = this.dispatch(this.ensureObjectIdRangeEvaluator());
			}
			return this.objectIdRangeEvaluator;
		}

		@Override
		public StorageFileWriter.Provider getWriterProvider()
		{
			if(this.writerProvider == null)
			{
				this.writerProvider = this.dispatch(this.ensureWriterProvider());
			}
			return this.writerProvider;
		}

		@Override
		public StorageGCZombieOidHandler getGCZombieOidHandler()
		{
			if(this.gCZombieOidHandler == null)
			{
				this.gCZombieOidHandler = this.dispatch(this.ensureStorageGCZombieOidHandler());
			}
			return this.gCZombieOidHandler;
		}

		@Override
		public StorageRootOidSelector.Provider getRootOidSelectorProvider()
		{
			if(this.rootOidSelectorProvider == null)
			{
				this.rootOidSelectorProvider = this.dispatch(this.ensureRootOidSelectorProvider());
			}
			return this.rootOidSelectorProvider;
		}

		@Override
		public StorageObjectIdMarkQueue.Creator getOidMarkQueueCreator()
		{
			if(this.oidMarkQueueCreator == null)
			{
				this.oidMarkQueueCreator = this.dispatch(this.ensureOidMarkQueueCreator());
			}
			return this.oidMarkQueueCreator;
		}

		@Override
		public StorageEntityMarkMonitor.Creator getEntityMarkMonitorCreator()
		{
			if(this.entityMarkMonitorCreator == null)
			{
				this.entityMarkMonitorCreator = this.dispatch(this.ensureEntityMarkMonitorCreator());
			}
			return this.entityMarkMonitorCreator;
		}
		
		@Override
		public StorageDataFileValidator.Creator getDataFileValidatorCreator()
		{
			if(this.dataFileValidatorCreator == null)
			{
				this.dataFileValidatorCreator = this.dispatch(this.ensureDataFileValidatorCreator());
			}
			return this.dataFileValidatorCreator;
		}
		
		@Override
		public BinaryEntityRawDataIterator.Provider getEntityDataIteratorProvider()
		{
			if(this.entityDataIteratorProvider == null)
			{
				this.entityDataIteratorProvider = this.dispatch(this.ensureEntityDataIteratorProvider());
			}
			return this.entityDataIteratorProvider;
		}
		
		@Override
		public StorageEntityDataValidator.Creator getEntityDataValidatorCreator()
		{
			if(this.entityDataValidatorCreator == null)
			{
				this.entityDataValidatorCreator = this.dispatch(this.ensureEntityDataValidatorCreator());
			}
			return this.entityDataValidatorCreator;
		}
		

		@Override
		public StorageLockFileSetup getLockFileSetup()
		{
			if(this.lockFileSetup == null)
			{
				this.lockFileSetup = this.dispatch(this.provideLockFileSetup());
			}
			return this.lockFileSetup;
		}
		
		@Override
		public StorageLockFileSetup.Provider getLockFileSetupProvider()
		{
			// intentionally no ensuring since the lock file mechanism is off by default
			return this.lockFileSetupProvider;
		}
		
		@Override
		public StorageLockFileManager.Creator getLockFileManagerCreator()
		{
			if(this.lockFileManagerCreator == null)
			{
				this.lockFileManagerCreator = this.dispatch(this.ensureLockFileManagerCreator());
			}
			return this.lockFileManagerCreator;
		}

		@Override
		public StorageExceptionHandler getExceptionHandler()
		{
			if(this.exceptionHandler == null)
			{
				this.exceptionHandler = this.dispatch(this.ensureExceptionHandler());
			}
			return this.exceptionHandler;
		}
		
		@Override
		public StorageEventLogger getEventLogger()
		{
			if(this.eventLogger == null)
			{
				this.eventLogger = this.dispatch(this.ensureEventLogger());
			}
			return this.eventLogger;
		}
		
		@Override
		public StorageWriteController writeController()
		{
			return this.writeController;
		}
		
		@Override
		public StorageWriteController getWriteController()
		{
			if(this.writeController == null)
			{
				this.writeController = this.dispatch(this.ensureWriteController());
			}
			return this.writeController;
		}
		
		@Override
		public StorageHousekeepingBroker housekeepingBroker()
		{
			return this.housekeepingBroker;
		}
		
		@Override
		public StorageHousekeepingBroker getHousekeepingBroker()
		{
			if(this.housekeepingBroker == null)
			{
				this.housekeepingBroker = this.dispatch(this.ensureHousekeepingBroker());
			}
			return this.housekeepingBroker;
		}
		
		@Override
		public final ObjectIdsSelector getLiveObjectIdChecker()
		{
			if(this.liveObjectIdChecker == null)
			{
				this.liveObjectIdChecker = this.dispatch(this.ensureObjectIdsSelector());
			}
			return this.liveObjectIdChecker;
		}

		@Override
		public final Reference<PersistenceLiveStorerRegistry> getLiveStorerRegistryReference()
		{
			if(this.storerRegistryReference == null)
			{
				this.storerRegistryReference = this.dispatch(this.ensureLiveStorerRegistryReference());
			}
			return this.storerRegistryReference;
		}

		@Override
		public StorageStructureValidator getStorageStructureValidator()
		{
			if(this.storageStructureValidator == null)
			{
				this.storageStructureValidator = this.dispatch(this.ensureStorageStructureValidator());
			}
			return this.storageStructureValidator;
		}
		
		
		@Override
		public F setOperationControllerCreator(
			final StorageOperationController.Creator operationControllerCreator
		)
		{
			this.operationControllerCreator = operationControllerCreator;
			return this.$();
		}
		
		@Override
		public F setInitialDataFileNumberProvider(
			final StorageInitialDataFileNumberProvider initialDataFileNumberProvider
		)
		{
			this.initialDataFileNumberProvider = initialDataFileNumberProvider;
			return this.$();
		}

		@Override
		public F setRequestAcceptorCreator(
			final StorageRequestAcceptor.Creator requestAcceptorCreator
		)
		{
			this.requestAcceptorCreator = requestAcceptorCreator;
			return this.$();
		}

		@Override
		public F setTaskBrokerCreator(final StorageTaskBroker.Creator taskBrokerCreator)
		{
			this.taskBrokerCreator = taskBrokerCreator;
			return this.$();
		}

		@Override
		public F setDataChunkValidatorProvider(
			final StorageDataChunkValidator.Provider dataChunkValidatorProvider
		)
		{
			this.dataChunkValidatorProvider = dataChunkValidatorProvider;
			return this.$();
		}
		
		@Override
		public F setDataChunkValidatorProvider2(
			final StorageDataChunkValidator.Provider2 dataChunkValidatorProvider2
		)
		{
			this.dataChunkValidatorProvider2 = dataChunkValidatorProvider2;
			return this.$();
		}

		@Override
		public F setChannelCreator(final StorageChannelsCreator channelCreator)
		{
			this.channelCreator = channelCreator;
			return this.$();
		}
		
		@Override
		public F setThreadNameProvider(final StorageThreadNameProvider threadNameProvider)
		{
			this.threadNameProvider = threadNameProvider;
			return this.$();
		}

		@Override
		public F setChannelThreadProvider(final StorageChannelThreadProvider channelThreadProvider)
		{
			this.channelThreadProvider = channelThreadProvider;
			return this.$();
		}
		
		@Override
		public F setBackupThreadProvider(final StorageBackupThreadProvider backupThreadProvider)
		{
			this.backupThreadProvider = backupThreadProvider;
			return this.$();
		}

		@Override
		public F setLockFileManagerThreadProvider(final StorageLockFileManagerThreadProvider lockFileManagerThreadProvider)
		{
			this.lockFileManagerThreadProvider = lockFileManagerThreadProvider;
			return this.$();
		}
		
		@Override
		public F setThreadProvider(final StorageThreadProvider threadProvider)
		{
			this.threadProvider = threadProvider;
			return this.$();
		}

		@Override
		public F setTaskCreator(final StorageRequestTaskCreator taskCreator)
		{
			this.requestTaskCreator = taskCreator;
			return this.$();
		}

		@Override
		public F setTypeDictionary(final StorageTypeDictionary typeDictionary)
		{
			this.typeDictionary = typeDictionary;
			return this.$();
		}

		@Override
		public F setRootTypeIdProvider(final StorageRootTypeIdProvider rootTypeIdProvider)
		{
			this.rootTypeIdProvider = rootTypeIdProvider;
			return this.$();
		}

		@Override
		public F setConfiguration(final StorageConfiguration configuration)
		{
			this.configuration = configuration;
			return this.$();
		}

		@Override
		public F setTimestampProvider(
			final StorageTimestampProvider timestampProvider
		)
		{
			this.timestampProvider = timestampProvider;
			return this.$();
		}

		@Override
		public F setObjectIdRangeEvaluator(
			final StorageObjectIdRangeEvaluator objectIdRangeEvaluator
		)
		{
			this.objectIdRangeEvaluator = objectIdRangeEvaluator;
			return this.$();
		}

		@Override
		public F setWriterProvider(final Provider writerProvider)
		{
			this.writerProvider = writerProvider;
			return this.$();
		}

		@Override
		public F setGCZombieOidHandler(final StorageGCZombieOidHandler gCZombieOidHandler)
		{
			this.gCZombieOidHandler = gCZombieOidHandler;
			return this.$();
		}

		@Override
		public F setRootOidSelectorProvider(
			final StorageRootOidSelector.Provider rootOidSelectorProvider
		)
		{
			this.rootOidSelectorProvider = rootOidSelectorProvider;
			return this.$();
		}

		@Override
		public F setOidMarkQueueCreator(
			final StorageObjectIdMarkQueue.Creator oidMarkQueueCreator)
		{
			this.oidMarkQueueCreator = oidMarkQueueCreator;
			return this.$();
		}

		@Override
		public F setEntityMarkMonitorCreator(
			final StorageEntityMarkMonitor.Creator entityMarkMonitorCreator
		)
		{
			this.entityMarkMonitorCreator = entityMarkMonitorCreator;
			return this.$();
		}
		
		@Override
		public F setDataFileValidatorCreator(
			final StorageDataFileValidator.Creator dataFileValidatorCreator
		)
		{
			this.dataFileValidatorCreator = dataFileValidatorCreator;
			return this.$();
		}
		
		@Override
		public F setEntityDataIteratorProvider(
			final BinaryEntityRawDataIterator.Provider entityDataIteratorProvider
		)
		{
			this.entityDataIteratorProvider = entityDataIteratorProvider;
			return this.$();
		}
		
		@Override
		public F setEntityDataValidatorCreator(
			final StorageEntityDataValidator.Creator entityDataValidatorCreator
		)
		{
			this.entityDataValidatorCreator = entityDataValidatorCreator;
			return this.$();
		}
		
		@Override
		public F setProcessIdentityProvider(
			final ProcessIdentityProvider processIdentityProvider
		)
		{
			this.processIdentityProvider = processIdentityProvider;
			return this.$();
		}
		
		@Override
		public F setLockFileSetup(
			final StorageLockFileSetup lockFileSetup
		)
		{
			this.lockFileSetup = lockFileSetup;
			return this.$();
		}
		
		@Override
		public F setLockFileSetupProvider(
			final StorageLockFileSetup.Provider lockFileSetupProvider
		)
		{
			this.lockFileSetupProvider = lockFileSetupProvider;
			return this.$();
		}
		
		@Override
		public F setLockFileManagerCreator(
			final StorageLockFileManager.Creator lockFileManagerCreator
		)
		{
			this.lockFileManagerCreator = lockFileManagerCreator;
			return this.$();
		}

		@Override
		public F setExceptionHandler(final StorageExceptionHandler exceptionHandler)
		{
			this.exceptionHandler = exceptionHandler;
			return this.$();
		}
		
		@Override
		public F addEventLogger(final StorageEventLogger eventLogger)
		{
			this.eventLogger = eventLogger;
			notNull(eventLogger);
			
			this.eventLogger = this.eventLogger != null
				? StorageEventLogger.Chain(this.eventLogger, eventLogger)
				: eventLogger
			;
			return this.$();
		}
		
		@Override
		public F setWriteController(final StorageWriteController writeController)
		{
			this.writeController = writeController;
			
			return this.$();
		}
		
		@Override
		public F setHousekeepingBroker(final StorageHousekeepingBroker housekeepingBroker)
		{
			this.housekeepingBroker = housekeepingBroker;
			
			return this.$();
		}
		
		@Override
		public F setLiveObjectIdChecker(final ObjectIdsSelector liveObjectIdChecker)
		{
			this.liveObjectIdChecker = liveObjectIdChecker;
			return this.$();
		}

		@Override
		public final F setLiveStorerRegistryReference(final Reference<PersistenceLiveStorerRegistry> refLiveStorerRegistry)
		{
			this.storerRegistryReference = refLiveStorerRegistry;
			return this.$();
		}
		
		@Override
		public F setStorageStructureValidator(final StorageStructureValidator storageStructureValidator)
		{
			this.storageStructureValidator = storageStructureValidator;
			return this.$();
		}
		
		public final boolean isByteOrderMismatch()
		{
			/* (11.02.2019 TM)NOTE: On byte order switching:
			 * Theoreticaly, the storage engine (OGS) could also use the switchByteOrder mechanism implemented for
			 * communication (OGC). However, there are a lot stumbling blocks in the details that are currently not
			 * worth resolving for a feature that is most probably never required in the foreseeable future.
			 * See StorageEntityCache$Default#putEntity
			 */
			return false;
		}
		
		@Override
		public StorageSystem createStorageSystem()
		{
			/* (11.02.2019 TM)NOTE: On byte order switching:
			 * Theoreticaly, the storage engine (OGS) could use the switchByteOrder mechanism implemented for
			 * communiction (OGC). However, there are a lot stumbling blocks involved in the details that
			 * are currently not worth resolving for a feature that is most probably never required in the
			 * foreseeable future.
			 * See StorageEntityCache$Default#putEntity
			 */
						
			return new StorageSystem.Default(
				this.getConfiguration()                ,
				this.getOperationControllerCreator()   ,
				this.getDataFileValidatorCreator()     ,
				this.getWriteController()              ,
				this.getHousekeepingBroker()           ,
				this.getWriterProvider()               ,
				this.getInitialDataFileNumberProvider(),
				this.getRequestAcceptorCreator()       ,
				this.getTaskBrokerCreator()            ,
				this.getDataChunkValidatorProvider()   ,
				this.getChannelCreator()               ,
				this.getThreadProvider()               ,
				this.getRequestTaskCreator()           ,
				this.getTypeDictionary()               ,
				this.getRootTypeIdProvider()           ,
				this.getTimestampProvider()            ,
				this.getObjectIdRangeEvaluator()       ,
				this.getGCZombieOidHandler()           ,
				this.getRootOidSelectorProvider()      ,
				this.getOidMarkQueueCreator()          ,
				this.getEntityMarkMonitorCreator()     ,
				this.isByteOrderMismatch()             ,
				this.getLockFileSetup()                ,
				this.getLockFileManagerCreator()       ,
				this.getExceptionHandler()             ,
				this.getEventLogger()                  ,
				this.getLiveObjectIdChecker()          ,
				this.getLiveStorerRegistryReference()  ,
				this.getStorageStructureValidator()
			);
		}

	}

}
