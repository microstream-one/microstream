package one.microstream.storage.restclient;

public class MainTestStorageRestClient
{
	public static void main(
		final String[] args
	)
	{
		final StorageRestConnection connection = StorageRestConnection.New(
			new StorageRestClientJersey("http://localhost:4567/microstream")
		);
	
		final StorageObject root = connection.root();
		System.out.println(root);
	}
	
}
