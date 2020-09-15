package api.datagenerator;

public class SineDataGenerator extends RadianDataGenerator {

    @Override
    protected double retrieveFromRadians(final double radians) {
        return Math.sin(radians);
    }

}
