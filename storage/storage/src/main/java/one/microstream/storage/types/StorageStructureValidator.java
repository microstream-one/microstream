package one.microstream.storage.types;

import org.slf4j.Logger;

import one.microstream.afs.types.ADirectory;
import one.microstream.collections.EqHashEnum;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.storage.exceptions.StorageExceptionStructureValidation;
import one.microstream.util.logging.Logging;

/**
 * Run validation(s) of the storage before starting the storage system.
 * Those are
 * - number of channel directories must match the configured channel count
 *   if there are any channel directories
 * - all channel directories must contain at least on data file
 */
public interface StorageStructureValidator {

	/**
	 * run the validations. Throws StorageStructureValidationException
	 * in case of problems
	 */
	public void validate();
	
	public static StorageStructureValidator New(
		final StorageLiveFileProvider fileProvider,
		final StorageChannelCountProvider channelCountProvider)
	{
		return new StorageStructureValidator.Default(fileProvider, channelCountProvider);
	}
	
	public class Default implements StorageStructureValidator {
			
		private final static Logger logger = Logging.getLogger(Default.class);
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final StorageLiveFileProvider     fileProvider;
		private final StorageChannelCountProvider channelCountProvider;
		private final String                      dataFileType;

		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		public Default(
			final StorageLiveFileProvider fileProvider,
			final StorageChannelCountProvider channelCountProvider
		)
		{
			this.fileProvider = fileProvider;
			this.channelCountProvider = channelCountProvider;
			this.dataFileType = this.fileProvider.fileNameProvider().dataFileType();
		}

		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public void validate()
		{
			//check integrity of storage
			if(this.fileProvider.baseDirectory().exists())
			{
				if(this.fileProvider.baseDirectory().listDirectories().isEmpty())
				{
					logger.debug("Empty baseDirectory: {}", this.fileProvider.baseDirectory().toPathString());
				}
				else
				{
					final XGettingEnum<ADirectory> existingDirectories = this.getExistingChannelDirectories();
					this.validateChannelCount(existingDirectories);
					this.validateChannelDirectories(existingDirectories);
				}
			}
			
			logger.info("Storage structure validated successfully.");
		}
		
		private XGettingEnum<ADirectory> getExistingChannelDirectories()
		{
			final XGettingEnum<ADirectory> directories = this.fileProvider.baseDirectory().listDirectories();
			
			final String channelDirectoryPrefix = this.fileProvider.fileNameProvider().channelDirectoryPrefix();
			final EqHashEnum<ADirectory> existingChannelDirs = directories.filterTo(
				EqHashEnum.New() , f -> f.identifier().startsWith(channelDirectoryPrefix));
			
			return existingChannelDirs;
		}
		
		private void validateChannelCount(final XGettingEnum<ADirectory> directories)
		{
			if(
				directories.size() > 0 &&
				directories.size() != this.channelCountProvider.getChannelCount())
			{
				throw new StorageExceptionStructureValidation(
					"Found channels (" + directories.size() + ") don't match the configured channel count " +
					this.channelCountProvider.getChannelCount());
			}
			
			logger.debug("Configured channel count matches to storage");
		}
		
		private void validateChannelDirectories(final XGettingEnum<ADirectory> directories)
		{
			directories.forEach(this::validateChannelDirectory);
			
			logger.debug("Channel directories are valid");
		}
		
		private void validateChannelDirectory(final ADirectory channelDirectory)
		{
			if(!channelDirectory.listFiles().containsSearched( f->
				f.identifier().endsWith(this.dataFileType)))
			{
				throw new StorageExceptionStructureValidation(
					"No data file in channel directory " + channelDirectory.identifier() + " found!");
			}
		}

	}
	
}
