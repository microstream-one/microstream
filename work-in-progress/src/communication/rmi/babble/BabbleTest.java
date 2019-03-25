package communication.rmi.babble;

import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class BabbleTest
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	public static final int    port        = 1337;
	public static final String host        = "localhost";
	public static final String channelName = "babbleTestChannel";



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static BabbleChannel connect(final String host, final int port, final String rmiChannelName) throws Exception
	{
		return (BabbleChannel)LocateRegistry.getRegistry(host, port).lookup(rmiChannelName);
	}

	public static void publish(final Remote remoteObject, final int port, final String rmiChannelName) throws Exception
	{
		// 1: get or create registry
		Registry registry;
		// (10.05.2011 TM)NOTE: no idea why this doesn't work
//		if((registry = LocateRegistry.getRegistry(port)) == null)
//		{
//			registry = LocateRegistry.createRegistry(port);
//		}
		registry = LocateRegistry.createRegistry(port);

		// 2: export remote object
		Remote exportedObject;
		if(remoteObject instanceof UnicastRemoteObject)
		{
			// (14.02.2012 TM)NOTE: fix for: UnicastRemoteObject.exportObject() throws Exception for UnicastRemoteObject instances
			exportedObject = remoteObject;
		}
		else
		{
			try
			{
				exportedObject = UnicastRemoteObject.exportObject(remoteObject, port);
			}
			catch(final Exception e)
			{
				// surprisingly can't close / shut down a RMI registy object ^^, only unexport it
				try
				{
					UnicastRemoteObject.unexportObject(registry, true);
				}
				catch(final NoSuchObjectException e1)
				{
					// well then not, it's only for clean-up anyway
					e1.printStackTrace();
				}
				throw e;
			}
		}

		/* sounds like a hilariously serious bug in RMI to me, but without that pause, the server will
		 * terminate without any exception. Probably the registry needs time to do stuff to being able to receice
		 * bindings and if it has no bindings, the thread terminates or so. Funny. And Frustrating.
		 */
		Thread.sleep(100);

		// 3: bind exported object to its channel name
		try
		{
			registry.rebind(rmiChannelName, exportedObject);
		}
		catch(final Exception e)
		{
			// again, can't get rid of registry itself, only unexport already exported channel and registry itself
			try
			{
				UnicastRemoteObject.unexportObject(exportedObject, true);
				UnicastRemoteObject.unexportObject(registry, true);
			}
			catch(final NoSuchObjectException e1)
			{
				// well then not, it's only for clean-up anyway
				e1.printStackTrace();
			}
			throw e;
		}
	}

}
