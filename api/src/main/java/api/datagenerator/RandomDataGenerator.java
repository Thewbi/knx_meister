package api.datagenerator;

import java.util.Random;

/**
 * Returns a random double value in the range [lowerBound, upperBound]
 */
public class RandomDataGenerator extends BoundedDataGenerator {

    private final Random random = new Random();

    @Override
    public Object getNextValue() {
        return getLowerBound() + (getUpperBound() - getLowerBound()) * random.nextDouble();
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
