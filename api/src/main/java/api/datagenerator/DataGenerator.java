package api.datagenerator;

public interface DataGenerator {

    Object getNextValue();

    DataGeneratorType getDataGeneratorType();

}
