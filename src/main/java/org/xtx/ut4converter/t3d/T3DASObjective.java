package org.xtx.ut4converter.t3d;

import org.xtx.ut4converter.MapConverter;
import org.xtx.ut4converter.ucore.ue4.BodyInstance;
import org.xtx.ut4converter.ucore.ue4.BodyInstance.ECollisionResponse;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Based on properties of UT4 Assault mod v1.6.1 Only UT99 support for the
 * moment
 *
 * @author XtremeXp
 *
 */
public class T3DASObjective extends T3DSound {

	/**
	 * Default pawn health. Used for objectives that can be damaged
	 */
	final double DEFAULT_PAWN_HEALTH = 100;

	/**
	 * Default message for objective completed in UT99
	 */
	final String DEFAULT_COMPLETED_OBJ_UT99 = "was destroyed!";

	/**
	 * If critical means this objective has to be completed to finish the map
	 * else this is a secondary/optional objective
	 */
	private boolean isCritical = true;

	/**
	 * Means this objective is completed by touching it UT99: Default is false
	 * UT4 MOD: default is true
	 */
	private boolean byTouch;

	/**
	 * Means this objective is completed by destroying it UT99: Default is true
	 * UT4 MOD: default is false
	 */
	private boolean byDamage = true;

	/**
	 * Objective health
	 */
	private Double objectiveHealth = 100d;

	/**
	 * Objective description
	 */
	private String objectiveDesc;

	/**
	 * Message displayed to players once objective completed
	 */
	private String completedText;

	/**
	 * UT4 AS Mod - Objective order Linked with defense priority
	 */
	private int order = 0;

	/**
	 * UT99 - The lower the value is the further the objective is (generally 0
	 * means last objective)
	 */
	private int defensePriority;

	/**
	 * UT4 AS Mod - Objective text displayed in hud (i guess ...)
	 */
	private String objectiveListText;

	// properties not handled by UT4 mod yet

	/**
	 * UT99 - For each step triggers this event
	 */
	private List<String> damageEvent;

	/**
	 * UT99 - For each damage level triggers events defined in "damageEvent"
	 */
	private List<String> damageEventThreshold;



	public T3DASObjective(MapConverter mc, String t3dClass) {
		super(mc, t3dClass);
	}

	@Override
	public boolean analyseT3DData(String line) {

		// FortName="The front doors"
		if (line.startsWith("FortName")) {
			objectiveDesc = T3DUtils.getString(line);
			objectiveListText = objectiveDesc;
		}

		// CompletedText="Test"
		else if (line.startsWith("CompletedText")) {
			completedText = T3DUtils.getString(line);
		}

		// DefensePriority=0 (0 = latest objective
		else if (line.startsWith("DefensePriority")) {
			defensePriority = T3DUtils.getInteger(line);
		}

		else if (line.startsWith("DestroyedMessage")) {
			completedText = T3DUtils.getString(line);
		}

		else if (line.startsWith("DamageEvent(")) {

			if (damageEvent == null) {
				damageEvent = new ArrayList<>();
			}

			damageEvent.add(T3DUtils.getString(line));
		}

		else if (line.startsWith("DamageEventThreshold(")) {

			if (damageEventThreshold == null) {
				damageEventThreshold = new ArrayList<>();
			}

			damageEventThreshold.add(T3DUtils.getString(line));
		}

		else if (line.startsWith("bTriggerOnly")) {
			byTouch = T3DUtils.getBoolean(line);
			byDamage = !byTouch;
		}

		// Pawn Property - Default is 100 (UT99)
		else if (line.startsWith("Health=")) {
			objectiveHealth = T3DUtils.getDouble(line);
		}

		else {
			return super.analyseT3DData(line);
		}

		return true;
	}



