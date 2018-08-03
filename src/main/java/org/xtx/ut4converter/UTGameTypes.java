/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.xtx.ut4converter;

import org.xtx.ut4converter.UTGames.UTGame;

/**
 * 
 * @author XtremeXp
 */
public class UTGameTypes {

	public static final String UT4_ASSAULT_CLASS = "BlueprintGeneratedClass'/Game/Blueprints/UTASGameMode.UTASGameMode_C'";

	public static final String UT4_DOM_CLASS = "Blueprint'/Game/Domination/BP_Domination.BP_Domination'";

	/**
     *
     */
	public enum GameType {

		/**
         *
         */
		ASSAULT("AS", "Assault", true),

		/**
         *
         */
		CTF("CTF", "Capture the flag", true),

		/**
         *
         */
		DM("DM", "Death match", false),

		/**
         *
         */
		DUEL("DUEL", "Death match", false),

		/**
         *
         */
		TDM("TDM", "Team deathmatch", true),

		/**
        *
        */
		DOM("DOM", "Domination", true),

		/**
        *
        */
		BR("BR", "Bombing run", true);

		String prefix;
		String name;

		/**
         *
         */
		public boolean isTeamBased;

		GameType(String prefix, String name, boolean isTeamBased) {
			this.prefix = prefix;
			this.isTeamBased = isTeamBased;
		}
	}

	/**
     *
     */
	public static final String GAMETYPE_DEATHMATCH = "DM";

	/**
     *
     */
	public static final String GAMETYPE_TEAM_DEATHMATCH = "TDM";

	/**
     *
     */
	public static final String GAMETYPE_DUEL = "DUEL";

	/**
     *
     */
	public static final String GAMETYPE_ASSAULT = "AS";

	/**
    *
    */
	public static final String GAMETYPE_DOMINATION = "DOM";

	/**
    *
    */
	public static final String GAMETYPE_BOMBING_RUN = "BR";

	/**
	 *
	 * @param mapName
	 * @return
	 */
	public static boolean isTeamBasedFromMapName(String mapName) {

		if (mapName == null) {
			return false;
		}

		if (mapName.contains("-")) {
			String prefix = mapName.split("\\-")[0];
			GameType gameType = getGameType(prefix);

			if (gameType != null) {
				return gameType.isTeamBased;
			}
		}

		return false;
	}

	public static boolean isUt99Assault(MapConverter mc) {
		return mc.getInputGame() == UTGame.UT99 && getGameType(mc.getInMap().getName().split("\\-")[0]) == GameType.ASSAULT;
	}

	public static GameType getGameType(String prefix) {

		if (prefix == null) {
			return null;
		}

		prefix = prefix.toUpperCase();

		switch (prefix) {
		case "DM":
			return GameType.DM;
		case "AS":
			return GameType.ASSAULT;
		case "CTF":
			return GameType.CTF;
		case "DOM":
			return GameType.DOM;
		case "BR":
			return GameType.BR;
		default:
			return null;
		}
	}

	/**
	 * 
	 * @param gameType
	 * @return
	 */
	public static boolean isTeamBased(String gameType) {

		switch (gameType) {
		case GAMETYPE_DEATHMATCH:
			return false;
		case GAMETYPE_TEAM_DEATHMATCH:
			return true;
		case GAMETYPE_ASSAULT:
			return true;
		case GAMETYPE_DUEL:
			return false;
		case GAMETYPE_DOMINATION:
			return true;
		case GAMETYPE_BOMBING_RUN:
			return true;
		default:
			return false;
		}
	}
}
