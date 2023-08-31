package one.microstream.storage.restadapter.types;

/*-
 * #%L
 * microstream-storage-restadapter
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *	Create a ViewerObjectDescription from the complex ObjectDescription type.
 *
 */
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
		this.gatherMemberValues();
		this.setReferences();

		return this.objDesc;
	}

	/**
	 *
	 * Collect all MemberValues and add them to the new ViewerObjectDescription.
	 *
	 */
	private void gatherMemberValues()
	{
		if(this.description.hasPrimitiveObjectInstance())
		{
			setPrimitiveValue(this.description, this.objDesc, this.valueLength);
		}
		else
		{
			final Object[] members = this.description.getValues();
			final List<Object> data = new ArrayList<>();
			this.appendFixedSizeValues(members, data);
			this.appendVariableSizeValues(members, data);
			this.objDesc.setData(data.toArray());
		}
	}

	/**
	 * Collect and add all "fixed sized" elements.
	 *
	 * @param members input ObjectDescription values
	 * @param data append collected elements to this list
	 */
	private void appendFixedSizeValues(final Object[] members, final List<Object> data)
	{
		final int upperLimit = getClampedArrayIndex(this.description.getLength(), this.fixedOffset, this.fixedLength);
		for(int i = this.fixedOffset; i < upperLimit; i++)
		{
			final Object member = members[i];
			if(member instanceof ObjectReferenceWrapper)
			{
				data.add(Long.toString(((ObjectReferenceWrapper) member).getObjectId()));
			}
			else
			{
				data.add(limitsPrimitiveType(member.toString(), this.valueLength));
			}
		}
	}

	/**
	 *	Collect and append "variable size" elements.
	 *
	 * @param members input ObjectDescription values
	 * @param data append collected elements to this list
	 */
	private void appendVariableSizeValues(final Object[] members, final List<Object> data)
	{
		if(this.description.getVariableLength() != null)
		{
			for(int i = 0; i < this.description.getVariableLength().length; i++)
			{
				final Object member = members[(int) (i + this.description.getLength())];
				if(member.getClass().isArray())
				{
					data.add(this.variableLengthValues((Object[]) member));
				}
			}
		}
	}

	/**
	 * Collect "variable sized" elements.
	 *
	 * @param values ObjectDescription values
	 */
	private Object[] variableLengthValues(final Object[] values)
	{
		final int upperLimit = getClampedArrayIndex(values.length, this.variableOffset, this.variableLength);
		return this.traverseValues(values, this.variableOffset, upperLimit);
	}

	/**
	 * Traverse input ObjectDescription values and collect them.
	 * Only elements within [startIndex and endIndex[ are taken into account.
	 *
	 * @param values input ObjectDescription values
	 */
	private Object[] traverseValues(final Object[] values, final int startIndex, final int endIndex)
	{
		final List<Object> data = new ArrayList<>();

		for(int i = startIndex; i < endIndex; i++)
		{
			final Object value = values[i];
			if(value instanceof ObjectReferenceWrapper)
			{
				data.add(Long.toString(((ObjectReferenceWrapper) value).getObjectId()));
			}
			else if(value.getClass().isArray())
			{
				final Object[] array = (Object[])value;
				data.add(this.traverseValues(array, 0, array.length));
			}
			else
			{
				data.add(limitsPrimitiveType(value.toString(), this.valueLength));
			}
		}

		return data.toArray();
	}

	/**
	 * Set the header values for the new ViewerObjectDescription.
	 */
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
					.toArray(String[]::new)
			);
		}
	}

	/**
	 * Collect and create Reference entries for the ViewerObjectDescription object to be created.
	 *
	 */
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
					final ViewerObjectDescriptionCreator descriptionCreator = new ViewerObjectDescriptionCreator(
						desc,
						this.fixedOffset,
						this.fixedLength,
						this.variableOffset,
						this.variableLength,
						this.valueLength
					);
					referencesList.add(descriptionCreator.create());
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

	/**
	 * Set the primitive value of the new ViewerObjectDescription object.
	 */
	private static void setPrimitiveValue(
		final ObjectDescription description,
		final ViewerObjectDescription objDesc,
		final long valueLength)
	{
		final String stringValue = description.getPrimitiveInstance().toString();
		final String subString = limitsPrimitiveType(stringValue, valueLength);
		objDesc.setData(new String[] { subString } );
		objDesc.setLength("1");
		objDesc.setVariableLength(new String[] {"0"});
		objDesc.setSimplified(true);
	}

	/**
	 * Calculate a valid end index for an array of "arrayLength" length
	 * considering a start index and element count.
	 *
	 * @param arrayLength length of the array
	 * @param startIndex index to start
	 * @param count number of desired elements
	 */
	private static int getClampedArrayIndex(final long arrayLength, final long startIndex, final long count)
	{
		final long realLength = Math.max(Math.min(arrayLength - startIndex, count), 0);
		return (int)Math.min(startIndex + realLength, Integer.MAX_VALUE);
	}

	/**
	 * Return a substring starting at index 0 with a max length of "valueLength".
	 * if valueLength exceeds the strings length it will be limited to the Strings length,
	 * no exceptions are thrown.
	 */
	private static String limitsPrimitiveType(final String data, final long valueLength)
	{
		final int endIndex = getClampedArrayIndex(data.length(), 0, valueLength);
		return data.substring(0, endIndex);
	}
}
