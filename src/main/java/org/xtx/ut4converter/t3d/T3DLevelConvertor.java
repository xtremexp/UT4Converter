/*
$ * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.xtx.ut4converter.t3d;

import javafx.concurrent.Task;
import org.xtx.ut4converter.MapConverter;
import org.xtx.ut4converter.UTGameTypes;
import org.xtx.ut4converter.UTGames;
import org.xtx.ut4converter.ui.ConversionViewController;

import javax.vecmath.Vector3d;
import java.io.*;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Converts T3D Unreal 1 / Unreal Tournament to Unreal Tournament "4" t3d file
 *
 * @author XtremeXp
 */
@SuppressWarnings("restriction")
public class T3DLevelConvertor extends Task<Object> {

	/**
	 * Current map converter
	 */
	private MapConverter mapConverter;

	/**
	 * Input t3d file that need conversion
	 */
	private File inT3dFile;

	/**
	 * Converted input t3d file
	 */
	private File outT3dFile;

	/**
	 * Reader for input t3d file
	 */
	private BufferedReader bfr;

	/**
	 * Writer for converted t3d file
	 */
	private BufferedWriter bwr;

	/**
	 * Actors that were not converted.
	 */
	private SortedSet<String> unconvertedActors = new TreeSet<>();

	private LinkedList<T3DActor> convertedActors = new LinkedList<>();


	private boolean createNoteWhenUnconverted = true;

	private Logger logger;

	private Vector3d levelDimension;

	private Vector3d boundBoxLocalisation;

	private int actorCount;

	private boolean noUi;

	/**
	 * Unconverted properties
	 */
	private Map<String, Set<String>> unconvertedProperties = new HashMap<>();

	/**
	 * If true means we don't analyze t3d lines of current actor being parsed
	 */
	boolean banalyseline = false;

	private T3DActor uta = null;


	/**
	 *
	 * @param originalT3d
	 *            Original t3d ut3 file
	 * @param convertedT3d
	 *            New t3d file converted in UT4 t3d format
	 * @param mc
	 *            MapConverter options
	 */
	public T3DLevelConvertor(File originalT3d, File convertedT3d, MapConverter mc) {

		this.inT3dFile = originalT3d;
		this.outT3dFile = convertedT3d;
		this.mapConverter = mc;
		this.mapConverter.setT3dLvlConvertor(this);
		this.logger = mc.getLogger();

		initialise();
	}

	/**
     *
     */
	private void initialise() {

		ConversionViewController cont = mapConverter.getConversionViewController();

		if (cont == null) {
			return;
		}

		cont.getProgressBarDetail().progressProperty().bind(progressProperty());
		cont.getProgressIndicatorDetail().progressProperty().bind(progressProperty());
		cont.getProgressMessageDetail().textProperty().bind(messageProperty());

		updateProgress(0, 100);
	}

	/**
	 * Count the total number of actors. Used to track progress for conversion
	 */
	private void countTotalActors() throws Exception {

		if (inT3dFile == null || !inT3dFile.exists()) {
			assert inT3dFile != null;
			throw new Exception("File " + inT3dFile.getAbsolutePath() + " does not exists!");
		}

		try {

			bfr = new BufferedReader(new FileReader(inT3dFile));
			String line;

			// Read input t3d file and convert actors
			while ((line = bfr.readLine()) != null) {
				if (isBeginActor(line)) {
					actorCount++;
				}
				// for ut3 triggering "End Object" thingy ...
				else {
					isEndActor(line);
				}
			}
		} finally {
			bfr.close();
		}

	}

