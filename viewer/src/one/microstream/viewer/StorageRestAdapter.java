package one.microstream.viewer;

import java.util.List;

import one.microstream.persistence.binary.types.ViewerMemberProvider;
import one.microstream.persistence.binary.types.ViewerObjectDescription;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.viewer.dataobjects.MemberDescription;
import one.microstream.viewer.dataobjects.ObjectDescription;
import one.microstream.viewer.dataobjects.ObjectDescriptionConverter;

public class StorageRestAdapter<T> extends EmbeddedStorageRestAdapter
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final ObjectDescriptionConverter<T> coverter;
	private final StorageViewDataProcessor preprocessor;

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public StorageRestAdapter(final EmbeddedStorageManager storage,
		final ObjectDescriptionConverter<T> converter,
		final StorageViewDataProcessor preprocessor)
	{
		super(storage);
		this.coverter = converter;
		this.preprocessor = preprocessor;;
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	public T getObject(final long objectId)
	{
		final ViewerObjectDescription description = super.getStorageObject(objectId);
		final ObjectDescription preprocessed = this.preprocessor.process(description);
		return this.coverter.convert(preprocessed);
	}

	public T getObject(final long objectId, final int... childIndices)
	{
		ViewerObjectMemberDescription description = super.getStorageObject(objectId).getMember(childIndices[0]);

		for(int i = 1; i < childIndices.length; i++)
		{
			description = description.getMember(childIndices[i]);
		}

		if(description != null)
		{
			final MemberDescription preprocessed = this.preprocessor.process(description);
			return this.coverter.convert(preprocessed);
		}

		return null;
	}

	public T getObject(final long objectId, final int maxElements, final int[] childIndices)
	{
		ViewerMemberProvider object = super.getStorageObject(objectId);

		for(int i = 0; i < childIndices.length - 1; i++)
		{
			object = object.getMember(childIndices[i]);
		}

		final int childIndex = childIndices[childIndices.length -1];
		final List<ViewerObjectMemberDescription> members = object.getMembers(childIndex , maxElements);

		final List<MemberDescription> preprocessed = this.preprocessor.process(members);
		return this.coverter.convert(preprocessed);
	}

	public T getRoot()
	{
		final ViewerRootDescription root = super.getUserRoot();
		return this.coverter.convert(this.preprocessor.process(root));
	}
}
