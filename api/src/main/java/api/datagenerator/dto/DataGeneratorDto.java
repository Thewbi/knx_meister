package api.datagenerator.dto;

import api.datagenerator.DataGeneratorType;

public class DataGeneratorDto {

    private int id;

    private DataGeneratorType dataGeneratorType;

    private double upperBound;

    private double lowerBound;

    private double constant;

    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public DataGeneratorType getDataGeneratorType() {
        return dataGeneratorType;
    }

    public void setDataGeneratorType(final DataGeneratorType dataGeneratorType) {
        this.dataGeneratorType = dataGeneratorType;
    }

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

    public double getConstant() {
        return constant;
    }

    public void setConstant(final double constant) {
        this.constant = constant;
    }

}
