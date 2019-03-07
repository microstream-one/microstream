package one.microstream.network.types;



public interface NetworkUserSessionProtocol<U, M, S extends NetworkUserSession<U, M>>
extends NetworkConnectionValidator.Provider<U>, NetworkSessionClientGreeter.Provider<S>
{
	public NetworkUserSession.Creator<U, ?, S> provideUserSessionCreator();

}
