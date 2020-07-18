package project.parsing;

import java.io.File;
import java.io.IOException;
import java.util.List;

import api.exception.ProjectParsingException;
import api.project.KNXProject;
import project.parsing.steps.ParsingStep;

public interface ProjectParser<T> {

	List<ParsingStep<T>> getParsingSteps();

	KNXProject parse(File file) throws IOException, ProjectParsingException;

}
