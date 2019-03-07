package communication.rmi.babble;
import java.rmi.RemoteException;


public class SimpleBabbleChannel implements BabbleChannel
{
	@Override
	public String babble(final String message) throws RemoteException
	{
		return "You said: "+message;
	}

}
