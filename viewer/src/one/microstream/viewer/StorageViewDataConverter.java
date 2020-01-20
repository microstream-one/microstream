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
		this.gson = new GsonBuilder().setLongSerializationPolicy(LongSerializationPolicy.STRING ).serializeNulls().create();
	}

	public String convert(final ViewerRootDescription root)
	{
		return this.gson.toJson(root);
	}

	public String convert(final ViewerObjectDescription description)
	{
		return this.gson.toJson(this.toSimple(description));
	}

	private String toSimple(final ViewerObjectDescription description)
	{
		return this.gson.toJson(this.simplify(description, 0, Long.MAX_VALUE));
	}

	public String convert(final ViewerObjectDescription description, final long dataOffset, final long dataLength)
	{
		return this.gson.toJson(this.simplify(description, dataOffset, dataLength));
	}

	private void setObjectHeader(final ViewerObjectDescription description, final SimpleObjectDescription objDesc)
	{
		objDesc.setObjectId(Long.toString(description.getObjectId()));
		objDesc.setTypeId(Long.toString(description.getPersistenceTypeDefinition().typeId()));
		objDesc.setLength(Long.toString(description.getLength()));
	}

	private void setPrimitiveValue(
		final ViewerObjectDescription description,
		final SimpleObjectDescription objDesc,
		final long dataOffset,
		final long dataLength)
	{
		final String stringValue = description.getPrimitiveInstance().toString();

		final int startIndex = (int) Math.min(dataOffset, stringValue.length());
		try {
			Math.addExact(startIndex, dataLength);}
		catch(final ArithmeticException e){ return;}

		final int endIndex = (int) Math.min(startIndex + dataLength, stringValue.length());

		final String subString = stringValue.substring(startIndex, endIndex);

		objDesc.setData(new String[] { subString } );
	}

	private void setReferences(
		final ViewerObjectDescription description,
		final SimpleObjectDescription objDesc,
		final long dataOffset,
		final long dataLength)
	{
		final ViewerObjectDescription refs[] = description.getReferences();
		if(refs != null)
		{
			final List<SimpleObjectDescription> refList = new ArrayList<>(refs.length);

			for (final ViewerObjectDescription desc : refs)
			{
				if(desc != null)
				{
					refList.add(this.simplify(desc, dataOffset, dataLength));
				}
				else
				{
					refList.add(null);
				}
			}

			objDesc.setReferences(refList.toArray(new SimpleObjectDescription[0]));
		}
	}

	private SimpleObjectDescription simplify(final ViewerObjectDescription description, final long dataOffset, final long dataLength)
	{
		final SimpleObjectDescription objDesc = new SimpleObjectDescription();

		this.setObjectHeader(description, objDesc);

		if(description.hasPrimitiveObjectInstance())
		{
			this.setPrimitiveValue(description, objDesc, dataOffset, dataLength);
		}
		else
		{
			objDesc.setData( this.simplifyObjectArray(description.getValues(), dataOffset, dataLength));
		}

		this.setReferences(description, objDesc, dataOffset, dataLength);
		return objDesc;
	}


	private Object[] simplifyObjectArray(final Object[] obj, final long dataOffset, final long dataLength)
	{
		final int startIndex = (int) dataOffset;
		final int realLength = (int) Math.max(Math.min(obj.length - startIndex, dataLength), 0);
		final int endIndex = startIndex + realLength;

		final Object[] dataArray = new Object[realLength];
		int counter = 0;

		for(int i = startIndex; i < endIndex; i++)
		{
			if(obj[i] instanceof ViewerObjectReferenceWrapper)
			{
				dataArray[counter] = Long.toString(((ViewerObjectReferenceWrapper) obj[i]).getObjectId());
			}
			else if(obj[i].getClass().isArray())
			{
				dataArray[counter] = this.simplifyObjectArray((Object[]) obj[i], dataOffset, dataLength);
			}
			else
			{
				dataArray[counter] = obj[i].toString();
			}

			counter++;
		}

		return dataArray;
	}


//	private SimpleObjectDescription toSimple( final ViewerObjectDescription description )
//	{
//		final SimpleObjectDescription objDesc = new SimpleObjectDescription();
//
//		this.setObjectHeader(description, objDesc);
//
//		if(description.hasPrimitiveObjectInstance())
//		{
//			objDesc.setData(new String[] {description.getPrimitiveInstance().toString()});
//		}
//		else
//		{
//			final Object[] data = new Object[description.getValues().length];
//
//			for(int i = 0; i < description.getValues().length; i++)
//			{
//				final Object obj = description.getValues()[i];
//
//				if(obj instanceof ViewerObjectReferenceWrapper)
//				{
//					data[i] = Long.toString(((ViewerObjectReferenceWrapper) obj).getObjectId());
//				}
//				else if(obj.getClass().isArray())
//				{
//					data[i] = this.objArray((Object[]) obj);
//				}
//				else
//				{
//					data[i] = obj.toString();
//				}
//			}
//
//			objDesc.setData(data);
//		}
//
//		return objDesc;
//	}
//
//	private Object[] objArray(final Object[] obj)
//	{
//		final Object[] dataArray = new Object[obj.length];
//
//		for(int i = 0; i < obj.length; i++)
//		{
//			if(obj[i] instanceof ViewerObjectReferenceWrapper)
//			{
//				dataArray[i] = Long.toString(((ViewerObjectReferenceWrapper) obj[i]).getObjectId());
//			}
//			else if(obj[i].getClass().isArray())
//			{
//				dataArray[i] = this.objArray((Object[]) obj[i]);
//			}
//			else
//			{
//				dataArray[i] = obj[i].toString();
//			}
//		}
//
//		return dataArray;
//	}


}
