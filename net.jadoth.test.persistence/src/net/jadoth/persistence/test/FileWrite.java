package net.jadoth.persistence.test;

import java.io.File;
import java.io.FileOutputStream;


public class FileWrite
{
	public static void main(final String args[]) throws Throwable
	{
//		try
//		{
//			// Create file
//			final FileOutputStream out = new FileOutputStream(new File("c:/files/out.txt"));
//			out.write("Hello Java".getBytes());
////			final FileWriter fstream = new FileWriter("c:/files/out.txt");
////			final BufferedWriter out = new BufferedWriter(fstream);
////			out.write("Hello Java");
//			//Close the output stream
//			out.close();
//		}
//		catch (final Exception e){//Catch exception if any
//			System.err.println("Error: " + e.getMessage());
//		}


		try(final FileOutputStream out = new FileOutputStream(new File("c:/files/out.txt")))
		{
			out.write("Hello Java".getBytes());
		}


		System.out.println("done");
	}
}