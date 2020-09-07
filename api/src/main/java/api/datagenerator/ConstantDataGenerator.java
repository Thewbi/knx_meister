package api.datagenerator;

/**
 * The constant data generator will not trigger a PROPERT_CHANGE event in the
 * framework because the value is constant and does not change. Only changes in
 * value trigger a PROPERT_CHANGE event.
 */
public class ConstantDataGenerator implements DataGenerator {

    private double constant;

    @Override
    public Object getNextValue() {
        return constant;
    }

    public double getConstant() {
        return constant;
    }

    public void setConstant(final double constant) {
        this.constant = constant;
    }

    @Override
    public DataGeneratorType getDataGeneratorType() {
        return DataGeneratorType.CONSTANT;
    }

    @Override
    public String toString() {
        return "ConstantDataGenerator [getConstant()=" + getConstant() + ", Type=" + getDataGeneratorType() + "]";
    }

}
