package org.xtx.ut4converter.t3d;

import org.xtx.ut4converter.MapConverter;
import org.xtx.ut4converter.UTGames;
import org.xtx.ut4converter.config.model.UserConfig;
import org.xtx.ut4converter.config.model.UserGameConfig;
import org.xtx.ut4converter.ui.ConversionSettingsController;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class BaseTest {


    /**
     * Initialise
     * @param inputGame
     * @return
     */
    public static MapConverter getMapConverterInstance(final UTGames.UTGame inputGame){
        final MapConverter mc = new MapConverter(inputGame, UTGames.UTGame.UT4);

        if (inputGame == UTGames.UTGame.U2) {
            mc.setScale(Double.parseDouble(ConversionSettingsController.DEFAULT_SCALE_UNREAL2_UE4));
        } else {
            mc.setScale(Double.parseDouble(ConversionSettingsController.DEFAULT_SCALE_FACTOR_UE2_UE4));
        }

        final UserConfig uc = loadUserConfig();
        mc.setUserConfig(uc);

        mc.setInMap(new File("C:\\Temp\\mymap.uxx"));

        mc.setInMap(new File("C:\\Temp\\mymap.uxx")); // fake map file

        mc.setT3dLvlConvertor(new T3DLevelConvertor(null, null, mc));

        return mc;
    }

    /**
     * Load user test config from application.properties file
     *
     * @return
     */
    public static UserConfig loadUserConfig() {
        final UserConfig uc = new UserConfig();
        uc.setIsFirstRun(false);


        final List<UserGameConfig> userGameConfigs = new ArrayList<>();

        try (final InputStream input = BaseTest.class.getResourceAsStream("/application.properties")) {

            final Properties prop = new Properties();
            prop.load(input);

            userGameConfigs.add(new UserGameConfig(UTGames.UTGame.U1, new File(prop.getProperty("u1.path"))));
            userGameConfigs.add(new UserGameConfig(UTGames.UTGame.U2, new File(prop.getProperty("u2.path"))));
            userGameConfigs.add(new UserGameConfig(UTGames.UTGame.UT2003, new File(prop.getProperty("ut2003.path"))));
            userGameConfigs.add(new UserGameConfig(UTGames.UTGame.UT2004, new File(prop.getProperty("ut2004.path"))));
            userGameConfigs.add(new UserGameConfig(UTGames.UTGame.UT3, new File(prop.getProperty("ut3.path"))));

            uc.setGame(userGameConfigs);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return uc;
    }

    /**
     * Utility fonction, given an input game, read a .t3d file for a specific actor and parse it to build the actor
     *
     * @param inputClassName
     * @param utActorClass
     * @param t3dFilePath    "/src/test/resources/t3d/***.t3d
     * @return
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     */
    public static T3DActor parseFromT3d(final MapConverter mc, final String inputClassName, final Class<? extends T3DActor> utActorClass, final String t3dFilePath) throws ReflectiveOperationException, IOException {

        final Constructor<? extends T3DActor> cons = utActorClass.getConstructor(MapConverter.class, String.class);
        final T3DActor uta = cons.newInstance(mc, inputClassName);

        // read the t3d file line by line
        try (final FileReader fr = new FileReader(t3dFilePath); final BufferedReader bfr = new BufferedReader(fr)) {

            String line;

            while ((line = bfr.readLine()) != null) {
                // apply trim as used by the t3dlevel convertor
                line = line.trim();
                uta.analyseT3DData(line);
            }
        }

        return uta;
    }

    /**
     * Export single t3d actor within t3d level so it can be copy/paste easily within editor
     *
     * @param t3DActor
     * @return
     */
    public static String toUT4T3D(T3DActor t3DActor) {

        return "Begin Map\n" + "\tBegin Level\n" +
                t3DActor.toT3d() +
                "\tEnd Level\n" +
                "\tBegin Surface\n" +
                "\tEnd Surface\n" +
                "End Map";
    }

    public static void setMapFile(final MapConverter mc, final String inMap){
        mc.setInMap(new File(UTGames.getMapsFolder(mc.getUserConfig().getGameConfigByGame(mc.getInputGame()).getPath(), mc.getInputGame())  + "/" + inMap));
    }
}