	/**
	 * Converts t3d file for final game
	 *
	 * @throws Exception Exception thrown
	 */
	public void readConvertAndWrite() throws Exception {

		if (inT3dFile == null || !inT3dFile.exists()) {
			assert inT3dFile != null;
			throw new Exception("File " + inT3dFile.getAbsolutePath() + " does not exists!");
		}

		try {
			countTotalActors();
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}

		logger.info("Converting t3d map " + inT3dFile.getName() + " to " + mapConverter.getOutputGame().name + " t3d level");

		// Read actor data from file
		if(!noUi) {
			updateMessage("Reading actors");
		}
		readActors();

		// Convert actors
		if(!noUi) {
			updateMessage("Converting actors");
		}

		convertActors();

		if(mapConverter.getFilteredClasses() == null || mapConverter.getFilteredClasses().length == 0) {
			addMapConversionNotesInfo();
		}

		// Write to file
		if(!noUi) {
			updateMessage("Writing actors");
		}
		writeActors();

		if(!noUi) {
			updateProgress(100, 100);
			updateMessage("All done!");
		}
	}

	/**
	 * Write info about original converted map
	 * and the scale ratior applied.
	 * Might be usefull if need to re-import actors
	 * to converted map, knowing which was the original scale.
	 */
	private void addMapConversionNotesInfo(){
		final T3DNote note = new T3DNote(mapConverter);
		note.setLocation(new Vector3d(0d, 200d, 0d));
		note.setName("NoteConvertedMap");
		note.setText("Converted Map: " + mapConverter.getInMap().getName());
		convertedActors.add(note);

		final T3DNote noteScale = new T3DNote(mapConverter);
		noteScale.setLocation(new Vector3d(0d, 300d, 0d));
		noteScale.setName("NoteScale");
		noteScale.setText("Scale: " + mapConverter.getScale());
		convertedActors.add(noteScale);
	}



	/**
	 * Find an actor by name
	 * @param actorName Actor name
	 * @return Actor found if any
	 */
	public T3DActor findActorByName(String actorName) {
		for (T3DActor uta : convertedActors) {
			if (uta != null && uta.getName() != null && uta.getName().equals(actorName)) {
				return uta;
			}
		}

		return null;
	}

	/**
	 * Write actors to .t3d file
	 *
	 * @throws IOException Exception thrown
	 */
	private void writeActors() throws IOException {
		try {

			bwr = new BufferedWriter(new FileWriter(outT3dFile));

			writeHeader();

			String buffer;

			if(!noUi) {
				updateProgress(0, convertedActors.size());
			}

			long actorsWriten = 0;

			for (T3DActor actor : convertedActors) {

				if (actor == null) {
					continue;
				}

				try {
					// write parent actor
					// actor not valid for conversion should not be written as well!
					if (actor.isValidWriting() && actor.isValidConverting()) {
						buffer = actor.toT3d();

						if (buffer != null) {
							bwr.write(buffer);
						}
					}

					// write replacement actors
					for (T3DActor repActor : actor.children) {
						if (repActor.isValidWriting()) {
							bwr.write(repActor.toT3d());
						}
					}
				} catch (Exception e) {
					logger.log(Level.SEVERE, "Error while writting actor " + actor.getName() + ":", e);
				}

				if(!noUi) {
					updateProgress(++actorsWriten, convertedActors.size());
				}
			}

			writeFooter();

			logger.info("Written file " + outT3dFile.getName());
		} catch (Exception e) {
			logger.log(Level.SEVERE, "ERROR:", e);
		} finally {
			bwr.close();
		}
	}

	/**
	 *
	 */
	private void convertActors() {

		int convertedActorsCount = 0;
		long xx = 80 - MapConverter.PROGRESS_BEFORE_CONVERT;
		if(!noUi) {
			updateProgress(0, convertedActors.size());
		}

		for (T3DActor uta : convertedActors) {
			if (uta != null) {

				try {
					if (uta.isValidConverting()) {
						// we might want to only re-scale map
						if (uta.getMapConverter().getOutputGame() != uta.getMapConverter().getInputGame()) {
							if(!noUi) {
								updateMessage("Converting " + uta.name);
							}
							uta.convert();
						}
						// rescale if needed
						// must always be done after convert
						uta.scale(mapConverter.getScale());
					}
				} catch (Exception e) {
					logger.log(Level.SEVERE, "Error converting actor " + uta.getName(), e);
					e.printStackTrace();
				}

			}

			convertedActorsCount ++;

			// update global conversion
			if(convertedActorsCount % 5 == 0 && !noUi){
				float pcDone = ((float) convertedActorsCount) / convertedActors.size();
				long newProgress = (long) (MapConverter.PROGRESS_BEFORE_CONVERT + xx * pcDone);
				mapConverter.updateMapConverterProgress(newProgress, 100);
				updateProgress(convertedActorsCount, convertedActors.size());
			}
		}

		logger.info(convertedActors.size() + " converted actors ");

	}

