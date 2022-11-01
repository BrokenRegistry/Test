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

package rotp.ui.util;

public class RandomAlienRaces extends ParamList {
	
	public static final String NO		= "No";
	public static final String SETTING	= "Setting";
	public static final String TARGET	= "Target";
	public static final String PLAYER	= "Player";
	public static final String FILES	= "Files";

	/**
	 * @param gui  The label header
	 * @param name The name
	 * @param defaultValue The default value
	 */
	public RandomAlienRaces(String gui, String name, String defaultValue) {
		super(gui, name, defaultValue);
		put(NO,		 gui + name + "_NO");
		put(SETTING, gui + name + "_SETTING");
		put(TARGET,	 gui + name + "_TARGET");
		put(PLAYER,	 gui + name + "_PLAYER");
		put(FILES,	 gui + name + "_FROM_FILES");
	}

	public boolean isRandom() {
		return !get().equalsIgnoreCase(NO);
	}
	public boolean isSettingWindow() {
		return get().equalsIgnoreCase(SETTING);
	}
	public boolean isTarget() {
		return get().equalsIgnoreCase(TARGET);
	}
	public boolean isPlayerCopy() {
		return get().equalsIgnoreCase(PLAYER);
	}
	public boolean isFromFiles() {
		return get().equalsIgnoreCase(FILES);
	}
}
