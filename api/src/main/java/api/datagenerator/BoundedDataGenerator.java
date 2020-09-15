package api.datagenerator;

public abstract class BoundedDataGenerator extends BaseDataGenerator {

    private double upperBound = 50;

    private double lowerBound = -20;

    public double getUpperBound() {
        return upperBound;
    }

    public void setUpperBound(final double upperBound) {
        this.upperBound = upperBound;
    }

    public double getLowerBound() {
        return lowerBound;
    }

    public void setLowerBound(final double lowerBound) {
        this.lowerBound = lowerBound;
    }

}