	/**
	 * Read actor data from original t3d level file
	 *
	 * @throws IOException Exception thrown
	 */
	private void readActors() throws IOException {

		try {

			bfr = new BufferedReader(new FileReader(inT3dFile));

			String line;
			// Current line of T3D File being analyzed
			int linenumber = 1;

			// Read input t3d file and convert actors
			while ((line = bfr.readLine()) != null) {
				try {
					analyzeLine(line);
					linenumber++;
				} catch (Exception e) {
					e.printStackTrace();
					logger.log(Level.SEVERE, "Error parsing Line #" + linenumber + " for " + inT3dFile.getName());
					if (uta != null) {
						logger.log(Level.SEVERE, "Current Actor Class: " + uta.t3dClass + " Line:");
					}
					logger.log(Level.SEVERE, "\"" + line + "\"");
					logger.log(Level.SEVERE, "ERROR:", e);
				}
			}

			logger.info(convertedActors.size() + " actors read");
		} finally {
			bfr.close();
		}
	}



	/**
	 * Says if current line is data for new actor
	 *
	 * @param line t3d data line
	 * @return <code>true</code> if lines describe a new actor
	 */
	private boolean isBeginActor(String line) {
		return line.contains("Begin Actor") || (this.mapConverter.getInputGame() == UTGames.UTGame.UT3 && line.startsWith("Begin Terrain "));
	}

	private boolean isEndActor(String line) {
		// Terrain actor does not begins with "Begin Actor Class=Terrain"
		return line.contains("End Actor") || (this.mapConverter.getInputGame() == UTGames.UTGame.UT3 && line.endsWith("End Terrain"));
	}

	private int actorsReadCount;

	/**
	 * Analyze T3D line to get and convert UT data
	 *
	 * @param line
	 *            current T3D line being read
	 * @throws Exception Exception thrown
	 */
	private void analyzeLine(String line) throws Exception {

		line = line.trim();

		// Current actor class
		Class<? extends T3DActor> utActorClass;

		if (isBeginActor(line)) {
			actorsReadCount++;

			// UT3 terrain is not an actor
			String currentClass;
			if (line.startsWith("Begin Terrain ")) {
				currentClass = "Terrain";
			} else {
				currentClass = getActorClass(line);
			}

			if (mapConverter.getSupportedActorClasses().canBeConverted(currentClass)) {
				utActorClass = mapConverter.getSupportedActorClasses().getConvertActorClass(currentClass);
				banalyseline = true;

				if (utActorClass != null) {
					Constructor<? extends T3DActor> cons = utActorClass.getConstructor(MapConverter.class, String.class);
					uta = cons.newInstance(mapConverter, currentClass);
					uta.setT3dOriginClass(currentClass);
					convertedActors.add(uta);
					uta.analyseT3DData(line);
				}
			} else {
				// skips some useless/uneeded actors to notify unconverted (e.g:
				// pathnodes for UE4/UT4)
				if (!mapConverter.getSupportedActorClasses().noNotifyUnconverted(currentClass)) {
					unconvertedActors.add(currentClass);

					if (createNoteWhenUnconverted) {
						banalyseline = true;
						uta = new T3DNote(mapConverter, "Unconverted: " + currentClass);
						uta.setT3dOriginClass(currentClass);
						uta.analyseT3DData(line);
						convertedActors.add(uta);
					} else {
						//logger.warning("Unconverted " + currentClass);
						banalyseline = false;
					}
				}
			}
		}

		// Actor End - We write converted data to t3d file
		else if (isEndActor(line)) {

			if(!noUi) {
				updateProgress(actorsReadCount, actorCount);
			}
			// Reset
			banalyseline = false;
			uta = null;
		}

		// Actor data being analyzed
		else {
			if (banalyseline && uta != null) {
				uta.preAnalyse(line);
				uta.analyseT3DData(line);
			}
		}
	}

