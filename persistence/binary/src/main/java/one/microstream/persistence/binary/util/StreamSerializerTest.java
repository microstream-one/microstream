package one.microstream.persistence.binary.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StreamSerializerTest
{
	private static final String FILE_NAME       = "target/test.ser";
	private static final String TYPED_FILE_NAME = "target/typed-test.ser";


	public static void main(final String[] args) throws Throwable
	{
//		write();
		read();
		
//		writeTyped();
//		readTyped();
		
//		testPlain();
	}
	
	
	static void testPlain()
	{
		final List<String> list = new ArrayList<>();
		for(int i = 0; i < 25_000; i++)
		{
			list.add(UUID.randomUUID().toString());
		}
		
		final byte[] data = Serializer.Bytes(
			SerializerFoundation.New()
				.registerEntityTypes(ArrayList.class)
		)
		.serialize(list);
		
		final Object deserialized = Serializer.Bytes(
			SerializerFoundation.New()
				.registerEntityTypes(ArrayList.class)
		)
		.deserialize(data);
		
		System.out.println();
	}
	
	
	static void write() throws Throwable
	{
		final Object data = createTestData();

		try(FileOutputStream out = new FileOutputStream(FILE_NAME))
		{
			createStreamSerializer().serialize(data, out);
		}
		
		System.out.println("File Written");
	}
	
	
	static void read() throws Throwable
	{
		try(FileInputStream fin = new FileInputStream(FILE_NAME))
		{
			final Object obj = createStreamSerializer().deserialize(fin);
			System.out.println();
		}
	}


	static StreamSerializer createStreamSerializer()
	{
		return StreamSerializer.New(
			SerializerFoundation.New()
				.registerEntityTypes(ArrayList.class)
		);
	}
	
	
	
	
	static void writeTyped() throws Throwable
	{
		final Object data = createTestData();

		try(FileOutputStream out = new FileOutputStream(TYPED_FILE_NAME))
		{
			TypedStreamSerializer.New().serialize(data, out);
		}
		
		System.out.println("File Written");
	}
	
	
	static void readTyped() throws Throwable
	{
		try(FileInputStream fin = new FileInputStream(TYPED_FILE_NAME))
		{
			final Object obj = TypedStreamSerializer.New().deserialize(fin);
			System.out.println();
		}
	}


	static Object createTestData()
	{
		final List<String> list = new ArrayList<>();
		for(int i = 0; i < 25_000; i++)
		{
			list.add(UUID.randomUUID().toString());
		}
		return list;
	}
	
}
