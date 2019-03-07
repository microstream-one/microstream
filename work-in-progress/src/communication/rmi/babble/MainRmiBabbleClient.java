package communication.rmi.babble;


public class MainRmiBabbleClient
{
	public static void main(final String[] args) throws Exception
	{
		final BabbleChannel channel = BabbleTest.connect(BabbleTest.host, BabbleTest.port, BabbleTest.channelName);

		while(true)
		{
			System.out.println("Server answered: "+channel.babble("blabla"));
		}
	}

}
