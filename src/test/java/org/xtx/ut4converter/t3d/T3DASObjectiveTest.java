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
        Assert.assertTrue(convertedT3d.contains("ObjectiveDesc=\"The front doors\""));
        Assert.assertTrue(convertedT3d.contains("ObjectiveListText=\"The front doors\""));
        Assert.assertTrue(convertedT3d.contains("CompletedText=\"The front doors are opened!\""));
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


    /**
     * Test objectives have correctly order property set, should be in reverse order as defensepriority
     *
     * @throws IOException
     * @throws ReflectiveOperationException
     */
    @Test
    public void testObjectiveOrder() throws IOException, ReflectiveOperationException {

        final T3DASObjective obj1 = T3DASObjectiveTest.buildAndConvertObjectiveActor(this.mc, 100);
        // same defense priority should have same objective description
        final T3DASObjective obj2a = T3DASObjectiveTest.buildAndConvertObjectiveActor(this.mc, 50);
        final T3DASObjective obj2b = T3DASObjectiveTest.buildAndConvertObjectiveActor(this.mc, 50);

        final T3DASObjective obj3 = T3DASObjectiveTest.buildAndConvertObjectiveActor(this.mc, 20);

        this.mc.getT3dLvlConvertor().getConvertedActors().add(obj2a);
        this.mc.getT3dLvlConvertor().getConvertedActors().add(obj3);
        this.mc.getT3dLvlConvertor().getConvertedActors().add(obj2b);
        this.mc.getT3dLvlConvertor().getConvertedActors().add(obj1);

        obj3.convert();
        obj2a.convert();
        obj2b.convert();
        obj1.convert();

        // order must be in reverse order as original defensePriority property
        Assert.assertEquals(0, obj1.getOrder());
        Assert.assertEquals(1, obj2a.getOrder());
        Assert.assertEquals(1, obj2b.getOrder());
        Assert.assertEquals(2, obj3.getOrder());
    }

    public static T3DASObjective buildAndConvertObjectiveActor(final MapConverter mc, int defensePriority) throws IOException, ReflectiveOperationException {
        // simulate existing objective actor converted within map
        final T3DActor objectiveActor = BaseTest.parseFromT3d(mc, "FortStandard", T3DASObjective.class, T3DASInfoTest.class.getResource("/t3d/ue1/UT99-FortStandard-Touch.t3d").getPath());
        T3DASObjective objCast = (T3DASObjective) objectiveActor;
        objCast.setDefensePriority(defensePriority);
        objCast.convert();

        return objCast;
    }
}
