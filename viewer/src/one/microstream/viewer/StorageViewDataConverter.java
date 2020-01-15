package one.microstream.viewer;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.LongSerializationPolicy;

import one.microstream.persistence.binary.types.ViewerObjectDescription;
import one.microstream.persistence.binary.types.ViewerObjectReferenceWrapper;
import one.microstream.viewer.dto.SimpleObjectDescription;

public class StorageViewDataConverter
{
	Gson gson;

	public StorageViewDataConverter()
	{
		super();
		this.gson = new GsonBuilder().setLongSerializationPolicy(LongSerializationPolicy.STRING ).create();
	}

	public String convert(final ViewerRootDescription root)
	{
		return this.gson.toJson(root);
	}

	public String convert(final ViewerObjectDescription description)
	{
		return this.gson.toJson(this.toSimple(description));
	}

	public String convert(final ViewerObjectDescription description, final long dataOffset, final long dataLength)
	{
		return this.gson.toJson(this.toSimple(description, dataOffset, dataLength));
	}

	private SimpleObjectDescription toSimple(final ViewerObjectDescription description, final long dataOffset, final long dataLength)
	{
		final SimpleObjectDescription objDesc = new SimpleObjectDescription();

		objDesc.setObjectId(Long.toString(description.getObjectId()));
		objDesc.setTypeId(Long.toString(description.getPersistenceTypeDefinition().typeId()));
		objDesc.setLength(Long.toString(description.getLength()));

		if(description.hasPrimitiveObjectInstance())
		{
			final String stringValue = description.getPrimitiveInstance().toString();

			//TODO: HAGR better bounds check and exceptions
			final int endIndex = (int) Math.min(dataOffset + dataLength, stringValue.length());
			final int beginIndex = (int) dataOffset;


			final String subString = stringValue.substring(beginIndex, endIndex);

			objDesc.setData(new String[] { subString } );
		}
		else
		{
			final int startIndex = (int) dataOffset;
			final int realLength = Math.max(Math.min(description.getValues().length - startIndex,  (int) dataLength), 0);
			final int endIndex = startIndex + realLength;

			final Object[] data = new Object[realLength];
			int counter = 0;

			for(int i = startIndex; i < endIndex; i++)
			{
				final Object obj = description.getValues()[i];

				if(obj instanceof ViewerObjectReferenceWrapper)
				{
					data[counter] = Long.toString(((ViewerObjectReferenceWrapper) obj).getObjectId());
				}
				else if(obj.getClass().isArray())
				{
					data[counter] = this.objArray((Object[]) obj);
				}
				else
				{
					data[counter] = obj.toString();
				}

				counter++;
			}

			objDesc.setData(data);
		}


		final ViewerObjectDescription refs[] = description.getReferences();
		if(refs != null)
		{
			final List<SimpleObjectDescription> refList = new ArrayList<>(refs.length);

			for (final ViewerObjectDescription desc : refs)
			{
				if(desc != null)
				{
					refList.add(this.toSimple(desc, dataOffset, dataLength));
				}
				else
				{
					refList.add(null);
				}
			}

			objDesc.setReferences(refList.toArray(new SimpleObjectDescription[0]));
		}
		return objDesc;
	}

	private SimpleObjectDescription toSimple( final ViewerObjectDescription description )
	{
		final SimpleObjectDescription objDesc = new SimpleObjectDescription();

		objDesc.setObjectId(Long.toString(description.getObjectId()));
		objDesc.setTypeId(Long.toString(description.getPersistenceTypeDefinition().typeId()));
		objDesc.setLength(Long.toString(description.getLength()));

		if(description.hasPrimitiveObjectInstance())
		{
			objDesc.setData(new String[] {description.getPrimitiveInstance().toString()});
		}
		else
		{
			final Object[] data = new Object[description.getValues().length];

			for(int i = 0; i < description.getValues().length; i++)
			{
				final Object obj = description.getValues()[i];

				if(obj instanceof ViewerObjectReferenceWrapper)
				{
					data[i] = Long.toString(((ViewerObjectReferenceWrapper) obj).getObjectId());
				}
				else if(obj.getClass().isArray())
				{
					data[i] = this.objArray((Object[]) obj);
				}
				else
				{
					data[i] = obj.toString();
				}
			}

			objDesc.setData(data);
		}

		return objDesc;
	}

	private Object[] objArray(final Object[] obj)
	{
		final Object[] dataArray = new Object[obj.length];

		for(int i = 0; i < obj.length; i++)
		{
			if(obj[i] instanceof ViewerObjectReferenceWrapper)
			{
				dataArray[i] = Long.toString(((ViewerObjectReferenceWrapper) obj[i]).getObjectId());
			}
			else if(obj[i].getClass().isArray())
			{
				dataArray[i] = this.objArray((Object[]) obj[i]);
			}
			else
			{
				dataArray[i] = obj[i].toString();
			}
		}

		return dataArray;
	}


}
