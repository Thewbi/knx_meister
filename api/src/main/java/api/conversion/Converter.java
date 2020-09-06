package api.conversion;

public interface Converter<S, T> {

    T convert(S data);

}
