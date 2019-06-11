package one.microstream.storage.types;

import java.nio.ByteOrder;

import one.microstream.exceptions.MissingFoundationPartException;
import one.microstream.persistence.binary.types.BinaryEntityRawDataIterator;
import one.microstream.persistence.types.Unpersistable;
import one.microstream.storage.types.StorageDataChunkValidator.Provider2;
import one.microstream.storage.types.StorageFileWriter.Provider;
import one.microstream.util.InstanceDispatcher;
import one.microstream.util.ProcessIdentityProvider;


/**
 * A kind of factory type that holds and creates on demand all the parts that form a {@link StorageManager} instance,
 * i.e. a functional database handling logic.
 * <p>
 * Additionally to the services of a mere factory type, a foundation type also keeps references to all parts
 * after a {@link StorageManager} instance has been created. This is useful if some internal logic parts shall be
 * accessed while the {@link StorageManager} logic is already running. Therefore, this type can best be thought of
 * as a {@literal foundation} on which the running database handling logic stands.
 * <p>
 * All {@literal set~} methods are simple setter methods without any additional logic worth mentioning.<br>
 * All {@literal set~} methods return {@literal this} to allow for easy method chaining to improve readability.<br>
 * All {@literal get~} methods return a logic part instance, if present or otherwise creates and sets one beforehand
 * via a default creation logic.
 * 
 * @author TM
 *
 * @param <F> the "self-type" of the  {@link StorageFoundation} implementation.
 */
