package api.datagenerator;

import java.util.Random;

/**
 * Returns a random double value in the range [lowerBound, upperBound]
 */
public class RandomDataGenerator extends BoundedDataGenerator {

    private final Random random = new Random();

    private double lastValue = 0.0d;

    @Override
    public Object getNextValue() {

        if (this.getDataGeneratorState() == DataGeneratorState.PAUSED) {
            return lastValue;
        }

        lastValue = getLowerBound() + (getUpperBound() - getLowerBound()) * random.nextDouble();

        return lastValue;
    }

    @Override
    public DataGeneratorType getDataGeneratorType() {
        return DataGeneratorType.RANDOM;
    }

    @Override
    public String toString() {
        return "RandomDataGenerator [getUpperBound()=" + getUpperBound() + ", getLowerBound()=" + getLowerBound()
                + ", Type=" + getDataGeneratorType() + "]";
    }

}
