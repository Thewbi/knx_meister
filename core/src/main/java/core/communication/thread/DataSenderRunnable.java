package core.communication.thread;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import api.device.Device;
import api.device.DeviceService;
import api.project.KNXComObject;
import common.utils.Utils;
import core.communication.Connection;
import core.data.sending.DataSender;

public class DataSenderRunnable implements Runnable {

    private static final int MAX_VALUE = 40;

    private static final int MIN_VALUE = 0;

    private static final int SLEEP_AMOUNT_IN_MILLIS = 5000;

    private static final Logger LOG = LogManager.getLogger(DataSenderRunnable.class);

    private String label;

    private DataSender dataSender;

    private Connection connection;

    private boolean done = false;

//    private int deviceIndex;

//    private final Random random = new Random();

//    private int currentValue = MIN_VALUE;
//
//    private int increment = 1;

    private DeviceService deviceService;

    @Override
    public void run() {

        LOG.info(label + " run() ...");
        done = false;

        try {

            // in order to be compatible with the ETS5 Bus-Monitor, the tunnel requests can
            // only be send
            // to the ETS5 Bus-Monitor after the Bus-Monitor did ask for the ConnectionState
            // and that
            // request was answered with the answer OK.
            //
            // The sequence is:
            // 1. The Bus-Monitor establishes a tunneling connection with the device.
            // 2. The device returns the ID of the tunneling connection.
            // 3. The Bus-Monitor requests the ConnectionState of the tunneling connection
            // using the ID from step 2.
            // 4. The device answers with OK (the tunneling connection is in an OK state).
            // 5. The device now can use the tunneling connection to send data to the
            // Bus-Monitor in the form
            // of tunneling requests
            //
            // If the thread does not sleep but sends a tunneling request immediately, the
            // Bus-Monitor receives the tunneling request before it has performed the
            // Connection State check. If any requests arrives before the connection state
            // check, the Bus-Monitor will disconnect the tunneling connection immediately.
            //
            // An alternative would be to start this thread only after the communication
            // partner
            // has send a connection state request but some partners do never send a
            // communication state request
            LOG.info(label + " Sleeping 5000 ...");
            Thread.sleep(5000);
            LOG.info(label + " Sleeping 5000 done.");
        } catch (final InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }

        LOG.info("{} done={}", label, done);

        while (!done) {

            LOG.info(label + " Sending data ...");

//			final String physicalAddress = "0/0/9";
//			final int dataPointId = 17;

//			final String physicalAddress = "0/0/3";
//			final int dataPointId = 626;

//			final String physicalAddress = "0/3/7";
//			// 0x86 = 134d - Window, Soll-Istpos Prozentwert,
//			// 1_13_DATASCALINGCONTROLSTATE_1_State, 8-Bit vorzeichenlos, DPST-5-1
//			final int dataPointId = 134;

//			// Wetter Temperatur
//			final String physicalAddress = "0/2/0";
//			final int dataPointId = 547;

            // this works:
//            final String devicePhysicalAddress = "1.1.255";
//            final String groupAddress = "0/1/1";
//            final int dataPointId = 325;
//			final int dataPointId = 145;

//			final String physicalAddress = "0/2/1";
//			final int dataPointId = 550;

//			final String physicalAddress = "0/2/2";
//			final int dataPointId = 553;

//			final int rangeMin = MIN_VALUE;
//			final int rangeMax = MAX_VALUE;
//			final double randomValue = rangeMin + (rangeMax - rangeMin) * random.nextDouble();
//			final double value = randomValue;

            for (final Device device : deviceService.getDevices().values()) {

                for (final KNXComObject knxComObject : device.getComObjects().values()) {

                    if (knxComObject.getDataGenerator() == null) {
                        continue;
                    }

                    final int address = knxComObject.getKnxGroupAddress().getAddress();
                    final int dataPointId = knxComObject.getNumber();
                    final String groupAddress = Utils.integerToKNXAddress(address, "/");

                    dataSender.send(device, connection, Utils.integerToKNXAddress(device.getPhysicalAddress(), "."),
                            groupAddress, dataPointId, knxComObject.getDataGenerator().getNextValue());
                }

            }

//            currentValue += increment;
//            if (currentValue >= MAX_VALUE) {
//                increment = -1;
//            }
//            if (currentValue <= MIN_VALUE) {
//                increment = 1;
//            }

            try {
                LOG.info(label + " Sleeping " + SLEEP_AMOUNT_IN_MILLIS + " ...");
                Thread.sleep(SLEEP_AMOUNT_IN_MILLIS);
                LOG.trace(label + " Sleeping \" + SLEEP_AMOUNT + \" done.");
            } catch (final InterruptedException e) {
                LOG.error(e.getMessage(), e);
                return;
            }
        }
    }

    public void stop() {
        LOG.info(label + " stop ...");
        done = true;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    public DataSender getDataSender() {
        return dataSender;
    }

    public void setDataSender(final DataSender dataSender) {
        this.dataSender = dataSender;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(final Connection connection) {
        this.connection = connection;
    }

    public void setDeviceService(final DeviceService deviceService) {
        this.deviceService = deviceService;
    }

//    public int getDeviceIndex() {
//        return deviceIndex;
//    }
//
//    public void setDeviceIndex(final int deviceIndex) {
//        this.deviceIndex = deviceIndex;
//    }

}
