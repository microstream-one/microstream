package one.microstream.storage.restadapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ViewerObjectDescriptionCreator
{
	private final ObjectDescription description;
	private final int fixedOffset;
	private final int fixedLength;
	private final int variableOffset;
	private final int variableLength;
	private final int valueLength;
	private ViewerObjectDescription objDesc;

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ViewerObjectDescriptionCreator(
		final ObjectDescription description,
		final long fixedOffset,
		final long fixedLength,
		final long variableOffset,
		final long variableLength,
		final long valueLength)
	{
		super();
		this.description = description;
		this.fixedOffset = (int)Math.min(Integer.MAX_VALUE, fixedOffset);
		this.fixedLength = (int)Math.min(Integer.MAX_VALUE, fixedLength);
		this.variableOffset = (int)Math.min(Integer.MAX_VALUE, variableOffset);
		this.variableLength = (int)Math.min(Integer.MAX_VALUE, variableLength);
		this.valueLength = (int)Math.min(Integer.MAX_VALUE, valueLength);
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	/**
	 * Create a ViewerObjectDescription Object
	 *
	 * @return ViewerObjectDescription
	 */
	public ViewerObjectDescription create()
	{
		this.objDesc = new ViewerObjectDescription();
		this.setObjectHeader();
		this.setMemberValues();
		this.setReferences();

		return this.objDesc;
	}

	private void setMemberValues()
	{
		if(this.description.hasPrimitiveObjectInstance())
		{
			setPrimitiveValue(this.description, this.objDesc, this.valueLength);
		}
		else
		{
			final Object[] members = this.description.getValues();
			final List<Object> data = new ArrayList<>();

			final int upperLimit = getClampedArrayIndex(this.description.getLength(), this.fixedOffset, this.fixedLength);

			for(int i = this.fixedOffset; i < upperLimit; i++)
			{
				if(members[i] instanceof ObjectReferenceWrapper)
				{
					data.add(Long.toString(((ObjectReferenceWrapper) members[i]).getObjectId()));
				}
				else
				{
					data.add(limitsPrimitiveType(members[i].toString(), this.valueLength));

				}
			}


			if(this.description.getVariableLength() != null)
			{
				for(int i = 0; i < this.description.getVariableLength().length; i++)
				{
					final Object obj = this.description.getValues()[(int) (i + this.description.getLength())];
					if(obj.getClass().isArray())
					data.add(this.variableLengthValues((Object[]) obj));
				}
			}
			this.objDesc.setData(data.toArray());
		}
	}

	private Object[] variableLengthValues(final Object[] obj)
	{
		final int upperLimit = getClampedArrayIndex(obj.length, this.variableOffset, this.variableLength);
		return this.traverseValues(obj, this.variableOffset, upperLimit);
	}


	private Object[] traverseValues(final Object[] values, final int startIndex, final int endIndex)
	{
		final List<Object> data = new ArrayList<>();

		for(int i = startIndex; i < endIndex; i++)
		{
			if(values[i] instanceof ObjectReferenceWrapper)
			{
				data.add(Long.toString(((ObjectReferenceWrapper) values[i]).getObjectId()));
			}
			else if(values[i].getClass().isArray())
			{
				data.add(this.traverseValues((Object[])values[i], 0, ((Object[])values[i]).length));
			}
			else
			{
				data.add(limitsPrimitiveType(values[i].toString(), this.valueLength));
			}
		}

		return data.toArray();
	}

	private void setObjectHeader()
	{
		this.objDesc.setObjectId(Long.toString(this.description.getObjectId()));
		this.objDesc.setTypeId(Long.toString(this.description.getPersistenceTypeDefinition().typeId()));
		this.objDesc.setLength(Long.toString(this.description.getLength()));

		if(this.description.getVariableLength() != null)
		{
			this.objDesc.setVariableLength(
				Arrays.stream(this.description.getVariableLength())
					.map( l -> l.toString())
					.collect(Collectors.toList())
					.toArray(new String[0]));
		}
	}

	private void setReferences()
	{
		final ObjectDescription references[] = this.description.getReferences();
		if(references != null)
		{
			final List<ViewerObjectDescription> referencesList = new ArrayList<>(references.length);

			for (final ObjectDescription desc : references)
			{
				if(desc != null)
				{
					final ViewerObjectDescriptionCreator c = new ViewerObjectDescriptionCreator(
							desc,
							this.fixedOffset,
							this.fixedLength,
							this.variableOffset,
							this.variableLength,
							this.valueLength);

					final ViewerObjectDescription resolvedReferences = c.create();
					referencesList.add(resolvedReferences);
				}
				else
				{
					referencesList.add(null);
				}
			}

			this.objDesc.setReferences(referencesList.toArray(new ViewerObjectDescription[0]));
		}
	}

	///////////////////////////////////////////////////////////////////////////
	// private static methods //
	///////////////////////////

	private static void setPrimitiveValue(
		final ObjectDescription description,
		final ViewerObjectDescription objDesc,
		final long valueLength)
	{
		final String stringValue = description.getPrimitiveInstance().toString();
		final String subString = limitsPrimitiveType(stringValue, valueLength);
		objDesc.setData(new String[] { subString } );
	}

	private static int getClampedArrayIndex(final long arrayLength, final long startIndex, final long count)
	{
		final long realLength = Math.max(Math.min(arrayLength - startIndex, count), 0);
		final long endIndex = Math.min(startIndex + realLength, Integer.MAX_VALUE);
		return (int) endIndex;
	}

	private static String limitsPrimitiveType(final String data, final long valueLength)
	{
		final int endIndex = getClampedArrayIndex(data.length(), 0, valueLength);
		return data.substring(0, endIndex);
	}
}
