package api.datagenerator;

public abstract class RadianDataGenerator extends BaseDataGenerator {

    private double percentage = 0.0d;

    private double percentageIncrement = .01d;

    abstract protected double retrieveFromRadians(final double radians);

    @Override
    public Object getNextValue() {

        final double radians = percentageToRadians(percentage);

        final double sin = retrieveFromRadians(radians);

        percentage += percentageIncrement;
        if (percentage >= 100.0d) {
            percentage = 0.0d;
        }

        return sin;
    }

    private double percentageToRadians(double percentage) {

        if (percentage > 100.0d) {
            percentage = 100.0d;
        }
        if (percentage < 0.0d) {
            percentage = 0.0d;
        }

        return 2 * Math.PI * percentage;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(final double percentage) {
        this.percentage = percentage;
    }

    public double getPercentageIncrement() {
        return percentageIncrement;
    }

    public void setPercentageIncrement(final double percentageIncrement) {
        this.percentageIncrement = percentageIncrement;
    }

}
