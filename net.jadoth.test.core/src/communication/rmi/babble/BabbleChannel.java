package communication.rmi.babble;
import java.rmi.Remote;
import java.rmi.RemoteException;


public interface BabbleChannel extends Remote
{
	public String babble(String message) throws RemoteException;

}
