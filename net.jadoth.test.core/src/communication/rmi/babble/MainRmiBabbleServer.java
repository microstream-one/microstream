package communication.rmi.babble;

public class MainRmiBabbleServer
{
	public static void main(final String[] args) throws Exception
	{
		BabbleTest.publish(new SimpleBabbleChannel(), BabbleTest.port, BabbleTest.channelName);
	}

}
