package api.conversion;

import api.exception.ObjectServerException;

public interface Converter<S, T> {

	T convert(S data) throws ObjectServerException;

}
