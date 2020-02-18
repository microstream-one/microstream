package one.microstream.storage.restclient;

public class MainTestStorageRestClient
{
	public static void main(
		final String[] args
	)
	{
		final StorageView view = StorageView.New(
			StorageViewConfiguration.Default(),
			StorageRestClientJersey.New("http://localhost:4567/microstream")
		);
	
		
	}
	
}
