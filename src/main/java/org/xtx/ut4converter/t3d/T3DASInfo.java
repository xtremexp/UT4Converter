package org.xtx.ut4converter.t3d;

import org.xtx.ut4converter.MapConverter;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Basic implementation of "AssaultInfo" to "UTASInfo" from UT4 Assault Mode
 *
 * @author XtremeXp
 *
 */
public class T3DASInfo extends T3DActor {

	public T3DASInfo(MapConverter mc, String t3dClass) {
		super(mc, t3dClass);
		registerSimpleArrayProperty("ObjDesc", String.class);
	}


	public String toT3d() {

		sbf.append(IDT).append("Begin Actor Class=UTASInfo_C \n");
		sbf.append(IDT).append("\tBegin Object Class=SceneComponent Name=\"DefaultSceneRoot\"\n");
		sbf.append(IDT).append("\tEnd Object\n");
		sbf.append(IDT).append("\tBegin Object Name=\"DefaultSceneRoot\"\n");
		writeLocRotAndScale();
		sbf.append(IDT).append("\tEnd Object\n");
		sbf.append(IDT).append("\tDefaultSceneRoot=DefaultSceneRoot\n");
		sbf.append(IDT).append("\tMapTimeLimit=600\n");
		sbf.append(IDT).append("\tRootComponent=DefaultSceneRoot\n");

		writeEndActor();

		return sbf.toString();
	}


	@Override
	public void convert() {

		final T3DSimpleProperty propObj = this.registeredProperties.stream().filter(p -> "ObjDesc".equals(p.getPropertyName())).findFirst().orElse(null);

		if (propObj != null && propObj.getPropertyValue() instanceof List) {


			int idx = 0;
			int oldDefensePriority = -1;

			// get objective actors and sort them by defense priority descending
			final List<T3DActor> objSorted = this.getMapConverter().getT3dLvlConvertor().getConvertedActors().stream().filter(t -> t instanceof T3DASObjective).sorted(Comparator.comparingInt(t -> ((T3DASObjective) t).getDefensePriority()).reversed()).collect(Collectors.toList());

			for (T3DActor t3DActor : objSorted) {
				final T3DASObjective obj = (T3DASObjective) t3DActor;


				if (oldDefensePriority != obj.getDefensePriority()) {
					idx++;
				}

				obj.setObjectiveDesc(((List<Object>) propObj.getPropertyValue()).get((idx-1)).toString());
				oldDefensePriority = obj.getDefensePriority();
			}
		}

		// copy object
		super.convert();
	}

}
