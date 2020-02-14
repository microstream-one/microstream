package storage.restservice.sparkjava;

import spark.Request;
import spark.Response;

public class RouteDocumentation extends RouteBase<RoutesManager>
{
	public RouteDocumentation(final RoutesManager storageRestAdapter)
	{
		super(storageRestAdapter);
	}

	@Override
	public Object handle(final Request request, final Response response)
	{
		response.type("application/json");
		return this.storageRestAdapter.getDocumentation(request.uri());
	}

//	@Override
//	public Object handle(final Request request, final Response response) throws Exception
//	{
//		//TODO: Error handling
//		final Hashtable<String, Hashtable<String, String>> routes = this.storageRestAdapter.getRegisteredRoutes();
//		final Hashtable<String,String> methods = routes.get(request.uri());
//
//		final JsonArray methodsJson = new JsonArray(methods.size());
//
//		methods.forEach((method, handler) -> {
//
//			methodsJson.add(this.returnAPI(handler, method));
//
//		});
//
//		response.type("application/json");
//		return methodsJson;
//	}
//
//
//
//
//	private JsonObject returnAPI(final String handler, final String httpMethod)
//	{
//		try(final InputStream in = this.getClass().getResourceAsStream("/resources/onlineDocu.json");
//			final BufferedReader reader = new BufferedReader(new InputStreamReader(in));)
//		{
//			final StringBuilder builder = new StringBuilder(in.available()*2);
//			String read = null;
//			while((read = reader.readLine()) != null)
//			{
//				builder.append(read);
//			}
//
//			final JsonObject g = new Gson().fromJson(builder.toString(), JsonObject.class);
//			final JsonObject o = g.getAsJsonObject("handler").getAsJsonObject(handler);
//			final JsonObject m = o.getAsJsonObject(httpMethod);
//
//			return m;
//		}
//		//TODO: trow different exceptions for io related issues and not available for handler ...
//		catch(final Exception e )
//		{
//			throw new ViewerException(e.getMessage());
//		}
//	}
}
