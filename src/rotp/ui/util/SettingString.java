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

import rotp.model.game.DynamicOptions;

public class SettingString extends SettingBase<String> {
	
	// ========== constructors ==========
	//
	/**
	 * @param guiLangLabel  The label header
	 * @param nameLangLabel The nameLangLabel
	 * @param defaultvalue The default value
	 * @param costTrue 
	 * @param costFalse 
	 */
	public SettingString(String guiLangLabel, String nameLangLabel, String defaultValue) {
		super(guiLangLabel, nameLangLabel, defaultValue, false, false, true);
		hasNoCost(true);
	}
	// ===== Overriders =====
	//
	@Override public void setFromCfgValue(String cfgValue) {
		set(cfgValue);
	}
	@Override public void setOptions(DynamicOptions options) {
		if (!isSpacer())
			options.setString(labelId(), settingValue());
	}
	@Override public void setFromOptions(DynamicOptions options) {
		if (!isSpacer())
			set(options.getString(labelId(), defaultValue()));
	}
}
