package api.datagenerator;

public class CosineDataGenerator extends RadianDataGenerator {

    @Override
    protected double retrieveFromRadians(final double radians) {
        return Math.cos(radians);
    }

}
