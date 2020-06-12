package project.parsing.knx.steps;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import project.parsing.ProjectParser;
import project.parsing.knx.KNXProjectParser;
import project.parsing.knx.KNXProjectParsingContext;

public class ExtractArchiveParsingStepTest {

	private final File projectFile = new File("src/test/resources/KNX IP BAOS 777.knxproj");

	@Test
	public void testProcess() throws IOException {

		final ExtractArchiveParsingStep extractArchiveParsingStep = new ExtractArchiveParsingStep();
		final DeleteTempFolderParsingStep deleteTempFolderParsingStep = new DeleteTempFolderParsingStep();

		final ProjectParser<KNXProjectParsingContext> parser = new KNXProjectParser();
		parser.getParsingSteps().add(extractArchiveParsingStep);
		parser.getParsingSteps().add(deleteTempFolderParsingStep);
		parser.parse(projectFile);
	}

}
