package one.microstream.io;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import one.microstream.chars.XChars;

public class MainTestCopyFile
{
	static final Path file1 = XIO.Path("file1.dat");
	static final Path file2 = XIO.Path("file2.dat");
	
	public static void main(final String[] args) throws IOException
	{
		cleanup();
		
		XIO.write(file1, "0123456789", XChars.utf8());
		XIO.write(file2, "ABCDEFGHIJKLMNOPQRSTUVWXYZ", XChars.utf8());

		print("Setup:");
		
		final FileChannel fc1 = FileChannel.open(file1, StandardOpenOption.READ, StandardOpenOption.WRITE);
		final FileChannel fc2 = FileChannel.open(file2, StandardOpenOption.READ, StandardOpenOption.WRITE);
		
		System.out.println("\ntransferFrom() ...\n");
		fc2.transferFrom(fc1, fc2.size(), fc1.size());
		
		print("Result:");
		
		cleanup();
	}
	
	static void print(final String label) throws IOException
	{
		System.out.println(label);
		System.out.println("file1 = " + XIO.readString(file1));
		System.out.println("file2 = " + XIO.readString(file2));
	}
	
	static void cleanup() throws IOException
	{
		XIO.delete(file1);
		XIO.delete(file2);
	}
}
