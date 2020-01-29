package one.microstream.viewer;

import java.util.ArrayList;
import java.util.List;

import one.microstream.persistence.binary.types.ObjectDescription;
import one.microstream.persistence.binary.types.ObjectReferenceWrapper;

public class ObjectDescriptionConverter
{
	public static ViewerObjectDescription convert(final ObjectDescription description, final long dataOffset, final long dataLength)
	{
		final ViewerObjectDescription objDesc = new ViewerObjectDescription();

		ObjectDescriptionConverter.setObjectHeader(description, objDesc);

		if(description.hasPrimitiveObjectInstance())
		{
			setPrimitiveValue(description, objDesc, dataOffset, dataLength);
		}
		else
		{
			objDesc.setData(simplifyObjectArray(description.getValues(), dataOffset, dataLength));
		}

		ObjectDescriptionConverter.setReferences(description, objDesc, dataOffset, dataLength);
		return objDesc;
	}

	private static void setPrimitiveValue(
		final ObjectDescription description,
		final ViewerObjectDescription objDesc,
		final long dataOffset,
		final long dataLength)
	{
		final String stringValue = description.getPrimitiveInstance().toString();
		final String subString = limitsPrimitiveType(stringValue, dataOffset, dataLength);
		objDesc.setData(new String[] { subString } );
	}

	private static void setObjectHeader(final ObjectDescription description, final ViewerObjectDescription objDesc)
	{
		objDesc.setObjectId(Long.toString(description.getObjectId()));
		objDesc.setTypeId(Long.toString(description.getPersistenceTypeDefinition().typeId()));
		objDesc.setLength(Long.toString(description.getLength()));
	}

	private static String limitsPrimitiveType(final String data, final long dataOffset, final long dataLength)
	{
		final int startIndex = (int) Math.min(dataOffset, data.length());
		final int realLength = (int) Math.max(Math.min(data.length() - startIndex, dataLength), 0);
		final int endIndex = startIndex + realLength;

		return data.substring(startIndex, endIndex);
	}

	private static void setReferences(
		final ObjectDescription description,
		final ViewerObjectDescription objDesc,
		final long dataOffset,
		final long dataLength)
	{
		final ObjectDescription refs[] = description.getReferences();
		if(refs != null)
		{
			final List<ViewerObjectDescription> refList = new ArrayList<>(refs.length);

			for (final ObjectDescription desc : refs)
			{
				if(desc != null)
				{
					refList.add(convert(desc, dataOffset, dataLength));
				}
				else
				{
					refList.add(null);
				}
			}

			objDesc.setReferences(refList.toArray(new ViewerObjectDescription[0]));
		}
	}

	private static Object[] simplifyObjectArray(final Object[] obj, final long dataOffset, final long dataLength)
	{
		final int startIndex = (int) Math.min(dataOffset, obj.length);
		final int realLength = (int) Math.max(Math.min(obj.length - startIndex, dataLength), 0);
		final int endIndex = startIndex + realLength;

		final Object[] dataArray = new Object[realLength];
		int counter = 0;

		for(int i = startIndex; i < endIndex; i++)
		{
			if(obj[i] instanceof ObjectReferenceWrapper)
			{
				dataArray[counter] = Long.toString(((ObjectReferenceWrapper) obj[i]).getObjectId());
			}
			else if(obj[i].getClass().isArray())
			{
				dataArray[counter] = simplifyObjectArray((Object[]) obj[i], dataOffset, dataLength);
			}
			else
			{
				dataArray[counter] = obj[i].toString();
			}

			counter++;
		}

		return dataArray;
	}

}
