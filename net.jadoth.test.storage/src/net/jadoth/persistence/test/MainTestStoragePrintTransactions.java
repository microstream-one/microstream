package net.jadoth.persistence.test;

import java.io.File;

import net.jadoth.chars.VarString;
import net.jadoth.storage.types.StorageTransactionsFileAnalysis;

public class MainTestStoragePrintTransactions
{
	public static void main(final String[] args)
	{
		printTransactionsFiles(4);
	}
	
	public static void printTransactionsFiles(final int channelCount)
	{
		System.out.println(StorageTransactionsFileAnalysis.EntryAssembler.assembleHeader(VarString.New(), "\t"));
		for(int i = 0; i < channelCount; i++)
		{
			final VarString vs = StorageTransactionsFileAnalysis.Logic.parseFile(
				new File("C:/Files/channel_"+i+"/transactions_"+i+".sft")
			);
			System.out.println(vs);
		}
	}
	
}
