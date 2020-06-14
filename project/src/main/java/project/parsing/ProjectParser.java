package project.parsing;

import java.io.File;
import java.io.IOException;
import java.util.List;

import project.parsing.domain.KNXProject;
import project.parsing.steps.ParsingStep;

public interface ProjectParser<T> {

	List<ParsingStep<T>> getParsingSteps();

	KNXProject parse(File file) throws IOException;

}
