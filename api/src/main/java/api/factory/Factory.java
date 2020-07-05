package api.factory;

import api.factory.exception.FactoryException;

public interface Factory<T> {

	T create(Object... args) throws FactoryException;

}
