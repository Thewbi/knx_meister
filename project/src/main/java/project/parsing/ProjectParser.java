package project.parsing;

import java.io.File;
import java.io.IOException;
import java.util.List;

import project.parsing.steps.ParsingStep;

public interface ProjectParser<T> {

	List<ParsingStep<T>> getParsingSteps();

	void parse(File file) throws IOException;

}
