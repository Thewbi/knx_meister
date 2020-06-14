package project.parsing.knx.steps;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import project.parsing.domain.KNXComObject;
import project.parsing.domain.KNXDatapointSubtype;
import project.parsing.domain.KNXDatapointType;
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

			final Collection<KNXComObject> values = knxDeviceInstance.getComObjects().values();
			final List<KNXComObject> valueList = new ArrayList<KNXComObject>(values);

			Collections.sort(valueList, new Comparator<KNXComObject>() {
				@Override
				public int compare(final KNXComObject lhs, final KNXComObject rhs) {
					return lhs.getNumber() - rhs.getNumber();
				}
			});

			for (final KNXComObject knxComObject : valueList) {

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

					final KNXDatapointSubtype knxDatapointSubtype = knxProject.getDatapointSubtypeMap()
							.get(dataPointTypeId);

					final Map<String, String> languageMap = knxProject.getLanguageStoreMap().get("de-DE");
					final KNXDatapointType knxDatapointType = knxDatapointSubtype.getKnxDatapointType();
					final String datapointTranslated = languageMap.get(knxDatapointType.getId());
					final String datapointSubtypeTranslated = languageMap.get(knxDatapointSubtype.getId());

					data += " " + knxDatapointType.getName() + " " + knxDatapointSubtype.getNumber() + " "
							+ datapointTranslated + ", " + datapointSubtypeTranslated;
				}

				LOG.info(data);
			}
		}
	}

}
