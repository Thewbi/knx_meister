package api.datagenerator;

import api.datagenerator.dto.DataGeneratorDto;
import api.factory.Factory;
import api.factory.exception.FactoryException;

public class DefaultDataGeneratorFactory implements Factory<DataGenerator> {

    @Override
    public DataGenerator create(final Object... args) throws FactoryException {

        final DataGeneratorDto dataGeneratorDto = (DataGeneratorDto) args[0];

        DataGenerator dataGenerator = null;

        switch (dataGeneratorDto.getDataGeneratorType()) {

        case CONSTANT:
            final ConstantDataGenerator constantDataGenerator = new ConstantDataGenerator();
            constantDataGenerator.setConstant(dataGeneratorDto.getConstant());
            dataGenerator = constantDataGenerator;
            break;

        case RANDOM:
            final RandomDataGenerator randomDataGenerator = new RandomDataGenerator();
            randomDataGenerator.setUpperBound(dataGeneratorDto.getUpperBound());
            randomDataGenerator.setLowerBound(dataGeneratorDto.getLowerBound());
            dataGenerator = randomDataGenerator;
            break;

        case SAW:
            final SawToothDataGenerator sawToothDataGenerator = new SawToothDataGenerator();
            sawToothDataGenerator.setUpperBound(dataGeneratorDto.getUpperBound());
            sawToothDataGenerator.setLowerBound(dataGeneratorDto.getLowerBound());
            sawToothDataGenerator.setCurrentValue(dataGeneratorDto.getLowerBound());
            dataGenerator = sawToothDataGenerator;
            break;

        default:
            throw new RuntimeException("Unknown types!");
        }

        return dataGenerator;
    }

}
