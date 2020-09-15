package api.datagenerator;

public interface DataGenerator {

    Object getNextValue();

    DataGeneratorType getDataGeneratorType();

    boolean isPaused();

    default boolean isNotPaused() {
        return !isPaused();
    }

}