	/**
	 * Returns current actor class from t3d line defining actor
	 *
	 * @param line
	 *            t3d line
	 * @return Actor class
	 */
	private String getActorClass(String line) {
		return (line.split("=")[1]).split(" ")[0];
	}

	/**
	 * Name of the big additive brush automatically added by converter
	 */
	public static final String THE_BIG_BRUSH_NAME = "BigAdditiveBrush";

	/**
	 * Write header of T3D file TODO check for UE1/UE2
	 *
	 * @throws IOException Exception thrown
	 */
	private void writeHeader() throws IOException {

		bwr.write("Begin Map\n");
		bwr.write("\tBegin Level Name=" + mapConverter.getUt4ReferenceBaseFolder() + "/" + mapConverter.getOutMapName() + "\n");

		// Auto creates a big additive brush surrounding level
		// to simulate creating a level in subtract mode (not existing in UE4
		// ...)
		if (mapConverter.isFromUE1UE2ToUE3UE4() && !mapConverter.hasClassFilter()) {

			double offset = 200d;
			Vector3d boundBox = getLevelDimensions();

			T3DBrush additiveBrush = T3DBrush.createBox(mapConverter, boundBox.x + offset, boundBox.y + offset, boundBox.z + offset);
			additiveBrush.location = boundBoxLocalisation;
			additiveBrush.name = THE_BIG_BRUSH_NAME;
			additiveBrush.brushClass = T3DBrush.BrushClass.Brush;

			// no need accurate light map resolution on this brush since it will
			// be never seen by player
			for (T3DPolygon p : additiveBrush.getPolyList()) {
				p.setLightMapScale( 2048d);
			}

			bwr.write(additiveBrush.toT3d());
		}
	}

	/**
	 *
	 * @return Level dimensions
	 */
	private Vector3d getLevelDimensions() {

		if (levelDimension == null) {
			Vector3d max = new Vector3d(0d, 0d, 0d);
			Vector3d min = new Vector3d(0d, 0d, 0d);

			// get the max/min boundaries of brush vertices on whole level
			for (T3DActor actor : convertedActors) {

				if (actor instanceof T3DBrush) {

					T3DBrush brush = (T3DBrush) actor;

					Vector3d maxA = brush.getMaxVertexPos();
					Vector3d minA = brush.getMinVertexPos();

					max.x = Math.max(max.x, maxA.x);
					max.y = Math.max(max.y, maxA.y);
					max.z = Math.max(max.z, maxA.z);

					min.x = Math.min(min.x, minA.x);
					min.y = Math.min(min.y, minA.y);
					min.z = Math.min(min.z, minA.z);
				}
			}

			// box dimensions that would fit perfectly the level in
			levelDimension = new Vector3d();
			levelDimension.x = Math.abs(max.x) + Math.abs(min.x);
			levelDimension.y = Math.abs(max.y) + Math.abs(min.y);
			levelDimension.z = Math.abs(max.z) + Math.abs(min.z);

			Vector3d loc = new Vector3d();
			loc.x = (max.x + min.x) / 2;
			loc.y = (max.y + min.y) / 2;
			loc.z = (max.z + min.z) / 2;

			boundBoxLocalisation = loc;
		}

		return levelDimension;
	}

	/**
	 * Default actor name of light mass importance volume
	 */
	public static final String LIGHTMASS_IMP_VOL_NAME = "LightMassImpVolume";

