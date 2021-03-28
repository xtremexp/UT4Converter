package org.xtx.ut4converter.t3d;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xtx.ut4converter.MapConverter;
import org.xtx.ut4converter.UTGames;

import java.io.IOException;
import java.util.List;


public class T3DASInfoTest {

    private MapConverter mc;

    @Before
    public void setUp() {
        this.mc = BaseTest.getMapConverterInstance(UTGames.UTGame.UT99);
    }

    /**
     * Test conversion of assault info actor.
     * Objective description must be copied to UTASObjective actors
     *
     * @throws IOException
     * @throws ReflectiveOperationException
     */
    @Test
    public void testConvertAssaultInfo() throws IOException, ReflectiveOperationException {

        // add 4 objectives to map (lower defense priority = latest objectives)
        // same count as ObjDesc in AssaultInfo actor
        final T3DASObjective obj1 = T3DASObjectiveTest.buildAndConvertObjectiveActor(this.mc,100);
        // same defense priority should have same objective description
        final T3DASObjective obj2a = T3DASObjectiveTest.buildAndConvertObjectiveActor(this.mc,50);
        final T3DASObjective obj2b = T3DASObjectiveTest.buildAndConvertObjectiveActor(this.mc,50);

        final T3DASObjective obj3 = T3DASObjectiveTest.buildAndConvertObjectiveActor(this.mc,20);
        final T3DASObjective obj4 = T3DASObjectiveTest.buildAndConvertObjectiveActor(this.mc,0);

        // add in map not in same order to test sorting
        this.mc.getT3dLvlConvertor().getConvertedActors().add(obj2a);
        this.mc.getT3dLvlConvertor().getConvertedActors().add(obj3);
        this.mc.getT3dLvlConvertor().getConvertedActors().add(obj2b);
        this.mc.getT3dLvlConvertor().getConvertedActors().add(obj4);
        this.mc.getT3dLvlConvertor().getConvertedActors().add(obj1);


        final T3DActor assaultInfoActor = BaseTest.parseFromT3d(this.mc, "AssaultInfo", T3DASInfo.class, T3DASInfoTest.class.getResource("/t3d/ue1/UT99-AssaultInfo.t3d").getPath());
        assaultInfoActor.convert();

        final T3DSimpleProperty propObj = assaultInfoActor.registeredProperties.stream().filter(p -> "ObjDesc".equals(p.getPropertyName())).findFirst().orElse(null);

        if (propObj != null) {
            List<String> objDescValues = (List<String>) propObj.getPropertyValue();

            // test UTASObjectiveActor have correct objective description set depending with order of objective
            Assert.assertEquals(objDescValues.get(0), obj1.getObjectiveDesc());
            // objectives with same defense priority must have same objective description
            Assert.assertEquals(objDescValues.get(1), obj2a.getObjectiveDesc());
            Assert.assertEquals(objDescValues.get(1), obj2b.getObjectiveDesc());
            Assert.assertEquals(objDescValues.get(2), obj3.getObjectiveDesc());
            Assert.assertEquals(objDescValues.get(3), obj4.getObjectiveDesc());
        } else {
            Assert.fail("Actor must have ObjDesc property converted");
        }
    }


}
