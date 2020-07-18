package project.parsing.steps;

import java.io.IOException;

import api.exception.ProjectParsingException;

public interface ParsingStep<T> {

	void process(T context) throws IOException, ProjectParsingException;

}