	/**
	 * Write footer of converted t3d file // Begin Map
	 * Name=/Game/RestrictedAssets/Maps/WIP/DM-SolarTest // Begin Level
	 * NAME=PersistentLevel TODO check for UE1/UE2
	 *
	 * @throws IOException Exception thrown
	 */
	private void writeFooter() throws IOException {

		if (mapConverter.toUnrealEngine4()) {

			Vector3d boundBox = getLevelDimensions();
			double offset = 100d;

			if (!mapConverter.hasClassFilter()) {
				// Automatically add a lightMassVolume around the whole level
				T3DBrush lightMassVolume = T3DBrush.createBox(mapConverter, boundBox.x + offset, boundBox.y + offset, boundBox.z + offset);
				lightMassVolume.location = boundBoxLocalisation;
				lightMassVolume.name = LIGHTMASS_IMP_VOL_NAME;
				lightMassVolume.brushClass = T3DBrush.BrushClass.LightmassImportanceVolume;
				bwr.write(lightMassVolume.toT3d());
			}

			// Automatically add a navigation volume
			// FIXME UED4 editor crashes for unknown reason
			/*
			 * T3DBrush navMeshBoundsVolume = T3DBrush.createBox(mapConverter,
			 * boundBox.x + offset, boundBox.y + offset, boundBox.z + offset);
			 * navMeshBoundsVolume.location = loc; navMeshBoundsVolume.name =
			 * "NavMeshBndsVolume"; navMeshBoundsVolume.brushClass =
			 * T3DBrush.BrushClass.NavMeshBoundsVolume;
			 * bwr.write(navMeshBoundsVolume.writeMoverProperties());
			 */

			// warn designer to do these steps
			// to be able to convert correctly objectives
			// since WorldInfo actor not imported by UE4 (which contained
			// gametype)
			if (UTGameTypes.isUt99Assault(mapConverter)) {
				logger.log(Level.WARNING, "After .t3d file import, set gametype to assault mode in world settings");
				logger.log(Level.WARNING, "delete all actors in map. Save and re-import the .t3d file");
				logger.log(Level.WARNING, "You should now see properly assault objectives !");
			}
		}

		bwr.write("\tEnd Level\n");
		bwr.write("\tBegin Surface\n");
		bwr.write("\tEnd Surface\n");
		bwr.write("End Map\n");
	}

	/**
	 *
	 * @param createNoteWhenUnconverted If true will create not within converted map for unconverted actors
	 */
	public void setCreateNoteWhenUnconverted(boolean createNoteWhenUnconverted) {
		this.createNoteWhenUnconverted = createNoteWhenUnconverted;
	}



	public Map<String, Set<String>> getUnconvertedProperties() {
		return unconvertedProperties;
	}

	/**
	 * Ignored unconverted properties that are not relevant to know they are not converted
	 * since there are useless ones.
	 */
	private static final List<String> IGNORED_UNCONVERTED_PROP = Arrays.asList("Region", "Level", "Brush", "Base", "PhysicsVolume", "myMarker", "StaticMeshInstance");

	/**
	 * Logs unconverted property from actor.
	 * Will help convertings these properties if they have not been converted yet!
	 * @param t3dClass T3d class of actor
	 * @param property Property name of actor that was not converted.
	 */
	void logUnconvertedProperty(final String t3dClass, final String property) {

		if(IGNORED_UNCONVERTED_PROP.contains(property)){
			return;
		}

		if (unconvertedProperties.containsKey(t3dClass)) {
			unconvertedProperties.get(t3dClass).add(property);
		} else {
			Set<String> props = new HashSet<>();
			props.add(property);
			unconvertedProperties.put(t3dClass, props);
		}
	}



	public SortedSet<String> getUnconvertedActors() {
		return unconvertedActors;
	}


	@Override
	protected Object call() throws Exception {
		readConvertAndWrite();
		return null;
	}

	public LinkedList<T3DActor> getConvertedActors() {
		return convertedActors;
	}

	public void setNoUi(boolean noUi) {
		this.noUi = noUi;
	}
}
