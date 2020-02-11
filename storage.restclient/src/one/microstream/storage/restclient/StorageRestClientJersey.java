package one.microstream.storage.restclient;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import one.microstream.persistence.binary.types.BinaryFieldLengthResolver;
import one.microstream.persistence.types.PersistenceTypeDescription;
import one.microstream.persistence.types.PersistenceTypeDictionaryParser;
import one.microstream.persistence.types.PersistenceTypeNameMapper;
import one.microstream.storage.restadapter.ViewerObjectDescription;
import one.microstream.storage.restadapter.ViewerRootDescription;
import one.microstream.storage.restadapter.ViewerStorageFileStatistics;

public class StorageRestClientJersey implements StorageRestClient
{
	private final String baseUrl;
	private Client       client;
	private WebTarget    storageRestService;
	
	public StorageRestClientJersey(final String baseUrl)
	{
		this.baseUrl = baseUrl;
	}
	
	private WebTarget storageRestService()
	{
		if(this.storageRestService == null)
		{
			this.client = ClientBuilder.newBuilder()
				.register(GsonReader.class)
				.build();
			
			this.storageRestService = this.client.target(this.baseUrl);
		}
		
		return this.storageRestService;
	}

	@Override
	public Map<Long, PersistenceTypeDescription> requestTypeDictionary()
	{
		final String data = this.storageRestService()
			.path("dictionary")
			.request(MediaType.APPLICATION_JSON)
			.get(String.class);
		
		final PersistenceTypeDictionaryParser parser = PersistenceTypeDictionaryParser.New(
			new BinaryFieldLengthResolver.Default(),
			PersistenceTypeNameMapper.New()
		);
		
		final Map<Long, PersistenceTypeDescription> dictionary = new HashMap<>();
		parser.parseTypeDictionaryEntries(data).forEach(entry -> dictionary.put(entry.typeId(), entry));
		return dictionary;
	}

	@Override
	public ViewerRootDescription requestRoot()
	{
		return this.storageRestService()
			.path("root")
			.request(MediaType.APPLICATION_JSON)
			.get(ViewerRootDescription.class);
	}
	
	@Override
	public ViewerObjectDescription requestObject(
		final long oid
	)
	{
		return this.requestObject(oid, false, -1, -1);
	}
	
	@Override
	public ViewerObjectDescription requestObjectWithReferences(
		final long oid
	)
	{
		return this.requestObject(oid, true, -1, -1);
	}
	
	@Override
	public ViewerObjectDescription requestObjectWithReferences(
		final long oid,
		final long referenceOffset,
		final long referenceLength
	)
	{
		return this.requestObject(oid, true, referenceOffset, referenceLength);
	}

	private ViewerObjectDescription requestObject(
		final long oid,
		final boolean references,
		final long referenceOffset,
		final long referenceLength
	)
	{
		WebTarget target = this.storageRestService()
			.path("object")
			.path(Long.toString(oid));
		
		if(references)
		{
			target = target.queryParam("references", "true");
			if(referenceOffset >= 0)
			{
				target = target.queryParam("referenceOffset", referenceOffset);
			}
			if(referenceLength >= 0)
			{
				target = target.queryParam("referenceLength", referenceLength);
			}
		}
						
		return target
			.request(MediaType.APPLICATION_JSON)
			.get(ViewerObjectDescription.class);
	}

	@Override
	public ViewerStorageFileStatistics requestFileStatistics()
	{
		return this.storageRestService()
			.path("maintenance/filesStatistics")
			.request(MediaType.APPLICATION_JSON)
			.get(ViewerStorageFileStatistics.class);
	}


	@Override
	public void close()
	{
		if(this.client != null)
		{
			this.client.close();
			this.client = null;
		}
		
		this.storageRestService = null;
	}
	
}
