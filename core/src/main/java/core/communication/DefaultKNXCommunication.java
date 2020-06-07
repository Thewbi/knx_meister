package core.communication;

import java.util.concurrent.locks.ReentrantLock;

import core.api.KNXCommunication;

public class DefaultKNXCommunication implements KNXCommunication {

	public static final long SEARCH_TIMEOUT = 5000;

	private final ReentrantLock searchRequestLock = new ReentrantLock();

	@Override
	public void searchRequest() {

		// only one search request at a time
		searchRequestLock.lock();
		try {
			try {
				Thread.sleep(1);
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		} finally {
			searchRequestLock.unlock();
		}
	}

}
