package project.parsing.knx.steps;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import api.exception.ProjectParsingException;
import project.parsing.ProjectParser;
import project.parsing.knx.KNXProjectParser;
import project.parsing.knx.KNXProjectParsingContext;

public class ReadProjectParsingStepTest {

	private final File projectFile = new File("src/test/resources/KNX IP BAOS 777.knxproj");

	@Test
	public void testProcess() throws IOException, ProjectParsingException {

		final ExtractArchiveParsingStep extractArchiveParsingStep = new ExtractArchiveParsingStep();
		final ReadProjectParsingStep readProjectParsingStep = new ReadProjectParsingStep();
		final DeleteTempFolderParsingStep deleteTempFolderParsingStep = new DeleteTempFolderParsingStep();

		final ProjectParser<KNXProjectParsingContext> parser = new KNXProjectParser();
		parser.getParsingSteps().add(extractArchiveParsingStep);
		parser.getParsingSteps().add(readProjectParsingStep);
		parser.getParsingSteps().add(deleteTempFolderParsingStep);

		parser.parse(projectFile);
	}

}
