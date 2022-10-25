/*
 * Copyright 2015-2020 Ray Fowler
 *
 * Licensed under the GNU General Public License, Version 3 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rotp.model.empires;

import static rotp.model.empires.Race.crEmpireNameRandom;
import static rotp.model.game.DynOptions.loadOptions;
import static rotp.ui.UserPreferences.playerCustomRace;
import static rotp.ui.UserPreferences.randomAlienRaces;
import static rotp.ui.UserPreferences.randomAlienRacesMax;
import static rotp.ui.UserPreferences.randomAlienRacesMin;
import static rotp.ui.UserPreferences.randomAlienRacesSmoothEdges;
import static rotp.ui.UserPreferences.randomAlienRacesTargetMax;
import static rotp.ui.UserPreferences.randomAlienRacesTargetMin;
import static rotp.ui.util.SettingBase.CostFormula.DIFFERENCE;
import static rotp.util.Base.random;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import br.profileManager.src.main.java.PMutil;
import rotp.Rotp;
import rotp.model.game.DynOptions;
import rotp.model.game.MOO1GameOptions;
import rotp.model.planet.PlanetType;
import rotp.ui.util.Modifier2KeysState;
import rotp.ui.util.SettingBase;
import rotp.ui.util.SettingBoolean;
import rotp.ui.util.SettingInteger;
import rotp.ui.util.SettingString;

public class CustomRaceDefinitions  {
	
	public	static final String ROOT	= "CUSTOM_RACE_";
	private	static final String PLANET	= "PLANET_";
	private	static final String EXT		= ".race";
	public	static final String RANDOMiZED_RACE_KEY	= "RANDOMIZED_RACE";
	public	static final String RANDOM_RACE_KEY		= "RANDOM_RACE_KEY";
	public	static final String CUSTOM_RACE_KEY		= "CUSTOM_RACE_KEY";
	private static final boolean booleansAreBullet	= true;

	private Race race; // !!! To be kept up to date !!!
	private final LinkedList<SettingBase<?>> settingList = new LinkedList<>(); // !!! To be kept up to date !!!
	private final LinkedList<SettingBase<?>> guiList	 = new LinkedList<>();

	private final SettingInteger randomTargetMax = new SettingInteger(
			ROOT, "RANDOM_TARGET_MAX", 75, null, null, 1, 5, 20);
	private final SettingInteger randomTargetMin = new SettingInteger(
			ROOT, "RANDOM_TARGET_MIN", 0, null, null, 1, 5, 20);
	private final SettingInteger randomMax = new SettingInteger(
			ROOT, "RANDOM_MAX", 50, -100, 100, 1, 5, 20);
	private final SettingInteger randomMin = new SettingInteger(
			ROOT, "RANDOM_MIN", -50, -100, 100, 1, 5, 20);
	private final SettingBoolean randomUseTarget = new SettingBoolean(
			ROOT, "RANDOM_USE_TARGET", false);
	private final SettingBoolean randomSmoothEdges = new SettingBoolean(
			ROOT, "RANDOM_EDGES", true);

	private LinkedList<Integer> spacerList; // For UI
	private LinkedList<Integer> columnList; // For UI
	private RaceList raceList;
	
	// ========== Constructors and Initializers ==========
	//
	public CustomRaceDefinitions() {
		newSettingList();
		pushSettings();
	}
	public CustomRaceDefinitions(Race race) {
		newSettingList();
		setRace(race.name());
	}
	public CustomRaceDefinitions(DynOptions srcOptions) {
		newSettingList();
		fromOptions(srcOptions);
		pushSettings();
	}
	private CustomRaceDefinitions(String fileName) {
		this(loadOptions(Rotp.jarPath(), fileName + EXT));
	}
	// -------------------- Static Methods --------------------
	// 
	private static Race getRandomAlienRace() {
		CustomRaceDefinitions cr = new CustomRaceDefinitions();
		cr.randomizeRace(randomAlienRacesMin.get(), randomAlienRacesMax.get(),
				randomAlienRacesTargetMin.get(), randomAlienRacesTargetMax.get(),
				randomAlienRaces.isTarget(), randomAlienRacesSmoothEdges.get(), false);
		return cr.getRace().isCustomRace(true);
	}
	public static Race getAlienRace(String key, DynOptions options) {
		if (key.equalsIgnoreCase(RANDOM_RACE_KEY)) {
			return getRandomAlienRace();
		}
		if (key.equalsIgnoreCase(CUSTOM_RACE_KEY)) {
			DynOptions opt = (DynOptions) playerCustomRace.get();
			return new CustomRaceDefinitions(opt).getRace();
		}
		if (options == null) // load from file
			return new CustomRaceDefinitions(key).getRace();
		else
			return new CustomRaceDefinitions(options).getRace();
	}
	static Race keyToRace(String raceKey) {
		if (raceKey.equalsIgnoreCase(RANDOM_RACE_KEY)) {
			return getRandomAlienRace();
		}
		if (raceKey.equalsIgnoreCase(CUSTOM_RACE_KEY)) {
			DynOptions opt = (DynOptions) playerCustomRace.get();
			return new CustomRaceDefinitions(opt).getRace();
		}
		// load from file
		return new CustomRaceDefinitions(raceKey).getRace();
	}
	public static DynOptions getDefaultOptions() {
		return new CustomRaceDefinitions().getAsOptions();
	}
	// ========== Options Management ==========
	//
	/**
	 * Settings to DynOptions
	 * @return DynOptions
	 */
	public DynOptions getAsOptions() {
		DynOptions destOptions = new DynOptions();
		for (SettingBase<?> setting : settingList)
			setting.setOptions(destOptions);
		for (SettingBase<?> setting : guiList)
			setting.setOptions(destOptions);
		return destOptions;
	}
	/**
	 * DynOptions to settings and race
	 * @param srcOptions
	 */
	public void fromOptions(DynOptions srcOptions) {
		for (SettingBase<?> setting : settingList)
			setting.setFromOptions(srcOptions);
		for (SettingBase<?> setting : guiList)
			setting.setFromOptions(srcOptions);
		pushSettings();
	}
	/**
	 * race to settings
	 */
	public void setFromRaceToShow(Race race) {
		this.race = race;
		pullSettings();
	}
	private void saveSettingList(String path, String fileName) {
		getAsOptions().save(path, fileName);
	}
	/**
	 * DynOptions to settings and race
	 */
	private void loadSettingList(String path, String fileName) {
		fromOptions(loadOptions(path, fileName));
	}
	private String fileName() { return race.id + EXT; }
	public void saveRace() { saveSettingList(Rotp.jarPath(), fileName()); }
	public void loadRace() {
		if (Race.isValidKey(race.id))
			setRace(race.id);
		else
			loadSettingList(Rotp.jarPath(), fileName());
	}
	// ========== Main Getters ==========
	//
	public LinkedList<SettingBase<?>> settingList()	{ return settingList; }
	public LinkedList<SettingBase<?>> guiList()		{ return guiList; }
	public LinkedList<Integer>		  spacerList()	{ return spacerList; }
	public LinkedList<Integer>		  columnList()	{ return columnList; }
	public RaceList initRaceList()	{ 
		raceList = new RaceList();
		return raceList;
	}
	public Race getRace() {
		pushSettings();
		race.raceOptions(getAsOptions());
		race.isCustomRace(true);
		return race;
	}
	// ========== Other Methods ==========
	//
	/**
	 * @param raceKey the new race
	 */
	public void setRace(String raceKey) {
		race = Race.keyed(raceKey).copy();
		pullSettings();
	}
	public int getCount() {
		int count = 0;
		for (SettingBase<?> setting : settingList) {
			if (!setting.isSpacer()
					&& !setting.hasNoCost())
				count++;
		}
		return count;
	}
	/**
	 * race is not up to date
	 */
	private void randomizeRace(float min, float max,
			float targetMin, float targetMax, boolean gaussian, boolean updateGui) {
		float target	= (targetMax + targetMin)/2;
		float maxDiff	= Math.max(0.04f, Math.abs(targetMax-targetMin)/2);
		float maxChange	= Math.max(0.1f, Math.abs(max-min));
		
		List<SettingBase<?>> shuffledSettingList = new ArrayList<>(settingList);
		// first pass full random
		randomizeRace(min, max, gaussian, updateGui);
		float cost = getTotalCost();
		
		// second pass going smoothly to the target
		for (int i=0; i<10; i++) {
			Collections.shuffle(shuffledSettingList);
			for (SettingBase<?> setting : shuffledSettingList) {
				if (!setting.isSpacer() &&!setting.hasNoCost()) {
					float difference = target - cost;
					if (Math.abs(difference) <= maxDiff)
						return;
					float costFactor = setting.costFactor();
					float changeRequest = difference/costFactor;
					cost -= setting.settingCost();
					if (Math.abs(changeRequest) <= maxChange) {
						float maxRequest = changeRequest + maxDiff/costFactor;
						if (maxRequest > 0)
							maxRequest = Math.min(maxChange, maxRequest);
						else
							maxRequest =  Math.max(-maxChange, maxRequest);						
						float minRequest = changeRequest - maxDiff/costFactor;
						if (maxRequest > 0)
							minRequest = Math.min(maxChange, minRequest);
						else
							minRequest =  Math.max(-maxChange, minRequest);
						float request = minRequest + random.nextFloat() * (maxRequest-minRequest);

						setting.setValueFromCost(setting.settingCost()+request*costFactor);
					} else {
						setting.setRandom(setting.lastRandomSource()
								+ maxChange*Math.signum(changeRequest));
					}
					cost += setting.settingCost();
					if (updateGui)
						setting.guiSelect();
				}
			}				
		}
		// third pass forcing the target
		for (int i=0; i<5; i++) {
			Collections.shuffle(shuffledSettingList);
			for (SettingBase<?> setting : shuffledSettingList) {
				if (!setting.isSpacer() &&!setting.hasNoCost()) {
					cost -= setting.settingCost();
					setting.setValueFromCost(target - cost);
					cost += setting.settingCost();
					if (updateGui)
						setting.guiSelect();
					if (Math.abs(cost-target) <= maxDiff)
						return;
				}
			}				
		}
	}
	/**
	 * race is not up to date
	 */
	private void randomizeRace(float min, float max, boolean gaussian, boolean updateGui) {
		for (SettingBase<?> setting : settingList) {
			if (!setting.isSpacer()) {
				setting.setRandom(min, max, gaussian);
				if (updateGui)
					setting.guiSelect();
			}
		}
	}
	public void randomizeRace(boolean updateGui) {
		randomizeRace(randomMin.settingValue(), randomMax.settingValue(),
			randomTargetMin.settingValue(), randomTargetMax.settingValue(),
			randomUseTarget.settingValue(), randomSmoothEdges.settingValue(), updateGui);
		pushSettings();
	}
	/**
	 * race is not up to date
	 */
	private void randomizeRace(float min, float max, float targetMin, float targetMax, 
			boolean useTarget, boolean gaussian, boolean updateGui) {
		if (useTarget)
			randomizeRace(min, max, targetMin, targetMax, gaussian, updateGui);
		else
			randomizeRace(min, max, gaussian, updateGui);
	}
	public  float getTotalCost() {
		float totalCost = 0;
		for (SettingBase<?> setting : settingList) {
			if (setting.isSpacer())
				continue;
			totalCost += setting.settingCost();
		}
		return totalCost;
	}
	private void pushSettings() {
		for (SettingBase<?> setting : settingList) {
			setting.pushSetting();
		}
	}
	public void pullSettings() {
		for (SettingBase<?> setting : settingList) {
			setting.pullSetting();
		}
	}
	private float updateSettings() {
		for (SettingBase<?> setting : settingList) {
			setting.updateGui();
		}
		return getTotalCost();
	}

	private void newSettingList() {
		spacerList  = new LinkedList<>();
		columnList  = new LinkedList<>();
		
		// First column (left)
		settingList.add(new BaseDataRace());
		endOfColumn();

		// ====================
		// Second column
		settingList.add(new RaceKey());
		settingList.add(new RaceName());
		settingList.add(new EmpireName());
		settingList.add(new RaceDescription1());
		settingList.add(new RaceDescription2());
		settingList.add(new RaceDescription4());
		settingList.add(new RaceDescription3());
		endOfColumn();

		// ====================
		// Third column
//		settingList.add(new RacePlanetType()); // not yet differentiated
		settingList.add(new HomeworldSize());
//		settingList.add(new SpeciesType()); // Not used in Game
		settingList.add(new PopGrowRate());
		settingList.add(new IgnoresEco());
		spacer();
		settingList.add(new ProdWorker());
		settingList.add(new ProdControl());
		settingList.add(new IgnoresFactoryRefit());
		settingList.add(new TechDiscovery());
		settingList.add(new TechResearch());
		spacer();
		settingList.add(new SpyCost());
		settingList.add(new SpySecurity());
		settingList.add(new SpyInfiltration());
//		settingList.add(new SpyTelepathy()); // Not used in Game
		spacer();
		settingList.add(new DiplomacyTrade());
// 		settingList.add(new DiploPosDP()); // Not used in Game
		settingList.add(new DiplomacyBonus());
		settingList.add(new DiplomacyCouncil());
		settingList.add(new RelationDefault());	// BR: Maybe All the races
		spacer();
		endOfColumn();

		// ====================
		// Fourth column
		settingList.add(new PlanetRessources());
		settingList.add(new PlanetEnvironment());
		settingList.add(new CreditsBonus());
		settingList.add(new HitPointsBonus());
		settingList.add(new MaintenanceBonus());
		settingList.add(new ShipSpaceBonus());
		spacer();
		settingList.add(new ShipAttack());
		settingList.add(new ShipDefense());
		settingList.add(new ShipInitiative());
		settingList.add(new GroundAttack());
		spacer();
		settingList.add(new ResearchComputer());
		settingList.add(new ResearchConstruction());
		settingList.add(new ResearchForceField());
		settingList.add(new ResearchPlanet());
		settingList.add(new ResearchPropulsion());
		settingList.add(new ResearchWeapon());
		spacer();
		endOfColumn();

		// ====================
		// Fifth column
		// endOfColumn();
		// ====================
		guiList.add(randomSmoothEdges);
	    guiList.add(randomMin);
	    guiList.add(randomMax);
	    guiList.add(randomTargetMin);
	    guiList.add(randomTargetMax);
	    guiList.add(randomUseTarget);	    
	    for(SettingBase<?> setting : guiList)
	    	setting.hasNoCost(true);
	}
	private void endOfColumn()	{ columnList.add(settingList.size()); }
	private void spacer()		{ spacerList.add(settingList.size()); }
	// ==================== Nested Classes ====================
	//
	// ==================== RaceList ====================
	//
	public class RaceList extends SettingBase<String> {
		
		private boolean newValue = false;
		private boolean reload	 = false;

		public RaceList() {
			super(ROOT, "RACE_LIST");
			isBullet(true);
			labelsAreFinals(true);
			hasNoCost(true);
			reload();
		}
		// ---------- Initializers ----------
		//
		public void reload() {
			String currentValue = settingValue();
			clearLists();
			add((DynOptions) playerCustomRace.get());
			defaultIndex(0);
			File[] fileList = loadListing();
			if (fileList != null)
				for (File file : fileList)
					add(DynOptions.loadOptions(file));
			initOptionsText();
			reload = true;
			set(currentValue);
		}
	    private File[] loadListing() {
	        String path	= Rotp.jarPath();
	        File saveDir = new File(path);
	        FilenameFilter filter = (File dir, String name1) -> name1.toLowerCase().endsWith(EXT);
	        File[] fileList = saveDir.listFiles(filter);
	        return fileList;
	    }
	    private void add(DynOptions opt) {
	    	CustomRaceDefinitions cr = new CustomRaceDefinitions(opt);
	    	Race dr = cr.getRace();
	    	String cfgValue	 = dr.setupName;
	    	String langLabel = dr.id;
	    	String tooltipKey = dr.description3;
	    	float cost = cr.getTotalCost();
	    	put(cfgValue, langLabel, cost, langLabel, tooltipKey);
	    }
	    public boolean newValue() {
	    	if (newValue) {
	    		newValue = false;
	    		return true;
	    	}
	    	return false;
	    }
		// ---------- Overriders ----------
		//
		@Override public String guiSettingValue() {
			return lmText(guiOptionLabel());
		}
		@Override public String guiOptionValue(int index) {
			return lmText(guiOptionLabel(index));
		}
		@Override protected void selectedValue(String value) {
			super.selectedValue(value);
			if (reload) { // No need to load options on reload
				reload = false;
				return;
			}
			if (index() == 0) {
				fromOptions((DynOptions) playerCustomRace.get());
				newValue = true;
				return;
			}
			File file = new File(Rotp.jarPath(), settingValue()+EXT);
			if (file.exists()) {
				fromOptions(DynOptions.loadOptions(file));
				newValue = true;
			}
		}
	}
	// ==================== RaceKey ====================
	//
	private class RaceKey extends SettingString {
		private RaceKey() {
			super(ROOT, "RACE_KEY", "CustomRace", 1);
			inputMessage("Enter the Race File Name");
			randomStr(RANDOMiZED_RACE_KEY);
		}
		@Override public void pushSetting() {
			race.id = settingValue();
		}
		@Override public void pullSetting() {
			set(race.id);
		}
	}
	// ==================== RaceName ====================
	//
	private class RaceName extends SettingString {
		private RaceName() {
			super(ROOT, "RACE_NAME", "Custom Race", 1);
			inputMessage("Enter the Race Name");
			randomStr("");
		}
		@Override public void pushSetting() {
			race.setupName = settingValue();
		}
		@Override public void pullSetting() {
			if (race.setupName == null)
				set(race.setupName());
			else
				set(race.setupName);				
		}
	}
	// ==================== EmpireName ====================
	//
	private class EmpireName extends SettingString {
		private EmpireName() {
			super(ROOT, "RACE_EMPIRE_NAME", "Custom Empire", 1);
			inputMessage("Enter the Empire Designation");
			randomStr(crEmpireNameRandom);
		}
		@Override public void pushSetting() {
			race.empireTitle = settingValue();
		}
		@Override public void pullSetting() {
			if (race.empireTitle == null)
				set(race.empireTitle());
			else
				set(race.empireTitle);				
		}
	}
	// ==================== RaceDescription1 ====================
	//
	private class RaceDescription1 extends SettingString {
		private RaceDescription1() {
			super(ROOT, "RACE_DESC_1", "Description 1", 2);
			inputMessage("Enter the Description");
			randomStr("Randomized");
		}
		@Override public void pushSetting() {
			race.description1 = settingValue();
		}
		@Override public void pullSetting() {
			set(race.description1);		
		}
	}
	// ==================== RaceDescription2 ====================
	//
	private class RaceDescription2 extends SettingString {
		private RaceDescription2() {
			super(ROOT, "RACE_DESC_2", "Description 2", 2);
			inputMessage("Enter the Description");
			randomStr("Randomized");
		}
		@Override public void pushSetting() {
			race.description2 = settingValue();
		}
		@Override public void pullSetting() {
			set(race.description2);		
		}
	}
	// ==================== RaceDescription3 ====================
	//
	private class RaceDescription3 extends SettingString {
		private RaceDescription3() {
			super(ROOT, "RACE_DESC_3", "Description 3", 3);
			inputMessage("Enter the Description");
			randomStr("Randomized");
		}
		@Override public void pushSetting() {
			race.description3 = settingValue();
		}
		@Override public void pullSetting() {
			set(race.description3);		
		}
	}
	// ==================== RaceDescription4 ====================
	//
	private class RaceDescription4 extends SettingString {
		private RaceDescription4() {
			super(ROOT, "RACE_DESC_4", "Description 4", 3);
			inputMessage("Enter the Description");
			randomStr("Randomized");
		}
		@Override public void pushSetting() {
			race.description4 = settingValue();
		}
		@Override public void pullSetting() {
			set(race.description4);		
		}
	}
	// ==================== BaseDataRace ====================
	//
	private class BaseDataRace extends SettingBase<String> {
		private boolean updateAllowed	= true;
		private boolean pullAllowed		= true;
		private boolean costInitialized	= false;
		private BaseDataRace() {
			super(ROOT, "BASE_DATARACE");
			isBullet(true);
			labelsAreFinals(true);
			hasNoCost(true);
			for (String race : MOO1GameOptions.allRaceOptions()) {
				Race r = Race.keyed(race);
				String name = r.setupName(); // Probably useless
				put(PMutil.suggestedUserViewFromCodeView(race), name, 0f, race);
			}
			defaultIndex(0);
			initOptionsText();
		}
		@Override public void updateGui() {
			if (updateAllowed) {
				String raceKey = settingValue();
				setRace(raceKey);
				super.updateGui();
				updateAllowed = false;
				pullSettings();
				updateSettings();
				updateAllowed = true;
			}
		}
		@Override public void guiSelect() {
			if (race == null) {
				pushSetting();
				updateGui();
				return;
			}
			race.baseRace = settingValue();
			switch (Modifier2KeysState.get()) {
			case CTRL:
			case CTRL_SHIFT:
				pushSetting();
				updateGui();
				return;
			default:
				super.updateGui();
			}
		}
		@Override public void pushSetting() {
			race = Race.keyed(settingValue()).copy();
			race.baseRace = settingValue();
		}
		@Override public void pullSetting() {
			if (!pullAllowed)
				return;
			if(!costInitialized) {
				String raceKey = race.name();
				pullAllowed = false;
				for (int i=0; i<listSize(); i++) {
					index(i);
					race = Race.keyed(settingValue()).copy();
					pullSettings();
					newCost(i, getTotalCost());
				}
				set(raceKey);
				race = Race.keyed(settingValue()).copy();
				pullSettings();
				pullAllowed = true;
				costInitialized = true;
			}
			set(race.baseRace());
		}
	}
	// ==================== CreditsBonus ====================
	//
	private class CreditsBonus extends SettingInteger {
		// big = good
		private CreditsBonus() {
			super(ROOT, "CREDIT", 0, 0, 35, 1, 5, 20,
					DIFFERENCE, new float[]{0f, .8f}, new float[]{0f, .8f});
		}
		@Override public void pushSetting() {
			race.bCBonus((float) settingValue()/100);
		}
		@Override public void pullSetting() {
			set(Math.round(race.bCBonus() * 100));
		}
	}
	// ==================== HitPointsBonus ====================
	//
	private class HitPointsBonus extends SettingInteger {
		// big = good
		private HitPointsBonus() {
			super(ROOT, "HIT_POINTS", 100, 50, 200, 1, 5, 20,
					DIFFERENCE, new float[]{0f, .4f}, new float[]{0f, .6f});
		}
		@Override public void pushSetting() {
			race.hPFactor((float) settingValue()/100);
		}
		@Override public void pullSetting() {
			set(Math.round(race.hPFactor() * 100));
		}
	}
	// ==================== ShipSpaceBonus ====================
	//
	// Absolute min = ? .75 not OK for colony building!
	private class ShipSpaceBonus extends SettingInteger {
		// big = good
		private ShipSpaceBonus() {
			super(ROOT, "SHIP_SPACE", 100, 80, 175, 1, 5, 20,
					DIFFERENCE, new float[]{0f, 1f}, new float[]{0f, 2f});
		}
		@Override public void pushSetting() {
			race.shipSpaceFactor((float) settingValue()/100);
		}
		@Override public void pullSetting() {
			set(Math.round(race.shipSpaceFactor() * 100));
		}
	}
	// ==================== MaintenanceBonus ====================
	//
	private class MaintenanceBonus extends SettingInteger {
		// Big = bad
		public MaintenanceBonus() {
			super(ROOT, "MAINTENANCE", 100, 50, 200, 1, 5, 20,
					DIFFERENCE, new float[]{0f, -.2f}, new float[]{0f, -.4f});
		}
		@Override public void pushSetting() {
			race.maintenanceFactor((float) settingValue()/100);
		}
		@Override public void pullSetting() {
			set(Math.round(race.maintenanceFactor() * 100));
		}
	}
	// ==================== PlanetRessources ====================
	//
	private class PlanetRessources extends SettingBase<String> {
		private static final String defaultValue = "Normal";
		
		public PlanetRessources() {
			super(ROOT, "HOME_RESOURCES");
			isBullet(true);
			labelsAreFinals(true);
			put("UltraPoor",	PLANET + "ULTRA_POOR",		-50f, "UltraPoor");
			put("Poor",			PLANET + "POOR",			-25f, "Poor");
			put("Normal",		ROOT   + "RESOURCES_NORMAL",  0f, "Normal");
			put("Rich",			PLANET + "RICH",			 30f, "Rich");
			put("UltraRich",	PLANET + "ULTRA_RICH",		 50f, "UltraRich");
			put("Artifacts",	PLANET + "ARTIFACTS",		 40f, "Artifacts");
			defaultCfgValue(defaultValue);
			initOptionsText();
		}
		@Override public void pushSetting() {
			race.planetRessource(settingValue());
		}
		@Override public void pullSetting() {
			set(race.planetRessource());
		}
	}
	// ==================== PlanetEnvironment ====================
	//
	private class PlanetEnvironment extends SettingBase<String> {
		private static final String defaultValue = "Normal";
		
		private PlanetEnvironment() {
			super(ROOT, "HOME_ENVIRONMENT");
			isBullet(true);
			labelsAreFinals(true);
			put("Hostile", PLANET + "HOSTILE",			  -20f, "Hostile");
			put("Normal",  ROOT   + "ENVIRONMENT_NORMAL",	0f, "Normal");
			put("Fertile", PLANET + "FERTILE",			   15f, "Fertile");
			put("Gaia",	   PLANET + "GAIA",				   30f, "Gaia");
			defaultCfgValue(defaultValue);
			initOptionsText();
		}
		@Override public void pushSetting() {
			race.planetEnvironment(settingValue());
		}
		@Override public void pullSetting() {
			set(race.planetEnvironment());
		}
	}
	// ==================== PlanetType ====================
	//
	@SuppressWarnings("unused")
	private class RacePlanetType extends SettingBase<String> {
		private static final String defaultValue = "Terran";
		
		private RacePlanetType() {
			super(ROOT, "HOME_TYPE");
			isBullet(true);
			labelsAreFinals(true);
//			put("Ocean",	PlanetType.OCEAN,	-8f, PlanetType.OCEAN);
//			put("Jungle",	PlanetType.JUNGLE,	-5f, PlanetType.JUNGLE);
//			put("Terran",	PlanetType.TERRAN,	 0f, PlanetType.TERRAN);
			put("Ocean",	PlanetType.OCEAN,	0f, PlanetType.OCEAN);
			put("Jungle",	PlanetType.JUNGLE,	0f, PlanetType.JUNGLE);
			put("Terran",	PlanetType.TERRAN,	0f, PlanetType.TERRAN);
			defaultCfgValue(defaultValue);
			initOptionsText();
			hasNoCost(true); // to be removed
		}
		@Override public void pushSetting() {
			race.homeworldPlanetType = settingValue();
		}
		@Override public void pullSetting() {
			set(race.homeworldPlanetType);
		}
	}
	// ==================== HomeworldSize ====================
	//
	private class HomeworldSize extends SettingInteger {
		private HomeworldSize() {
			super(ROOT, "HOME_SIZE", 100, 70, 150, 1, 5, 20,
					DIFFERENCE, new float[]{0f, .4f}, new float[]{0f, .7f});
		}
		@Override public void pushSetting() {
			race.homeworldSize = settingValue();
		}
		@Override public void pullSetting() {
			set(race.homeworldSize);
		}
	}
	// ==================== SpeciesType ====================
	//
	@SuppressWarnings("unused")
	private class SpeciesType extends SettingBase<Integer> {
		private static final String defaultValue = "Terran";
		
		private SpeciesType() {
			super(ROOT, "RACE_TYPE");
			isBullet(true);
			labelsAreFinals(true);
			put("Terran",	"RACE_TERRAN",   0f, 1);
			put("Aquatic",	"RACE_AQUATIC",  2f, 2);
			put("Silicate",	"RACE_SILICATE", 4f, 3);
			put("Robotic",	"RACE_ROBOTIC",	 4f, 4);
			defaultCfgValue(defaultValue);
			initOptionsText();
		}
		@Override public void pushSetting() {
			race.speciesType = settingValue();
		}
		@Override public void pullSetting() {
			set(race.speciesType);
		}
	}
	// ==================== IgnoreEco ====================
	//
	private class IgnoresEco extends SettingBoolean {
		private static final boolean defaultValue = false;
		
		private IgnoresEco() {
			super(ROOT, "IGNORES_ECO", defaultValue, 50f, 0f);
			isBullet(booleansAreBullet);
			initOptionsText();
		}
		@Override public void pushSetting() {
			race.ignoresPlanetEnvironment = settingValue();
		}
		@Override public void pullSetting() {
			set(race.ignoresPlanetEnvironment);
		}
	}
	// ==================== PopGrowRate ====================
	//
	private class PopGrowRate extends SettingInteger {
		private PopGrowRate() {
			super(ROOT, "POP_GROW_RATE", 100, 50, 200, 1, 5, 20,
					DIFFERENCE, new float[]{0f, .2f, .003f}, new float[]{0f, .3f});
		}
		@Override public void pushSetting() {
			race.growthRateMod = (float) settingValue()/100;
		}
		@Override public void pullSetting() {
			set(Math.round (race.growthRateMod * 100));
		}
	}
	// ==================== ShipAttack ====================
	//
	private class ShipAttack extends SettingInteger {
		private ShipAttack() {
			super(ROOT, "SHIP_ATTACK", 0, -1, 5, 1, 1, 1,
					DIFFERENCE, new float[]{0f, 3f}, new float[]{0f, 5f});
			initOptionsText();
		}
		@Override public void pushSetting() {
			race.shipAttackBonus(settingValue());
		}
		@Override public void pullSetting() {
			set(race.shipAttackBonus());
		}
	}
	// ==================== ShipDefense ====================
	//
	private class ShipDefense extends SettingInteger {
		private ShipDefense() {
			super(ROOT, "SHIP_DEFENSE", 0, -1, 5, 1, 1, 1,
					DIFFERENCE, new float[]{0f, 1.5f, 1.5f}, new float[]{0f, 6f});
			initOptionsText();
		}
		@Override public void pushSetting() {
			race.shipDefenseBonus(settingValue());
		}
		@Override public void pullSetting() {
			set(race.shipDefenseBonus());
		}
	}
	// ==================== ShipInitiative ====================
	//
	private class ShipInitiative extends SettingInteger {
		private ShipInitiative() {
			super(ROOT, "SHIP_INITIATIVE", 0, -1, 5, 1, 1, 1,
					DIFFERENCE, new float[]{5f, 1f}, new float[]{0f, 6f});
			initOptionsText();
		}
		@Override public void pushSetting() {
			race.shipInitiativeBonus(settingValue());
		}
		@Override public void pullSetting() {
			set(race.shipInitiativeBonus());
		}
	}
	// ==================== GroundAttack ====================
	//
	private class GroundAttack extends SettingInteger {
		private GroundAttack() {
			super(ROOT, "GROUND_ATTACK", 0, -20, 30, 1, 5, 20,
					DIFFERENCE, new float[]{0f, 1.25f}, new float[]{0f, 0.75f});
			initOptionsText();
		}
		@Override public void pushSetting() {
			race.groundAttackBonus(settingValue());
		}
		@Override public void pullSetting() {
			set(race.groundAttackBonus());
		}
	}
	// ==================== SpyCost ====================
	//
	private class SpyCost extends SettingInteger {
		private SpyCost() {
			super(ROOT, "SPY_COST", 100, 50, 200, 1, 5, 20,
					DIFFERENCE, new float[]{0f, -.1f}, new float[]{0f, -.2f});
			initOptionsText();
		}
		@Override public void pushSetting() {
			race.spyCostMod = (float) settingValue()/100;
		}
		@Override public void pullSetting() {
			set(Math.round(race.spyCostMod * 100));
		}
	}
	// ==================== SpySecurity ====================
	//
	private class SpySecurity extends SettingInteger {
		private SpySecurity() {
			super(ROOT, "SPY_SECURITY", 0, -20, 40, 1, 5, 20,
					DIFFERENCE, new float[]{0f, 1f}, new float[]{0f, 2f});
			initOptionsText();
		}
		@Override public void pushSetting() {
			race.internalSecurityAdj = (float) settingValue()/100;
		}
		@Override public void pullSetting() {
			set(Math.round(race.internalSecurityAdj * 100));
		}
	}
	// ==================== SpyInfiltration ====================
	//
	private class SpyInfiltration extends SettingInteger {
		private SpyInfiltration() {
			super(ROOT, "SPY_INFILTRATION", 0, -20, 40, 1, 5, 20,
					DIFFERENCE, new float[]{0f, 1.25f}, new float[]{0f, 2.5f});
			initOptionsText();
		}
		@Override public void pushSetting() {
			race.spyInfiltrationAdj = (float) settingValue()/100;
		}
		@Override public void pullSetting() {
			set(Math.round(race.spyInfiltrationAdj * 100));
		}
	}
	// ==================== SpyTelepathy ====================
	//
	@SuppressWarnings("unused")
	private class SpyTelepathy extends SettingBoolean {
		private static final boolean defaultValue = false;
		
		private SpyTelepathy() {
			super(ROOT, "SPY_TELEPATHY", defaultValue, 20f, 0f);
			isBullet(booleansAreBullet);
			initOptionsText();
		}
		@Override public void pushSetting() {
			race.telepathic = settingValue();
		}
		@Override public void pullSetting() {
			set(race.telepathic);
		}
	}
	// ==================== DiplomacyTrade ====================
	//
	private class DiplomacyTrade extends SettingInteger {
		private DiplomacyTrade() {
			super(ROOT, "DIPLOMACY_TRADE", 0, -30, 30, 1, 5, 20,
					DIFFERENCE, new float[]{0f, .4f}, new float[]{0f, .3f});
			initOptionsText();
		}
		@Override public void pushSetting() {
			race.tradePctBonus = (float) settingValue()/100;
		}
		@Override public void pullSetting() {
			set(Math.round(race.tradePctBonus * 100));
		}
	}
	// ==================== DiploPosDP ====================
	//
	@SuppressWarnings("unused")
	private class DiploPosDP extends SettingInteger {
		private DiploPosDP() {
			super(ROOT, "DIPLO_POS_DP", 100, 70, 200, 1, 5, 20,
					DIFFERENCE, new float[]{0f, .3f}, new float[]{0f, .8f});
			initOptionsText();
		}
		@Override public void pushSetting() {
			race.positiveDPMod = (float) settingValue()/100;
		}
		@Override public void pullSetting() {
			set(Math.round(race.positiveDPMod * 100));
		}
	}
	// ==================== DiplomacyBonus ====================
	//
	private class DiplomacyBonus extends SettingInteger {
		private DiplomacyBonus() {
			super(ROOT, "DIPLOMACY_BONUS", 0, -50, 100, 1, 5, 20,
					DIFFERENCE, new float[]{0f, .1f}, new float[]{0f, .2f});
			initOptionsText();
		}
		@Override public void pushSetting() {
			race.diplomacyBonus = settingValue();
		}
		@Override public void pullSetting() {
			set(race.diplomacyBonus);
		}
	}
	// ==================== DiplomacyCouncil ====================
	//
	private class DiplomacyCouncil extends SettingInteger {
		private DiplomacyCouncil() {
			super(ROOT, "DIPLOMACY_COUNCIL", 0, -25, 25, 1, 5, 20,
					DIFFERENCE, new float[]{0f, .2f}, new float[]{0f, .2f});
			initOptionsText();
		}
		@Override public void pushSetting() {
			race.councilBonus = (float) settingValue()/100;
		}
		@Override public void pullSetting() {
			set(Math.round(race.councilBonus * 100));
		}
	}
	// ==================== RelationDefault ====================
	//
	private class RelationDefault extends SettingInteger {
		private RelationDefault() {
			super(ROOT, "RELATION_DEFAULT", 0, -10, 10, 1, 2, 4,
					DIFFERENCE, new float[]{0f, .4f}, new float[]{0f, .4f});
			initOptionsText();
		}
		@Override public void pushSetting() {
			race.defaultRaceRelations(settingValue());
		}
		@Override public void pullSetting() {
			set((int)race.defaultRaceRelations());
		}
	}
	// ==================== ProdWorker ====================
	//
	private class ProdWorker extends SettingInteger {
		// bigger = better
		private ProdWorker() {
			super(ROOT, "PROD_WORKER", 100, 70, 200, 1, 5, 20,
					DIFFERENCE, new float[]{0f, .3f, 0.003f}, new float[]{0f, 0.8f, 0.006f});
			initOptionsText();
		}
		@Override public void pushSetting() {
			race.workerProductivityMod = (float) settingValue()/100;
		}
		@Override public void pullSetting() {
			set(Math.round(race.workerProductivityMod * 100));
		}
	}
	// ==================== ProdControl ====================
	//
	private class ProdControl extends SettingInteger {
		private ProdControl() {
			super(ROOT, "PROD_CONTROL", 0, -1, 4, 1, 1, 1,
					DIFFERENCE, new float[]{0f, 15f}, new float[]{0f, 30f});
			initOptionsText();
		}
		@Override public void pushSetting() {
			race.robotControlsAdj = settingValue();
		}
		@Override public void pullSetting() {
			set(race.robotControlsAdj);
		}
	}
	// ==================== IgnoresFactoryRefit ====================
	//
	private class IgnoresFactoryRefit extends SettingBoolean {
		private static final boolean defaultValue = false;
		
		private IgnoresFactoryRefit() {
			super(ROOT, "PROD_REFIT_COST", defaultValue, 20f, 0f);
			isBullet(booleansAreBullet);
			initOptionsText();
		}
		@Override public void pushSetting() {
			race.ignoresFactoryRefit = settingValue();
		}
		@Override public void pullSetting() {
			set(race.ignoresFactoryRefit());
		}
	}
	// ==================== TechDiscovery ====================
	//
	private class TechDiscovery extends SettingInteger {
		// bigger = better
		private TechDiscovery() {
			super(ROOT, "TECH_DISCOVERY", 50, 30, 100, 1, 5, 20,
					DIFFERENCE, new float[]{0f, .5f}, new float[]{0f, 0.5f});
			initOptionsText();
		}
		@Override public void pushSetting() {
			race.techDiscoveryPct = (float) settingValue()/100;
		}
		@Override public void pullSetting() {
			set(Math.round(race.techDiscoveryPct * 100));
		}
	}
	// ==================== TechResearch ====================
	//
	private class TechResearch extends SettingInteger {
		// bigger = better
		private TechResearch() {
			super(ROOT, "TECH_RESEARCH", 100, 60, 200, 1, 5, 20, DIFFERENCE,
					new float[]{0f, 0.7f, 0.004f},
					new float[]{0f, 1.0f, 0.006f});
			initOptionsText();
		}
		@Override public void pushSetting() {
			race.researchBonusPct = (float) settingValue()/100;
		}
		@Override public void pullSetting() {
			set(Math.round(race.researchBonusPct * 100));
		}
	}
	// ==================== ResearchComputer ====================
	//
	private static final int studyCostMin = 50;
	private static final int studyCostMax = 200;
	private static final float researchC1pos = -.0f;
	private static final float researchC1neg = -.0f;
	private static final float researchC2pos = -.0012f;
	private static final float researchC2neg = -.005f;

	private class ResearchComputer extends SettingInteger {
		// smaller = better
		private ResearchComputer() {
			super(ROOT, "RESEARCH_COMPUTER", 100, studyCostMin, studyCostMax,
					1, 5, 20, DIFFERENCE, 
					new float[]{0f, researchC1pos, researchC2pos},
					new float[]{0f, researchC1neg, researchC2neg});
			initOptionsText();
		}
		@Override public void pushSetting() {
			race.techMod[0] = (float) settingValue()/100;
		}
		@Override public void pullSetting() {
			set(Math.round(race.techMod[0] * 100));
		}
	}
	// ==================== ResearchConstruction ====================
	//
	private class ResearchConstruction extends SettingInteger {
		private ResearchConstruction() {
			super(ROOT, "RESEARCH_CONSTRUCTION", 100, studyCostMin, studyCostMax,
					1, 5, 20, DIFFERENCE, 
					new float[]{0f, researchC1pos, researchC2pos},
					new float[]{0f, researchC1neg, researchC2neg});
			initOptionsText();
		}
		@Override public void pushSetting() {
			race.techMod[1] = (float) settingValue()/100;
		}
		@Override public void pullSetting() {
			set(Math.round(race.techMod[1] * 100));
		}
	}
	// ==================== ResearchForceField ====================
	//
	private class ResearchForceField extends SettingInteger {
		private ResearchForceField() {
			super(ROOT, "RESEARCH_FORCEFIELD", 100, studyCostMin, studyCostMax,
					1, 5, 20, DIFFERENCE, 
					new float[]{0f, researchC1pos, researchC2pos},
					new float[]{0f, researchC1neg, researchC2neg});
			initOptionsText();
		}
		@Override public void pushSetting() {
			race.techMod[2] = (float) settingValue()/100;
		}
		@Override public void pullSetting() {
			set(Math.round(race.techMod[2] * 100));
		}
	}
	// ==================== ResearchPlanet ====================
	//
	private class ResearchPlanet extends SettingInteger {
		private ResearchPlanet() {
			super(ROOT, "RESEARCH_PLANET", 100, studyCostMin, studyCostMax,
					1, 5, 20, DIFFERENCE, 
					new float[]{0f, researchC1pos, researchC2pos},
					new float[]{0f, researchC1neg, researchC2neg});
			initOptionsText();
		}
		@Override public void pushSetting() {
			race.techMod[3] = (float) settingValue()/100;
		}
		@Override public void pullSetting() {
			set(Math.round(race.techMod[3] * 100));
		}
	}
	// ==================== ResearchPropulsion ====================
	//
	private class ResearchPropulsion extends SettingInteger {
		private ResearchPropulsion() {
			super(ROOT, "RESEARCH_PROPULSION", 100, studyCostMin, studyCostMax,
					1, 5, 20, DIFFERENCE, 
					new float[]{0f, researchC1pos, researchC2pos},
					new float[]{0f, researchC1neg, researchC2neg});
			initOptionsText();
		}
		@Override public void pushSetting() {
			race.techMod[4] = (float) settingValue()/100;
		}
		@Override public void pullSetting() {
			set(Math.round(race.techMod[4] * 100));
		}
	}
	// ==================== ResearchWeapon ====================
	//
	private class ResearchWeapon extends SettingInteger {
		private ResearchWeapon() {
			super(ROOT, "RESEARCH_WEAPON", 100, studyCostMin, studyCostMax,
					1, 5, 20, DIFFERENCE, 
					new float[]{0f, researchC1pos, researchC2pos},
					new float[]{0f, researchC1neg, researchC2neg});
			initOptionsText();
		}
		@Override public void pushSetting() {
			race.techMod[5] = (float) settingValue()/100;
		}
		@Override public void pullSetting() {
			set(Math.round(race.techMod[5] * 100));
		}
	}
}