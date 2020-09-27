package project.parsing.factory;

import api.factory.Factory;
import api.factory.exception.FactoryException;
import project.parsing.ProjectParser;
import project.parsing.knx.KNXProjectParser;
import project.parsing.knx.KNXProjectParsingContext;
import project.parsing.knx.steps.ApplicationProgramParsingStep;
import project.parsing.knx.steps.DatapointTypeParsingStep;
import project.parsing.knx.steps.DeleteTempFolderParsingStep;
import project.parsing.knx.steps.ExtractArchiveParsingStep;
import project.parsing.knx.steps.GroupAddressParsingStep;
import project.parsing.knx.steps.HardwareParsingStep;
import project.parsing.knx.steps.ManufacturerParsingStep;
import project.parsing.knx.steps.OutputParsingStep;
import project.parsing.knx.steps.ReadProjectInstallationsParsingStep;
import project.parsing.knx.steps.ReadProjectParsingStep;

public class ProjectParserFactory implements Factory<ProjectParser<KNXProjectParsingContext>> {

    @Override
    public ProjectParser<KNXProjectParsingContext> create(final Object... args) throws FactoryException {

        final ExtractArchiveParsingStep extractArchiveParsingStep = new ExtractArchiveParsingStep();
        final ReadProjectParsingStep readProjectParsingStep = new ReadProjectParsingStep();
        final ManufacturerParsingStep manufacturerParsingStep = new ManufacturerParsingStep();
        final ReadProjectInstallationsParsingStep readProjectInstallationsParsingStep = new ReadProjectInstallationsParsingStep();
        final HardwareParsingStep hardwareParsingStep = new HardwareParsingStep();
        final ApplicationProgramParsingStep applicationProgramParsingStep = new ApplicationProgramParsingStep();
        final GroupAddressParsingStep groupAddressParsingStep = new GroupAddressParsingStep();
        final DatapointTypeParsingStep datapointTypeParsingStep = new DatapointTypeParsingStep();
        final DeleteTempFolderParsingStep deleteTempFolderParsingStep = new DeleteTempFolderParsingStep();
        final OutputParsingStep outputParsingStep = new OutputParsingStep();

        final ProjectParser<KNXProjectParsingContext> knxProjectParser = new KNXProjectParser();
        knxProjectParser.getParsingSteps().add(extractArchiveParsingStep);
        knxProjectParser.getParsingSteps().add(readProjectParsingStep);
        knxProjectParser.getParsingSteps().add(manufacturerParsingStep);
        knxProjectParser.getParsingSteps().add(readProjectInstallationsParsingStep);
        knxProjectParser.getParsingSteps().add(hardwareParsingStep);
        knxProjectParser.getParsingSteps().add(applicationProgramParsingStep);
        knxProjectParser.getParsingSteps().add(groupAddressParsingStep);
        knxProjectParser.getParsingSteps().add(datapointTypeParsingStep);
        knxProjectParser.getParsingSteps().add(deleteTempFolderParsingStep);
        knxProjectParser.getParsingSteps().add(outputParsingStep);

        return knxProjectParser;
    }

}
