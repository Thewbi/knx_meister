package api.datagenerator;

import org.junit.jupiter.api.Test;

public class SineDataGeneratorTest {

    @Test
    public void testGenerateValue() {

        final SineDataGenerator sineDataGenerator = new SineDataGenerator();
        sineDataGenerator.setPercentage(0.0d);
        sineDataGenerator.setPercentageIncrement(.01d);

        for (int i = 0; i < 100; i++) {
            final Object nextValue = sineDataGenerator.getNextValue();
            System.out.println(String.format("%.6f", nextValue));
        }
    }

}
