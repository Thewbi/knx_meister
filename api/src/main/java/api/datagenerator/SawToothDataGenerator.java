package api.datagenerator;

public class SawToothDataGenerator extends BoundedDataGenerator {

    private double currentValue = getLowerBound();

    private double increment = 1;

    @Override
    public Object getNextValue() {

        if (this.getDataGeneratorState() == DataGeneratorState.PAUSED) {
            return currentValue;
        }

        final double currentReturnValue = currentValue;

        currentValue += increment;
        if (currentValue >= getUpperBound()) {
            increment = -1;
        }
        if (currentValue <= getLowerBound()) {
            increment = 1;
        }

        return currentReturnValue;
    }

    public double getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(final double currentValue) {
        this.currentValue = currentValue;
    }

    @Override
    public DataGeneratorType getDataGeneratorType() {
        return DataGeneratorType.SAW;
    }

    @Override
    public String toString() {
        return getDataGeneratorType().name() + " [currentValue=" + currentValue + ", increment=" + increment
                + ", upperBound=" + getUpperBound() + ", lowerBound=" + getLowerBound() + "]";
    }

}