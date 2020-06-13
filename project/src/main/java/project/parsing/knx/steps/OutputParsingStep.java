package project.parsing.knx.steps;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import project.parsing.domain.KNXComObject;
import project.parsing.domain.KNXDatapointSubtype;
import project.parsing.domain.KNXDeviceInstance;
import project.parsing.domain.KNXGroupAddress;
import project.parsing.domain.KNXProject;
import project.parsing.knx.KNXProjectParsingContext;
import project.parsing.steps.ParsingStep;

public class OutputParsingStep implements ParsingStep<KNXProjectParsingContext> {

	private static final Logger LOG = LogManager.getLogger(OutputParsingStep.class);

	@Override
	public void process(final KNXProjectParsingContext context) throws IOException {

		final KNXProject knxProject = context.getKnxProject();

		if (knxProject == null) {
			return;
		}

		for (final KNXDeviceInstance knxDeviceInstance : knxProject.getDeviceInstances()) {

			LOG.info("KNXDeviceInstance: " + knxDeviceInstance.getId() + " " + knxDeviceInstance.getAddress());

			Collections.sort(knxDeviceInstance.getComObjects(), new Comparator<KNXComObject>() {

				@Override
				public int compare(final KNXComObject lhs, final KNXComObject rhs) {
					return lhs.getNumber() - rhs.getNumber();
				}
			});

			for (final KNXComObject knxComObject : knxDeviceInstance.getComObjects()) {

				if (!knxComObject.isGroupObject()) {
					continue;
				}

				// number and text
				String data = knxComObject.getNumber() + " " + knxComObject.getText();

				// group address
				if (knxComObject.getKnxGroupAddress() != null) {
					data += " " + knxComObject.getKnxGroupAddress().getGroupAddress();
				}

				// data point type
				final KNXGroupAddress knxGroupAddress = knxComObject.getKnxGroupAddress();
				if (knxGroupAddress != null) {

					final String dataPointTypeId = knxGroupAddress.getDataPointType();

					final KNXDatapointSubtype knxDatapointSubtype = context.getDatapointSubtypeMap()
							.get(dataPointTypeId);

					data += " "
							+ context.getLanguageStoreMap().get("de-DE")
									.get(knxDatapointSubtype.getKnxDatapointType().getId())
							+ ", " + context.getLanguageStoreMap().get("de-DE").get(knxDatapointSubtype.getId());
				}

				LOG.info(data);
			}
		}
	}

}
