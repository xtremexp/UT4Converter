package org.xtx.ut4converter.t3d;

import org.xtx.ut4converter.MapConverter;

/**
 * Used for assault ut4 mode SpectatorCam(UT99) -> UTASCinematicCamera
 *
 * @author XtremeXp
 *
 */
public class T3DASCinematicCamera extends T3DActor {

	public T3DASCinematicCamera(MapConverter mc, String t3dClass) {
		super(mc, t3dClass);
	}


	public String toT3d() {

		writeBeginObj(sbf, IDT);

		sbf.append(IDT).append("\tBegin Object Class=CameraComponent Name=\"CameraComponent\"\n");
		sbf.append(IDT).append("\tEnd Object\n");

		sbf.append(IDT).append("\tBegin Object Name=\"CameraComponent\"\n");
		writeLocRotAndScale();
		sbf.append(IDT).append("\tEnd Object\n");

		sbf.append(IDT).append("\tCameraComponent=CameraComponent\n");
		sbf.append(IDT).append("\tRootComponent=CameraComponent\n");

		writeEndActor();

		return sbf.toString();
	}

}
