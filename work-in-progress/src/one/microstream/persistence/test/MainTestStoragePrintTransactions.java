package one.microstream.persistence.test;

import one.microstream.afs.nio.NioFileSystem;
import one.microstream.chars.VarString;
import one.microstream.io.XIO;
import one.microstream.storage.types.StorageTransactionsAnalysis;

public class MainTestStoragePrintTransactions
{
	public static void main(final String[] args)
	{
		printTransactionsFiles(1);
	}
	
	public static void printTransactionsFiles(final int channelCount)
	{
		final NioFileSystem nfs = NioFileSystem.New();
		
		System.out.println(StorageTransactionsAnalysis.EntryAssembler.assembleHeader(VarString.New(), "\t"));
		for(int i = 0; i < channelCount; i++)
		{
			final VarString vs = StorageTransactionsAnalysis.Logic.parseFile(
				nfs.ensureFile(XIO.Path("storage/channel_"+i+"/transactions_"+i+".sft"))
			);
			System.out.println(vs);
		}
	}
	
}
