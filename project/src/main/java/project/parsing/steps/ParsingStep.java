package project.parsing.steps;

import java.io.IOException;

public interface ParsingStep<T> {

	void process(T context) throws IOException;

}
