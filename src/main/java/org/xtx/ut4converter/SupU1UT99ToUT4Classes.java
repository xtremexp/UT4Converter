/*'
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.xtx.ut4converter;

import java.util.HashMap;
import org.xtx.ut4converter.t3d.T3DBrush;
import org.xtx.ut4converter.t3d.T3DLight;
import org.xtx.ut4converter.t3d.T3DMatch.Match;
import org.xtx.ut4converter.t3d.T3DMover;
import org.xtx.ut4converter.t3d.T3DPlayerStart;
import org.xtx.ut4converter.t3d.T3DSound;
import org.xtx.ut4converter.t3d.T3DTeleporter;
        
/**
 * FXML Controller class
 * @author XtremeXp
 */
public class SupU1UT99ToUT4Classes extends SupportedClasses {
    
    MapConverter mapConv;
    
    /**
     *
     * @param mc
     */
    public SupU1UT99ToUT4Classes(MapConverter mc) {
        super();
        this.mapConv = mc;
        
        putUtClass(T3DBrush.class, "Brush");
        putUtClass(T3DMover.class, "Mover", "AttachMover", "AssertMover", "RotatingMover", "ElevatorMover", "MixMover", "GradualMover", "LoopMover");
        putUtClass(T3DPlayerStart.class, "PlayerStart");
        
        putUtClass(T3DLight.class, "Light", "SpotLight", "ChargeLight");
        putUtClass(T3DTeleporter.class, "Teleporter", "VisibleTeleporter", "VisibleTeleporter");
        putUtClass(T3DSound.class, "AmbientSound", "DynamicAmbientSound");
        //addPickups();
        
        addMatches(mc);
        
        uneededActors.add("PathNode");
        uneededActors.add("InventorySpot");
    }
    
    private void addMatches(MapConverter mc){
        HashMap<String, Match> hm = mc.getActorClassMatch();
        
        for(String c : hm.keySet()){
            putUtClass(hm.get(c).t3dClass, c);
        }
    }

    /**
     *
     */
    public SupU1UT99ToUT4Classes() {
        super();
    }
    
    /**
     * For testing purposes only.
     * Converts only one actor so it's easier
     * to import it in map being converted
     * @param actor
     * @param t3dClass 
     */
    public void setConvertOnly(String actor, Class t3dClass){
        classToUtActor.clear();
        classToUtActor.put(actor.toLowerCase(), t3dClass);
    }

}
