package api.datagenerator;

public abstract class BaseDataGenerator implements DataGenerator {

    private int id;

    private DataGeneratorType dataGeneratorType;

    private DataGeneratorState dataGeneratorState = DataGeneratorState.RUNNING;

    @Override
    public abstract Object getNextValue();

    @Override
    public boolean isPaused() {
        return dataGeneratorState == DataGeneratorState.PAUSED;
    }

    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    @Override
    public DataGeneratorType getDataGeneratorType() {
        return dataGeneratorType;
    }

    public void setDataGeneratorType(final DataGeneratorType dataGeneratorType) {
        this.dataGeneratorType = dataGeneratorType;
    }

    public DataGeneratorState getDataGeneratorState() {
        return dataGeneratorState;
    }

    public void setDataGeneratorState(final DataGeneratorState dataGeneratorState) {
        this.dataGeneratorState = dataGeneratorState;
    }

}
