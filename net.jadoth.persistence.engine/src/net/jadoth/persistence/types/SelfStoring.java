package net.jadoth.persistence.types;


public interface SelfStoring
{
	public <S extends Storer> S storeBy(S storer);
}
