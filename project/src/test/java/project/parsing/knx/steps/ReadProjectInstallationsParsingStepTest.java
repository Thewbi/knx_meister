package project.parsing.knx.steps;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import api.exception.ProjectParsingException;
import project.parsing.ProjectParser;
import project.parsing.knx.KNXProjectParser;
import project.parsing.knx.KNXProjectParsingContext;

public class ReadProjectInstallationsParsingStepTest {

    @Test
    public void testProcess_777() throws IOException, ProjectParsingException {

        final File projectFile = new File("src/test/resources/KNX IP BAOS 777.knxproj");

        final ExtractArchiveParsingStep extractArchiveParsingStep = new ExtractArchiveParsingStep();
        final ReadProjectParsingStep readProjectParsingStep = new ReadProjectParsingStep();
        final ReadProjectInstallationsParsingStep readProjectInstallationsParsingStep = new ReadProjectInstallationsParsingStep();
        final DeleteTempFolderParsingStep deleteTempFolderParsingStep = new DeleteTempFolderParsingStep();

        final ProjectParser<KNXProjectParsingContext> parser = new KNXProjectParser();
        parser.getParsingSteps().add(extractArchiveParsingStep);
        parser.getParsingSteps().add(readProjectParsingStep);
        parser.getParsingSteps().add(readProjectInstallationsParsingStep);
        parser.getParsingSteps().add(deleteTempFolderParsingStep);

        parser.parse(projectFile);
    }

    @Test
    public void testProcess_FirstSteps() throws IOException, ProjectParsingException {

        final File projectFile = new File("src/test/resources/KNXfirstSteps200212_5devices.knxproj");

        final ExtractArchiveParsingStep extractArchiveParsingStep = new ExtractArchiveParsingStep();
        final ReadProjectParsingStep readProjectParsingStep = new ReadProjectParsingStep();
        final ReadProjectInstallationsParsingStep readProjectInstallationsParsingStep = new ReadProjectInstallationsParsingStep();
        final DeleteTempFolderParsingStep deleteTempFolderParsingStep = new DeleteTempFolderParsingStep();

        final ProjectParser<KNXProjectParsingContext> parser = new KNXProjectParser();
        parser.getParsingSteps().add(extractArchiveParsingStep);
        parser.getParsingSteps().add(readProjectParsingStep);
        parser.getParsingSteps().add(readProjectInstallationsParsingStep);
        parser.getParsingSteps().add(deleteTempFolderParsingStep);

        parser.parse(projectFile);
    }

}
