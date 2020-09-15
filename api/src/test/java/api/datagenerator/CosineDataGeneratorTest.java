package api.datagenerator;

import org.junit.jupiter.api.Test;

public class CosineDataGeneratorTest {

    @Test
    public void testGenerateValue() {

        final CosineDataGenerator cosineDataGenerator = new CosineDataGenerator();
        cosineDataGenerator.setPercentage(0.0d);
        cosineDataGenerator.setPercentageIncrement(.01d);

        for (int i = 0; i < 100; i++) {
            final Object nextValue = cosineDataGenerator.getNextValue();
            System.out.println(String.format("%.6f", nextValue));
        }
    }

}
