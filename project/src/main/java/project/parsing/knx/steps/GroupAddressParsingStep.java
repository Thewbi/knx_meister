package project.parsing.knx.steps;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import project.parsing.domain.KNXComObject;
import project.parsing.domain.KNXDeviceInstance;
import project.parsing.domain.KNXGroupAddress;
import project.parsing.domain.KNXProject;
import project.parsing.knx.KNXProjectParsingContext;
import project.parsing.steps.ParsingStep;

public class GroupAddressParsingStep implements ParsingStep<KNXProjectParsingContext> {

	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger(GroupAddressParsingStep.class);

	@Override
	public void process(final KNXProjectParsingContext context) throws IOException {

		final KNXGroupAddress knxGroupAddress = context.getKnxGroupAddress();

		final Map<String, KNXGroupAddress> linkMap = new HashMap<>();

		fillLinkMap(knxGroupAddress, linkMap);

		final KNXProject knxProject = context.getKnxProject();
		for (final KNXDeviceInstance knxDeviceInstance : knxProject.getDeviceInstances()) {

			for (final KNXComObject knxComObject : knxDeviceInstance.getComObjects()) {

				if (StringUtils.isBlank(knxComObject.getGroupAddressLink())) {
					continue;
				}
				final KNXGroupAddress linkedKnxGroupAddress = linkMap.get(knxComObject.getGroupAddressLink());
				knxComObject.setKnxGroupAddress(linkedKnxGroupAddress);
			}
		}
	}

	private void fillLinkMap(final KNXGroupAddress knxGroupAddress, final Map<String, KNXGroupAddress> linkMap) {

		final String id = knxGroupAddress.getId();
		if (StringUtils.isNotBlank(id)) {

			final String[] split = id.split("_");
			final String groupAddressLinkTarget = split[1];
			linkMap.put(groupAddressLinkTarget, knxGroupAddress);
		}

		for (final KNXGroupAddress childKnxGroupAddress : knxGroupAddress.getKNXGroupAddresses()) {

			fillLinkMap(childKnxGroupAddress, linkMap);
		}
	}

}
