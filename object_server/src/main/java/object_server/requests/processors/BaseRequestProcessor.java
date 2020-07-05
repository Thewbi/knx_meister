package object_server.requests.processors;

import api.project.KNXProject;

public abstract class BaseRequestProcessor implements RequestProcessor {

	private KNXProject knxProject;

	public KNXProject getKnxProject() {
		return knxProject;
	}

	public void setKnxProject(final KNXProject knxProject) {
		this.knxProject = knxProject;
	}

}
