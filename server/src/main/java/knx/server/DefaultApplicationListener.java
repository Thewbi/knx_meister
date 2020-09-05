package knx.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import core.communication.MulticastListenerReaderThread;
import core.communication.ObjectServerReaderThread;

@Component
public class DefaultApplicationListener implements ApplicationListener<ContextRefreshedEvent> {

	private final static Logger LOG = LoggerFactory.getLogger(DefaultApplicationListener.class);

	@Autowired
	private MulticastListenerReaderThread multicastListenerThread;

	@Autowired
	private ObjectServerReaderThread objectServerReaderThread;

//	@Value("${bind.ip}")
//	private String bindIp;
//
//	@Value("${multicast.ip}")
//	private String multicastIp;

	@Override
	public void onApplicationEvent(final ContextRefreshedEvent contextRefreshedEvent) {

		LOG.info("DefaultApplicationListener.onApplicationEvent()");

		new Thread(objectServerReaderThread).start();
		new Thread(multicastListenerThread).start();
	}

}
