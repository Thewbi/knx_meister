package api.datagenerator;

public class ConstantDataGenerator implements DataGenerator {

    private double constant;

    @Override
    public Object getNextValue() {
        return constant++;
    }

    public double getConstant() {
        return constant;
    }

    public void setConstant(final double constant) {
        this.constant = constant;
    }

}
