package one.microstream.integrations.spring.boot.types.config;

/*-
 * #%L
 * microstream-integrations-spring-boot3
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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import one.microstream.integrations.spring.boot.types.StorageFilesystem;

@ConfigurationProperties(prefix = "one.microstream")
public class MicrostreamConfigurationProperties
{

    /**
     * The base directory of the storage in the file system. Default is "storage" in the working directory.
     */
    private String storageDirectory;

    /**
     * The live file system configuration
     */
    @NestedConfigurationProperty
    private StorageFilesystem storageFilesystem;

    /**
     * If configured, the storage will not delete files. Instead of deleting a file it will be moved to this directory.
     */
    private String deletionDirectory;

    /**
     * If configured, files that will get truncated are copied into this directory.
     */
    private String truncationDirectory;

    /**
     * The backup directory.
     */
    private String backupDirectory;

    /**
     * The backup file system configuration. See storage targets configuration.
     */
    @NestedConfigurationProperty
    private StorageFilesystem backupFilesystem;

    /**
     * The number of threads and number of directories used by the storage engine. Every thread has exclusive access to its directory. Default is 1.
     */
    private String channelCount;

    /**
     * Name prefix of the subdirectories used by the channel threads. Default is "channel_".
     */
    private String channelDirectoryPrefix;

    /**
     * Name prefix of the storage files. Default is "channel_".
     */
    private String dataFilePrefix;

    /**
     * Name suffix of the storage files. Default is ".dat".
     */
    private String dataFileSuffix;

    /**
     * Name prefix of the storage transaction file. Default is "transactions_".
     */
    private String transactionFilePrefix;

    /**
     * Name suffix of the storage transaction file. Default is ".sft".
     */
    private String transactionFileSuffix;

    /**
     * The name of the dictionary file. Default is "PersistenceTypeDictionary.ptd".
     */
    private String typeDictionaryFileName;

    /**
     * Name suffix of the storage rescue files. Default is ".bak".
     */
    private String rescuedFileSuffix;

    /**
     * Name of the lock file. Default is "used.lock".
     */
    private String lockFileName;

    /**
     * Interval for the housekeeping. This is work like garbage collection or cache checking.
     * In combination with houseKeepingNanoTimeBudget the maximum processor time for housekeeping work can be set.
     * Default is 1 second.
     */
    private String housekeepingInterval;

    /**
     * Number of nanoseconds used for each housekeeping cycle.
     * Default is 10 milliseconds = 0.01 seconds.
     */
    private String housekeepingTimeBudget;

    /**
     * Timeout in milliseconds for the entity cache evaluator.
     * If an entity wasn’t accessed in this timespan it will be removed from the cache. Default is 1 day.
     */
    private String entityCacheTimeout;

    /**
     * Minimum file size for a data file to avoid cleaning it up. Default is 1024^2 = 1 MiB.
     */
    private String dataFileMinimumSize;

    /**
     * Maximum file size for a data file to avoid cleaning it up. Default is 1024^2*8 = 8 MiB.
     */
    private String dataFileMaximumSize;

    /**
     * The ratio (value in ]0.0;1.0]) of non-gap data contained in a storage file to prevent the file from being dissolved. Default is 0.75 (75%).
     */
    private String dataFileMinimumUseRatio;

    /**
     * A flag defining whether the current head file (the only file actively written to) shall be subjected to file cleanups as well.
     */
    private String dataFileCleanupHeadFile;

    /**
     * Is the {@code StorageManager} started when the CDI bean for the instance is created or not.
     * Be aware that when you don't rely on the autostart of the StorageManager, you are responsible for starting it
     * and might result in Exceptions when code is executed that relies on a started StorageManager.
     * Default value is true.
     */
    private Boolean autoStart = true;


    public String getStorageDirectory()
    {
        return this.storageDirectory;
    }

    public void setStorageDirectory(final String storageDirectory)
    {
        this.storageDirectory = storageDirectory;
    }

    public StorageFilesystem getStorageFilesystem()
    {
        return this.storageFilesystem;
    }

    public void setStorageFilesystem(final StorageFilesystem storageFilesystem)
    {
        this.storageFilesystem = storageFilesystem;
    }

    public String getDeletionDirectory()
    {
        return this.deletionDirectory;
    }

    public void setDeletionDirectory(final String deletionDirectory)
    {
        this.deletionDirectory = deletionDirectory;
    }

    public String getTruncationDirectory()
    {
        return this.truncationDirectory;
    }

    public void setTruncationDirectory(final String truncationDirectory)
    {
        this.truncationDirectory = truncationDirectory;
    }

    public String getBackupDirectory()
    {
        return this.backupDirectory;
    }

    public void setBackupDirectory(final String backupDirectory)
    {
        this.backupDirectory = backupDirectory;
    }

    public StorageFilesystem getBackupFilesystem()
    {
        return this.backupFilesystem;
    }

    public void setBackupFilesystem(final StorageFilesystem backupFilesystem)
    {
        this.backupFilesystem = backupFilesystem;
    }

    public String getChannelCount()
    {
        return this.channelCount;
    }

    public void setChannelCount(final String channelCount)
    {
        this.channelCount = channelCount;
    }

    public String getChannelDirectoryPrefix()
    {
        return this.channelDirectoryPrefix;
    }

    public void setChannelDirectoryPrefix(final String channelDirectoryPrefix)
    {
        this.channelDirectoryPrefix = channelDirectoryPrefix;
    }

    public String getDataFilePrefix()
    {
        return this.dataFilePrefix;
    }

    public void setDataFilePrefix(final String dataFilePrefix)
    {
        this.dataFilePrefix = dataFilePrefix;
    }

    public String getDataFileSuffix()
    {
        return this.dataFileSuffix;
    }

    public void setDataFileSuffix(final String dataFileSuffix)
    {
        this.dataFileSuffix = dataFileSuffix;
    }

    public String getTransactionFilePrefix()
    {
        return this.transactionFilePrefix;
    }

    public void setTransactionFilePrefix(final String transactionFilePrefix)
    {
        this.transactionFilePrefix = transactionFilePrefix;
    }

    public String getTransactionFileSuffix()
    {
        return this.transactionFileSuffix;
    }

    public void setTransactionFileSuffix(final String transactionFileSuffix)
    {
        this.transactionFileSuffix = transactionFileSuffix;
    }

    public String getTypeDictionaryFileName()
    {
        return this.typeDictionaryFileName;
    }

    public void setTypeDictionaryFileName(final String typeDictionaryFileName)
    {
        this.typeDictionaryFileName = typeDictionaryFileName;
    }

    public String getRescuedFileSuffix()
    {
        return this.rescuedFileSuffix;
    }

    public void setRescuedFileSuffix(final String rescuedFileSuffix)
    {
        this.rescuedFileSuffix = rescuedFileSuffix;
    }

    public String getLockFileName()
    {
        return this.lockFileName;
    }

    public void setLockFileName(final String lockFileName)
    {
        this.lockFileName = lockFileName;
    }

    public String getHousekeepingInterval()
    {
        return this.housekeepingInterval;
    }

    public void setHousekeepingInterval(final String housekeepingInterval)
    {
        this.housekeepingInterval = housekeepingInterval;
    }

    public String getHousekeepingTimeBudget()
    {
        return this.housekeepingTimeBudget;
    }

    public void setHousekeepingTimeBudget(final String housekeepingTimeBudget)
    {
        this.housekeepingTimeBudget = housekeepingTimeBudget;
    }

    public String getEntityCacheTimeout()
    {
        return this.entityCacheTimeout;
    }

    public void setEntityCacheTimeout(final String entityCacheTimeout)
    {
        this.entityCacheTimeout = entityCacheTimeout;
    }

    public String getDataFileMinimumSize()
    {
        return this.dataFileMinimumSize;
    }

    public void setDataFileMinimumSize(final String dataFileMinimumSize)
    {
        this.dataFileMinimumSize = dataFileMinimumSize;
    }

    public String getDataFileMaximumSize()
    {
        return this.dataFileMaximumSize;
    }

    public void setDataFileMaximumSize(final String dataFileMaximumSize)
    {
        this.dataFileMaximumSize = dataFileMaximumSize;
    }

    public String getDataFileMinimumUseRatio()
    {
        return this.dataFileMinimumUseRatio;
    }

    public void setDataFileMinimumUseRatio(final String dataFileMinimumUseRatio)
    {
        this.dataFileMinimumUseRatio = dataFileMinimumUseRatio;
    }

    public String getDataFileCleanupHeadFile()
    {
        return this.dataFileCleanupHeadFile;
    }

    public void setDataFileCleanupHeadFile(final String dataFileCleanupHeadFile)
    {
        this.dataFileCleanupHeadFile = dataFileCleanupHeadFile;
    }

    public Boolean getAutoStart()
    {
        return this.autoStart;
    }

    public void setAutoStart(final Boolean autoStart)
    {
        this.autoStart = autoStart;
    }
}
