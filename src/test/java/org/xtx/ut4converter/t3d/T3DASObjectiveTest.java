package org.xtx.ut4converter.t3d;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xtx.ut4converter.MapConverter;
import org.xtx.ut4converter.UTGames;

import java.io.IOException;


public class T3DASObjectiveTest {

    private MapConverter mc;

    @Before
    public void setUp() {
        this.mc = BaseTest.getMapConverterInstance(UTGames.UTGame.UT99);
    }

    /**
     * Test conversion of touch objective
     *
     * @throws IOException
     * @throws ReflectiveOperationException
     */
    @Test
    public void testConvertTouchObjective() throws IOException, ReflectiveOperationException {
        final T3DActor actor = BaseTest.parseFromT3d(this.mc, "FortStandard", T3DASObjective.class, T3DASObjectiveTest.class.getResource("/t3d/ue1/UT99-FortStandard-Touch.t3d").getPath());
        actor.convert();

        final String convertedT3d = BaseTest.toUT4T3D(actor);
        // test type objective = touch (ObectiveType default value)
        Assert.assertFalse(convertedT3d.contains("ObjectiveType=NewEnumerator1"));
    }

    /**
     * Test conversion of destroy objective
     *
     * @throws IOException
     * @throws ReflectiveOperationException
     */
    @Test
    public void testConvertDestroyObjective() throws IOException, ReflectiveOperationException {
        final T3DActor actor = BaseTest.parseFromT3d(this.mc, "FortStandard", T3DASObjective.class, T3DASObjectiveTest.class.getResource("/t3d/ue1/UT99-FortStandard-FinalDestroy.t3d").getPath());
        actor.convert();

        final String convertedT3d = BaseTest.toUT4T3D(actor);

        // test type objective = destroy
        Assert.assertTrue(convertedT3d.contains("ObjectiveHealth=500.0"));

        // test type objective = destroy
        Assert.assertTrue(convertedT3d.contains("ObjectiveType=NewEnumerator1"));
    }
}