	public String toT3d() {

		sbf.append(IDT).append("Begin Actor Class=UTASObjective_C Name=").append(name).append("\n");
		sbf.append(IDT).append("\tBegin Object Class=SceneComponent Name=\"DefaultSceneRoot\"\n");
		writeEndObject();

		// Need to add collision component else objective would not trigger at
		// all
		// TODO check if needed if byDamage = false
		sbf.append(IDT).append("\tBegin Object Class=CapsuleComponent Name=\"Capsule\"\n");
		sbf.append(IDT).append("\t\tBegin Object Class=BodySetup Name=\"BodySetup_4546\"\n");
		sbf.append(IDT).append("\t\tEnd Object\n");
		writeEndObject();

		sbf.append(IDT).append("\tBegin Object Name=\"DefaultSceneRoot\"\n");
		writeLocRotAndScale();
		writeEndObject();

		sbf.append(IDT).append("\tBegin Object Name=\"Capsule\"\n");

		sbf.append(IDT).append("\tBegin Object Name=\"BodySetup_4546\"\n");
		sbf.append(IDT).append("\t\tCollisionTraceFlag=CTF_UseSimpleAsComplex\n");
		writeEndObject();

		sbf.append(IDT).append("\t\tCapsuleHalfHeight=").append(collisionHeight).append("\n");
		sbf.append(IDT).append("\t\tCapsuleRadius=").append(collisionRadius).append("\n");

		if (byTouch) {
			// TODO modify BodyInstance class to set profile trigger quickly
			sbf.append(IDT)
					.append("\t\tBodyInstance=(ResponseToChannels=(WorldStatic=ECR_Block,WorldDynamic=ECR_Block,Pawn=ECR_Block,Visibility=ECR_Block,Camera=ECR_Block,PhysicsBody=ECR_Block,Vehicle=ECR_Block,Destructible=ECR_Block,GameTraceChannel1=ECR_Block,GameTraceChannel2=ECR_Block,GameTraceChannel3=ECR_Block,GameTraceChannel4=ECR_Block,GameTraceChannel5=ECR_Block,GameTraceChannel6=ECR_Block,GameTraceChannel7=ECR_Block),CollisionProfileName=\"Trigger\",CollisionResponses=(ResponseArray=((Channel=\"WorldStatic\",Response=ECR_Overlap),(Channel=\"WorldDynamic\",Response=ECR_Overlap),(Channel=\"Pawn\",Response=ECR_Overlap),(Channel=\"Visibility\",Response=ECR_Ignore),(Channel=\"Camera\",Response=ECR_Overlap),(Channel=\"PhysicsBody\",Response=ECR_Overlap),(Channel=\"Vehicle\",Response=ECR_Overlap),(Channel=\"Destructible\",Response=ECR_Overlap),(Channel=\"Projectile\",Response=ECR_Ignore),(Channel=\"Weapon\",Response=ECR_Ignore),(Channel=\"ProjectileShootable\",Response=ECR_Ignore),(Channel=\"WeaponNoCharacter\",Response=ECR_Ignore),(Channel=\"TransDisk\",Response=ECR_Ignore))),MassInKg=131.720444)\n");
		} else {
			BodyInstance bi = new BodyInstance();
			bi.setCollisionResponse(ECollisionResponse.ECR_Block);
			sbf.append(IDT).append("\t");
			bi.toT3d(sbf);
			sbf.append("\n");
		}

		sbf.append(IDT).append("\t\tAttachParent=DefaultSceneRoot\n");
		sbf.append(IDT).append("\t\tCreationMethod=Instance\n");
		writeEndObject();

		if (objectiveDesc != null) {
			sbf.append(IDT).append("\tObjectiveDesc=\"").append(objectiveDesc).append("\"\n");
		}

		if (completedText != null) {
			sbf.append(IDT).append("\tCompletedText=\"").append(completedText).append("\"\n");
		}

		if (byDamage) {
			if (objectiveHealth != null) {
				sbf.append(IDT).append("\tObjectiveHealth=").append(objectiveHealth).append("\n");
			} else {
				sbf.append(IDT).append("\tObjectiveHealth=").append(DEFAULT_PAWN_HEALTH).append("\n");
			}

			// if not byDamage then objective type is destroy
			sbf.append(IDT).append("\t\tObjectiveType=NewEnumerator1\n");
		}

		sbf.append(IDT).append("\tisCritical=").append(isCritical).append("\n");
		sbf.append(IDT).append("\tOrder=").append(order).append("\n");

		sbf.append(IDT).append("\tRootComponent=DefaultSceneRoot\n");

		if (objectiveListText != null) {
			sbf.append(IDT).append("\tObjectiveListText=\"").append(objectiveListText).append("\"\n");
		}

		sbf.append(IDT).append("\tInstanceComponents(0)=CapsuleComponent'Capsule'\n");

		writeEndActor();

		return sbf.toString();
	}


	@Override
	public void convert() {

		if (completedText == null) {
			completedText = DEFAULT_COMPLETED_OBJ_UT99;
		}

		completedText = objectiveDesc + " " + completedText;

		// TODO get default values for UT99 fortstandards ...
		if (collisionRadius == null) {
			collisionRadius = 48d;
		}

		if (collisionHeight == null) {
			collisionHeight = 64d;
		}

		// find all objective actors sorted by defense priority descending
		final List<T3DActor> objSortedByDefensePriority = this.getMapConverter().getT3dLvlConvertor().getConvertedActors().stream().filter(t -> t instanceof T3DASObjective).sorted(Comparator.comparingInt(t -> ((T3DASObjective) t).getDefensePriority()).reversed()).collect(Collectors.toList());
		int idx = 0;
		int oldDefensePriority = -1;

		// recompute order for all objectives
		for (final T3DActor otherObjective : objSortedByDefensePriority) {

			if (otherObjective instanceof T3DASObjective) {
				T3DASObjective t3DASObjective = (T3DASObjective) otherObjective;

				if (oldDefensePriority != t3DASObjective.getDefensePriority()) {
					idx++;
				}

				t3DASObjective.setOrder((idx - 1));
				oldDefensePriority = t3DASObjective.getDefensePriority();
			}
		}

		super.convert();
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public void setDefensePriority(int defensePriority) {
		this.defensePriority = defensePriority;
	}

	public int getDefensePriority() {
		return defensePriority;
	}

	public String getObjectiveDesc() {
		return objectiveDesc;
	}

	public void setObjectiveDesc(String objectiveDesc) {
		this.objectiveDesc = objectiveDesc;
	}
}