public interface StorageFoundation<F extends StorageFoundation<?>>
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
	 * executon of {@link #createStorageManager()}, a suitable instance is created via an internal default
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
	 * executon of {@link #createStorageManager()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return {@linkDoc StorageFoundation#getConfiguration()@return}
	 * 
	 * @throws {@linkDoc StorageFoundation#getConfiguration()@throws}
	 */
	public StorageOperationController.Creator getOperationControllerCreator();
	
	/**
	 * Returns the currently set {@link StorageInitialDataFileNumberProvider} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageManager()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return {@linkDoc StorageFoundation#getConfiguration()@return}
	 * 
	 * @throws {@linkDoc StorageFoundation#getConfiguration()@throws}
	 */
	public StorageInitialDataFileNumberProvider getInitialDataFileNumberProvider();
	
	/**
	 * Returns the currently set {@link StorageRequestAcceptor.Creator} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageManager()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return {@linkDoc StorageFoundation#getConfiguration()@return}
	 * 
	 * @throws {@linkDoc StorageFoundation#getConfiguration()@throws}
	 */
	public StorageRequestAcceptor.Creator getRequestAcceptorCreator();
	
	/**
	 * Returns the currently set {@link StorageTaskBroker.Creator} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageManager()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return {@linkDoc StorageFoundation#getConfiguration()@return}
	 * 
	 * @throws {@linkDoc StorageFoundation#getConfiguration()@throws}
	 */
	public StorageTaskBroker.Creator getTaskBrokerCreator();
	
	/**
	 * Returns the currently set {@link StorageDataChunkValidator.Provider} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageManager()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return {@linkDoc StorageFoundation#getConfiguration()@return}
	 * 
	 * @throws {@linkDoc StorageFoundation#getConfiguration()@throws}
	 */
	public StorageDataChunkValidator.Provider getDataChunkValidatorProvider();
	
	/**
	 * Returns the currently set {@link StorageDataChunkValidator.Provider2} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageManager()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return {@linkDoc StorageFoundation#getConfiguration()@return}
	 * 
	 * @throws {@linkDoc StorageFoundation#getConfiguration()@throws}
	 */
	public StorageDataChunkValidator.Provider2 getDataChunkValidatorProvider2();
	
	/**
	 * Returns the currently set {@link StorageChannelsCreator} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageManager()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return {@linkDoc StorageFoundation#getConfiguration()@return}
	 * 
	 * @throws {@linkDoc StorageFoundation#getConfiguration()@throws}
	 */
	public StorageChannelsCreator getChannelCreator();
	
	/**
	 * Returns the currently set {@link StorageChannelThreadProvider} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageManager()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return {@linkDoc StorageFoundation#getConfiguration()@return}
	 * 
	 * @throws {@linkDoc StorageFoundation#getConfiguration()@throws}
	 */
	public StorageChannelThreadProvider getChannelThreadProvider();
	
	/**
	 * Returns the currently set {@link StorageBackupThreadProvider} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageManager()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return {@linkDoc StorageFoundation#getConfiguration()@return}
	 * 
	 * @throws {@linkDoc StorageFoundation#getConfiguration()@throws}
	 */
	public StorageBackupThreadProvider getBackupThreadProvider();
	
	/**
	 * Returns the currently set {@link StorageLockFileManagerThreadProvider} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageManager()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return {@linkDoc StorageFoundation#getConfiguration()@return}
	 * 
	 * @throws {@linkDoc StorageFoundation#getConfiguration()@throws}
	 */
	public StorageLockFileManagerThreadProvider getLockFileManagerThreadProvider();
	
	/**
	 * Returns the currently set {@link StorageThreadProvider} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageManager()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return {@linkDoc StorageFoundation#getConfiguration()@return}
	 * 
	 * @throws {@linkDoc StorageFoundation#getConfiguration()@throws}
	 */
	public StorageThreadProvider getThreadProvider();
	
	/**
	 * Returns the currently set {@link StorageRequestTaskCreator} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageManager()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return {@linkDoc StorageFoundation#getConfiguration()@return}
	 * 
	 * @throws {@linkDoc StorageFoundation#getConfiguration()@throws}
	 */
	public StorageRequestTaskCreator getRequestTaskCreator();
	
	/**
	 * Returns the currently set {@link StorageTypeDictionary} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageManager()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return {@linkDoc StorageFoundation#getConfiguration()@return}
	 * 
	 * @throws {@linkDoc StorageFoundation#getConfiguration()@throws}
	 */
	public StorageTypeDictionary getTypeDictionary();
	
	/**
	 * Returns the currently set {@link StorageRootTypeIdProvider} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageManager()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return {@linkDoc StorageFoundation#getConfiguration()@return}
	 * 
	 * @throws {@linkDoc StorageFoundation#getConfiguration()@throws}
	 */
	public StorageRootTypeIdProvider getRootTypeIdProvider();
	
	/**
	 * Returns the currently set {@link StorageTimestampProvider} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageManager()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return {@linkDoc StorageFoundation#getConfiguration()@return}
	 * 
	 * @throws {@linkDoc StorageFoundation#getConfiguration()@throws}
	 */
	public StorageTimestampProvider getTimestampProvider();
	
	/**
	 * Returns the currently set {@link StorageObjectIdRangeEvaluator} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageManager()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return {@linkDoc StorageFoundation#getConfiguration()@return}
	 * 
	 * @throws {@linkDoc StorageFoundation#getConfiguration()@throws}
	 */
	public StorageObjectIdRangeEvaluator getObjectIdRangeEvaluator();
	
	/**
	 * Returns the currently set {@link StorageFileReader.Provider} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageManager()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return {@linkDoc StorageFoundation#getConfiguration()@return}
	 * 
	 * @throws {@linkDoc StorageFoundation#getConfiguration()@throws}
	 */
	public StorageFileReader.Provider getReaderProvider();
	
	/**
	 * Returns the currently set {@link StorageFileWriter.Provider} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageManager()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return {@linkDoc StorageFoundation#getConfiguration()@return}
	 * 
	 * @throws {@linkDoc StorageFoundation#getConfiguration()@throws}
	 */
	public StorageFileWriter.Provider getWriterProvider();
	
	/**
	 * Returns the currently set {@link StorageGCZombieOidHandler} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageManager()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return {@linkDoc StorageFoundation#getConfiguration()@return}
	 * 
	 * @throws {@linkDoc StorageFoundation#getConfiguration()@throws}
	 */
	public StorageGCZombieOidHandler getGCZombieOidHandler();
	
	/**
	 * Returns the currently set {@link StorageRootOidSelector.Provider} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageManager()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return {@linkDoc StorageFoundation#getConfiguration()@return}
	 * 
	 * @throws {@linkDoc StorageFoundation#getConfiguration()@throws}
	 */
	public StorageRootOidSelector.Provider getRootOidSelectorProvider();
	
	/**
	 * Returns the currently set {@link StorageOidMarkQueue.Creator} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageManager()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return {@linkDoc StorageFoundation#getConfiguration()@return}
	 * 
	 * @throws {@linkDoc StorageFoundation#getConfiguration()@throws}
	 */
	public StorageOidMarkQueue.Creator getOidMarkQueueCreator();
	
	/**
	 * Returns the currently set {@link StorageEntityMarkMonitor.Creator} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageManager()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return {@linkDoc StorageFoundation#getConfiguration()@return}
	 * 
	 * @throws {@linkDoc StorageFoundation#getConfiguration()@throws}
	 */
	public StorageEntityMarkMonitor.Creator getEntityMarkMonitorCreator();
	
	/**
	 * Returns the currently set {@link StorageDataFileValidator.Creator} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageManager()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return {@linkDoc StorageFoundation#getConfiguration()@return}
	 * 
	 * @throws {@linkDoc StorageFoundation#getConfiguration()@throws}
	 */
	public StorageDataFileValidator.Creator getDataFileValidatorCreator();
	
	/**
	 * Returns the currently set {@link BinaryEntityRawDataIterator.Provider} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageManager()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return {@linkDoc StorageFoundation#getConfiguration()@return}
	 * 
	 * @throws {@linkDoc StorageFoundation#getConfiguration()@throws}
	 */
	public BinaryEntityRawDataIterator.Provider getEntityDataIteratorProvider();
	
	/**
	 * Returns the currently set {@link StorageEntityDataValidator.Creator} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageManager()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return {@linkDoc StorageFoundation#getConfiguration()@return}
	 * 
	 * @throws {@linkDoc StorageFoundation#getConfiguration()@throws}
	 */
	public StorageEntityDataValidator.Creator getEntityDataValidatorCreator();
	
	/**
	 * Returns the currently set {@link ProcessIdentityProvider} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageManager()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return {@linkDoc StorageFoundation#getConfiguration()@return}
	 * 
	 * @throws {@linkDoc StorageFoundation#getConfiguration()@throws}
	 */
	public ProcessIdentityProvider getProcessIdentityProvider();
	
	/**
	 * Returns the currently set {@link StorageLockFileSetup} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageManager()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return {@linkDoc StorageFoundation#getConfiguration()@return}
	 * 
	 * @throws {@linkDoc StorageFoundation#getConfiguration()@throws}
	 */
	public StorageLockFileSetup getLockFileSetup();
	
	/**
	 * Returns the currently set {@link StorageLockFileSetup.Provider} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageManager()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return {@linkDoc StorageFoundation#getConfiguration()@return}
	 * 
	 * @throws {@linkDoc StorageFoundation#getConfiguration()@throws}
	 */
	public StorageLockFileSetup.Provider getLockFileSetupProvider();
	
	/**
	 * Returns the currently set {@link StorageLockFileManager.Creator} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageManager()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return {@linkDoc StorageFoundation#getConfiguration()@return}
	 * 
	 * @throws {@linkDoc StorageFoundation#getConfiguration()@throws}
	 */
	public StorageLockFileManager.Creator getLockFileManagerCreator();
	
	/**
	 * Returns the currently set {@link StorageExceptionHandler} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createStorageManager()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return {@linkDoc StorageFoundation#getConfiguration()@return}
	 * 
	 * @throws {@linkDoc StorageFoundation#getConfiguration()@throws}
	 */
	public StorageExceptionHandler getExceptionHandler();

	
	
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
	 * @return {@linkDoc StorageFoundation#setConfiguration(StorageConfiguration)@return}
	 */
	public F setOperationControllerCreator(StorageOperationController.Creator operationControllerCreator);
	
	/**
	 * Sets the {@link StorageInitialDataFileNumberProvider} instance to be used for the assembly.
	 * 
	 * @param initDataFileNumberProvider the instance to be used.
	 * 
	 * @return {@linkDoc StorageFoundation#setConfiguration(StorageConfiguration)@return}
	 */
	public F setInitialDataFileNumberProvider(StorageInitialDataFileNumberProvider initDataFileNumberProvider);
	
	/**
	 * Sets the {@link StorageRequestAcceptor.Creator} instance to be used for the assembly.
	 * 
	 * @param requestAcceptorCreator the instance to be used.
	 * 
	 * @return {@linkDoc StorageFoundation#setConfiguration(StorageConfiguration)@return}
	 */
	public F setRequestAcceptorCreator(StorageRequestAcceptor.Creator requestAcceptorCreator);
	
	/**
	 * Sets the {@link StorageTaskBroker.Creator} instance to be used for the assembly.
	 * 
	 * @param taskBrokerCreator the instance to be used.
	 * 
	 * @return {@linkDoc StorageFoundation#setConfiguration(StorageConfiguration)@return}
	 */
	public F setTaskBrokerCreator(StorageTaskBroker.Creator taskBrokerCreator);
	
	/**
	 * Sets the {@link StorageDataChunkValidator.Provider} instance to be used for the assembly.
	 * 
	 * @param dataChunkValidatorProvider the instance to be used.
	 * 
	 * @return {@linkDoc StorageFoundation#setConfiguration(StorageConfiguration)@return}
	 */
	public F setDataChunkValidatorProvider(StorageDataChunkValidator.Provider dataChunkValidatorProvider);
	
	/**
	 * Sets the {@link StorageDataChunkValidator.Provider2} instance to be used for the assembly.
	 * 
	 * @param dataChunkValidatorProvider2 the instance to be used.
	 * 
	 * @return {@linkDoc StorageFoundation#setConfiguration(StorageConfiguration)@return}
	 */
	public F setDataChunkValidatorProvider2(StorageDataChunkValidator.Provider2 dataChunkValidatorProvider2);
	
	/**
	 * Sets the {@link StorageChannelsCreator} instance to be used for the assembly.
	 * 
	 * @param channelCreator the instance to be used.
	 * 
	 * @return {@linkDoc StorageFoundation#setConfiguration(StorageConfiguration)@return}
	 */
	public F setChannelCreator(StorageChannelsCreator channelCreator);
	
	/**
	 * Sets the {@link StorageChannelThreadProvider} instance to be used for the assembly.
	 * 
	 * @param channelThreadProvider the instance to be used.
	 * 
	 * @return {@linkDoc StorageFoundation#setConfiguration(StorageConfiguration)@return}
	 */
	public F setChannelThreadProvider(StorageChannelThreadProvider channelThreadProvider);
	
	/**
	 * Sets the {@link StorageBackupThreadProvider} instance to be used for the assembly.
	 * 
	 * @param backupThreadProvider the instance to be used.
	 * 
	 * @return {@linkDoc StorageFoundation#setConfiguration(StorageConfiguration)@return}
	 */
	public F setBackupThreadProvider(StorageBackupThreadProvider backupThreadProvider);
	
	/**
	 * Sets the {@link StorageLockFileManagerThreadProvider} instance to be used for the assembly.
	 * 
	 * @param lockFileManagerThreadProvider the instance to be used.
	 * 
	 * @return {@linkDoc StorageFoundation#setConfiguration(StorageConfiguration)@return}
	 */
	public F setLockFileManagerThreadProvider(StorageLockFileManagerThreadProvider lockFileManagerThreadProvider);
	
	/**
	 * Sets the {@link StorageThreadProvider} instance to be used for the assembly.
	 * 
	 * @param threadProvider the instance to be used.
	 * 
	 * @return {@linkDoc StorageFoundation#setConfiguration(StorageConfiguration)@return}
	 */
	public F setThreadProvider(StorageThreadProvider threadProvider);
	
	/**
	 * Sets the {@link StorageRequestTaskCreator} instance to be used for the assembly.
	 * 
	 * @param taskCreator the instance to be used.
	 * 
	 * @return {@linkDoc StorageFoundation#setConfiguration(StorageConfiguration)@return}
	 */
	public F setTaskCreator(StorageRequestTaskCreator taskCreator);
	
	/**
	 * Sets the {@link StorageTypeDictionary} instance to be used for the assembly.
	 * 
	 * @param typeDictionary the instance to be used.
	 * 
	 * @return {@linkDoc StorageFoundation#setConfiguration(StorageConfiguration)@return}
	 */
	public F setTypeDictionary(StorageTypeDictionary typeDictionary);
	
	/**
	 * Sets the {@link StorageRootTypeIdProvider} instance to be used for the assembly.
	 * 
	 * @param rootTypeIdProvider the instance to be used.
	 * 
	 * @return {@linkDoc StorageFoundation#setConfiguration(StorageConfiguration)@return}
	 */
	public F setRootTypeIdProvider(StorageRootTypeIdProvider rootTypeIdProvider);
	
	/**
	 * Sets the {@link StorageTimestampProvider} instance to be used for the assembly.
	 * 
	 * @param timestampProvider the instance to be used.
	 * 
	 * @return {@linkDoc StorageFoundation#setConfiguration(StorageConfiguration)@return}
	 */
	public F setTimestampProvider(StorageTimestampProvider timestampProvider);
	
	/**
	 * Sets the {@link StorageObjectIdRangeEvaluator} instance to be used for the assembly.
	 * 
	 * @param objectIdRangeEvaluator the instance to be used.
	 * 
	 * @return {@linkDoc StorageFoundation#setConfiguration(StorageConfiguration)@return}
	 */
	public F setObjectIdRangeEvaluator(StorageObjectIdRangeEvaluator objectIdRangeEvaluator);
	
	/**
	 * Sets the {@link StorageFileReader.Provider} instance to be used for the assembly.
	 * 
	 * @param readerProvider the instance to be used.
	 * 
	 * @return {@linkDoc StorageFoundation#setConfiguration(StorageConfiguration)@return}
	 */
	public F setReaderProvider(StorageFileReader.Provider readerProvider);
	
	/**
	 * Sets the {@link StorageFileWriter.Provider} instance to be used for the assembly.
	 * 
	 * @param writerProvider the instance to be used.
	 * 
	 * @return {@linkDoc StorageFoundation#setConfiguration(StorageConfiguration)@return}
	 */
	public F setWriterProvider(StorageFileWriter.Provider writerProvider);
	
	/**
	 * Sets the {@link StorageGCZombieOidHandler} instance to be used for the assembly.
	 * 
	 * @param gCZombieOidHandler the instance to be used.
	 * 
	 * @return {@linkDoc StorageFoundation#setConfiguration(StorageConfiguration)@return}
	 */
	public F setGCZombieOidHandler(StorageGCZombieOidHandler gCZombieOidHandler);
	
	/**
	 * Sets the {@link StorageRootOidSelector.Provider} instance to be used for the assembly.
	 * 
	 * @param rootOidSelectorProvider the instance to be used.
	 * 
	 * @return {@linkDoc StorageFoundation#setConfiguration(StorageConfiguration)@return}
	 */
	public F setRootOidSelectorProvider(StorageRootOidSelector.Provider rootOidSelectorProvider);
	
	/**
	 * Sets the {@link StorageOidMarkQueue.Creator} instance to be used for the assembly.
	 * 
	 * @param oidMarkQueueCreator the instance to be used.
	 * 
	 * @return {@linkDoc StorageFoundation#setConfiguration(StorageConfiguration)@return}
	 */
	public F setOidMarkQueueCreator(StorageOidMarkQueue.Creator oidMarkQueueCreator);
	
	/**
	 * Sets the {@link StorageEntityMarkMonitor.Creator} instance to be used for the assembly.
	 * 
	 * @param entityMarkMonitorCreator the instance to be used.
	 * 
	 * @return {@linkDoc StorageFoundation#setConfiguration(StorageConfiguration)@return}
	 */
	public F setEntityMarkMonitorCreator(StorageEntityMarkMonitor.Creator entityMarkMonitorCreator);
	
	/**
	 * Sets the {@link StorageDataFileValidator.Creator} instance to be used for the assembly.
	 * 
	 * @param dataFileValidatorCreator the instance to be used.
	 * 
	 * @return {@linkDoc StorageFoundation#setConfiguration(StorageConfiguration)@return}
	 */
	public F setDataFileValidatorCreator(StorageDataFileValidator.Creator dataFileValidatorCreator);
	
	/**
	 * Sets the {@link BinaryEntityRawDataIterator.Provider} instance to be used for the assembly.
	 * 
	 * @param entityRawDataIteratorProvider the instance to be used.
	 * 
	 * @return {@linkDoc StorageFoundation#setConfiguration(StorageConfiguration)@return}
	 */
	public F setEntityDataIteratorProvider(BinaryEntityRawDataIterator.Provider entityRawDataIteratorProvider);
	
	/**
	 * Sets the {@link StorageEntityDataValidator.Creator} instance to be used for the assembly.
	 * 
	 * @param entityDataValidatorCreator the instance to be used.
	 * 
	 * @return {@linkDoc StorageFoundation#setConfiguration(StorageConfiguration)@return}
	 */
	public F setEntityDataValidatorCreator(StorageEntityDataValidator.Creator entityDataValidatorCreator);
	
	/**
	 * Sets the {@link ProcessIdentityProvider} instance to be used for the assembly.
	 * 
	 * @param processIdentityProvider the instance to be used.
	 * 
	 * @return {@linkDoc StorageFoundation#setConfiguration(StorageConfiguration)@return}
	 */
	public F setProcessIdentityProvider(ProcessIdentityProvider processIdentityProvider);
	
	/**
	 * Sets the {@link StorageLockFileSetup} instance to be used for the assembly.
	 * 
	 * @param lockFileSetup the instance to be used.
	 * 
	 * @return {@linkDoc StorageFoundation#setConfiguration(StorageConfiguration)@return}
	 */
	public F setLockFileSetup(StorageLockFileSetup lockFileSetup);
	
	/**
	 * Sets the {@link StorageLockFileSetup.Provider} instance to be used for the assembly.
	 * 
	 * @param lockFileSetupProvider the instance to be used.
	 * 
	 * @return {@linkDoc StorageFoundation#setConfiguration(StorageConfiguration)@return}
	 */
	public F setLockFileSetupProvider(StorageLockFileSetup.Provider lockFileSetupProvider);
	
	/**
	 * Sets the {@link StorageLockFileManager.Creator} instance to be used for the assembly.
	 * 
	 * @param lockFileManagerCreator the instance to be used.
	 * 
	 * @return {@linkDoc StorageFoundation#setConfiguration(StorageConfiguration)@return}
	 */
	public F setLockFileManagerCreator(StorageLockFileManager.Creator lockFileManagerCreator);
	
	/**
	 * Sets the {@link StorageExceptionHandler} instance to be used for the assembly.
	 * 
	 * @param exceptionHandler the instance to be used.
	 * 
	 * @return {@linkDoc StorageFoundation#setConfiguration(StorageConfiguration)@return}
	 */
	public F setExceptionHandler(StorageExceptionHandler exceptionHandler);

	
	
	/**
	 * Creates and returns a new {@link StorageManager} instance by using the current state of all registered
	 * logic part instances and by on-demand creating missing ones via a default logic.
	 * <p>
	 * The returned {@link StorageManager} instance will NOT yet be started.
	 * 
	 * @return a new {@link StorageManager} instance.
	 */
	public StorageManager createStorageManager();



	public class Default<F extends StorageFoundation.Default<?>>
	extends InstanceDispatcher.Default
	implements StorageFoundation<F>, Unpersistable
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private StorageConfiguration                  configuration                ;
		private StorageOperationController.Creator    operationControllerCreator   ;
		private StorageInitialDataFileNumberProvider  initialDataFileNumberProvider;
		private StorageRequestAcceptor.Creator        requestAcceptorCreator       ;
		private StorageTaskBroker.Creator             taskBrokerCreator            ;
		private StorageDataChunkValidator.Provider    dataChunkValidatorProvider   ;
		private StorageDataChunkValidator.Provider2   dataChunkValidatorProvider2  ;
		private StorageChannelsCreator                channelCreator               ;
		private StorageChannelThreadProvider          channelThreadProvider        ;
		private StorageBackupThreadProvider           backupThreadProvider         ;
		private ProcessIdentityProvider               processIdentityProvider      ;
		private StorageLockFileManagerThreadProvider  lockFileManagerThreadProvider;
		private StorageThreadProvider                 threadProvider               ;
		private StorageRequestTaskCreator             requestTaskCreator           ;
		private StorageTypeDictionary                 typeDictionary               ;
		private StorageRootTypeIdProvider             rootTypeIdProvider           ;
		private StorageTimestampProvider              timestampProvider            ;
		private StorageObjectIdRangeEvaluator         objectIdRangeEvaluator       ;
		private StorageFileReader.Provider            readerProvider               ;
		private StorageFileWriter.Provider            writerProvider               ;
		private StorageGCZombieOidHandler             gCZombieOidHandler           ;
		private StorageRootOidSelector.Provider       rootOidSelectorProvider      ;
		private StorageOidMarkQueue.Creator           oidMarkQueueCreator          ;
		private StorageEntityMarkMonitor.Creator      entityMarkMonitorCreator     ;
		private StorageDataFileValidator.Creator      dataFileValidatorCreator     ;
		private BinaryEntityRawDataIterator.Provider  entityDataIteratorProvider   ;
		private StorageEntityDataValidator.Creator    entityDataValidatorCreator   ;
		private StorageLockFileSetup                  lockFileSetup                ;
		private StorageLockFileSetup.Provider         lockFileSetupProvider        ;
		private StorageLockFileManager.Creator        lockFileManagerCreator       ;
		private StorageExceptionHandler               exceptionHandler             ;

		
		
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
			return this.getConfiguration().fileEvaluator();
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
			return getDataChunkValidatorProvider2().provideDataChunkValidatorProvider(this);
		}

		protected StorageDataChunkValidator.Provider2 ensureDataChunkValidatorProvider2()
		{
			return new StorageDataChunkValidator.NoOp();
		}

		protected StorageChannelsCreator ensureChannelCreator()
		{
			return new StorageChannelsCreator.Default();
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

		protected StorageFileReader.Provider ensureReaderProvider()
		{
			return new StorageFileReader.Provider.Default();
		}

		protected StorageFileWriter.Provider ensureWriterProvider()
		{
			return new StorageFileWriter.Provider.Default();
		}

		protected StorageRootOidSelector.Provider ensureRootOidSelectorProvider()
		{
			return new StorageRootOidSelector.Provider.Default();
		}

		protected StorageOidMarkQueue.Creator ensureOidMarkQueueCreator()
		{
			return new StorageOidMarkQueue.Creator.Default();
		}

		protected StorageEntityMarkMonitor.Creator ensureEntityMarkMonitorCreator()
		{
			return new StorageEntityMarkMonitor.Creator.Default();
		}

		protected StorageDataFileValidator.Creator ensureDataFileValidatorCreator()
		{
			return StorageDataFileValidator.Creator(
				this.getEntityDataIteratorProvider(),
				this.getEntityDataValidatorCreator()
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
		public StorageFileReader.Provider getReaderProvider()
		{
			if(this.readerProvider == null)
			{
				this.readerProvider = this.dispatch(this.ensureReaderProvider());
			}
			return this.readerProvider;
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
		public StorageOidMarkQueue.Creator getOidMarkQueueCreator()
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
		public F setReaderProvider(final StorageFileReader.Provider readerProvider)
		{
			this.readerProvider = readerProvider;
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
			final StorageOidMarkQueue.Creator oidMarkQueueCreator)
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
		public StorageManager createStorageManager()
		{
			/* (11.02.2019 TM)NOTE: On byte order switching:
			 * Theoreticaly, the storage engine (OGS) could use the switchByteOrder mechanism implemented for
			 * communiction (OGC). However, there are a lot stumbling blocks involved in the details that
			 * are currently not worth resolving for a feature that is most probably never required in the
			 * foreseeable future.
			 * See StorageEntityCache$Default#putEntity
			 */
						
			return new StorageManager.Default(
				this.getConfiguration()                ,
				this.getOperationControllerCreator()   ,
				this.getDataFileValidatorCreator()     ,
				this.getWriterProvider()               ,
				this.getReaderProvider()               ,
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
				this.getExceptionHandler()
			);
		}

	}

}
