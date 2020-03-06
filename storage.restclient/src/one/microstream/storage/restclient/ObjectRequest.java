
package one.microstream.storage.restclient;

public interface ObjectRequest
{
	public long objectId();
	
	public Long valueLength();
	
	public Long fixedOffset();
	
	public Long fixedLength();
	
	public Long variableOffset();
	
	public Long variableLength();
	
	public Boolean references();
	
	public Long referenceOffset();
	
	public Long referenceLength();
	
	
	public static ObjectRequest New(final long objectId)
	{
		return new ObjectRequest.Default(objectId, null, null, null, null, null, null, null, null);
	}
	
	
	public static Builder Builder(final long objectId)
	{
		return new Builder.Default(objectId);
	}
	
	
	public static interface Builder
	{
		public Builder valueLength(
			Long valueLength
		);
		
		public default Builder fixedRange(final long offset, final long length)
		{
			return this.fixedOffset(offset).fixedLength(length);
		}
		
		public Builder fixedOffset(
			Long fixedOffset
		);
		
		public Builder fixedLength(
			Long fixedLength
		);
		
		public default Builder variableRange(final long offset, final long length)
		{
			return this.variableOffset(offset).variableLength(length);
		}
		
		public Builder variableOffset(
			Long variableOffset
		);
		
		public Builder variableLength(
			Long variableLength
		);
		
		public default Builder withReferences()
		{
			return this.references(true);
		}
		
		public default Builder withoutReferences()
		{
			return this.references(false);
		}
		
		public Builder references(
			Boolean references
		);
		
		public default Builder referenceRange(final long offset, final long length)
		{
			return this.referenceOffset(offset).referenceLength(length);
		}
		
		public Builder referenceOffset(
			Long referenceOffset
		);
		
		public Builder referenceLength(
			Long referenceLength
		);
		
		public ObjectRequest build();
		
		
		public static class Default implements Builder
		{
			private final long objectId;
			private Long       valueLength;
			private Long       fixedOffset;
			private Long       fixedLength;
			private Long       variableOffset;
			private Long       variableLength;
			private Boolean    references;
			private Long       referenceOffset;
			private Long       referenceLength;
			
			Default(
				final long objectId
			)
			{
				super();
				this.objectId = objectId;
			}
			
			@Override
			public Builder valueLength(
				final Long valueLength
			)
			{
				this.valueLength = valueLength;
				return this;
			}
			
			@Override
			public Builder fixedOffset(
				final Long fixedOffset
			)
			{
				this.fixedOffset = fixedOffset;
				return this;
			}
			
			@Override
			public Builder fixedLength(
				final Long fixedLength
			)
			{
				this.fixedLength = fixedLength;
				return this;
			}
			
			@Override
			public Builder variableOffset(
				final Long variableOffset
			)
			{
				this.variableOffset = variableOffset;
				return this;
			}
			
			@Override
			public Builder variableLength(
				final Long variableLength
			)
			{
				this.variableLength = variableLength;
				return this;
			}
			
			@Override
			public Builder references(
				final Boolean references
			)
			{
				this.references = references;
				return this;
			}
			
			@Override
			public Builder referenceOffset(
				final Long referenceOffset
			)
			{
				this.referenceOffset = referenceOffset;
				return this;
			}
			
			@Override
			public Builder referenceLength(
				final Long referenceLength
			)
			{
				this.referenceLength = referenceLength;
				return this;
			}
			
			@Override
			public ObjectRequest build()
			{
				return new ObjectRequest.Default(
					this.objectId,
					this.valueLength,
					this.fixedOffset,
					this.fixedLength,
					this.variableOffset,
					this.variableLength,
					this.references,
					this.referenceOffset,
					this.referenceLength
				);
			}
			
		}
		
	}
	
	
	public static class Default implements ObjectRequest
	{
		private final long    objectId;
		private final Long    valueLength;
		private final Long    fixedOffset;
		private final Long    fixedLength;
		private final Long    variableOffset;
		private final Long    variableLength;
		private final Boolean references;
		private final Long    referenceOffset;
		private final Long    referenceLength;
		
		Default(
			final long objectId,
			final Long valueLength,
			final Long fixedOffset,
			final Long fixedLength,
			final Long variableOffset,
			final Long variableLength,
			final Boolean references,
			final Long referenceOffset,
			final Long referenceLength
		)
		{
			super();
			this.objectId        = objectId;
			this.valueLength     = valueLength;
			this.fixedOffset     = fixedOffset;
			this.fixedLength     = fixedLength;
			this.variableOffset  = variableOffset;
			this.variableLength  = variableLength;
			this.references      = references;
			this.referenceOffset = referenceOffset;
			this.referenceLength = referenceLength;
		}
		
		@Override
		public long objectId()
		{
			return this.objectId;
		}
		
		@Override
		public Long valueLength()
		{
			return this.valueLength;
		}
		
		@Override
		public Long fixedOffset()
		{
			return this.fixedOffset;
		}
		
		@Override
		public Long fixedLength()
		{
			return this.fixedLength;
		}
		
		@Override
		public Long variableOffset()
		{
			return this.variableOffset;
		}
		
		@Override
		public Long variableLength()
		{
			return this.variableLength;
		}
		
		@Override
		public Boolean references()
		{
			return this.references;
		}
		
		@Override
		public Long referenceOffset()
		{
			return this.referenceOffset;
		}
		
		@Override
		public Long referenceLength()
		{
			return this.referenceLength;
		}
		
	}
	
}
