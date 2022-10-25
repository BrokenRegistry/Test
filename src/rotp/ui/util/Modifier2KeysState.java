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

import java.awt.event.InputEvent;

public enum Modifier2KeysState {
	NONE, SHIFT, CTRL, CTRL_SHIFT;

	private static Modifier2KeysState lastState = Modifier2KeysState.NONE;
	private static boolean isShiftDown;
	private static boolean isCtrlDown;

	public static boolean isShiftDown()	{ return isShiftDown; }
	public static boolean isCtrlDown()	{ return isCtrlDown; }
	public static Modifier2KeysState set(InputEvent e) {
		lastState = analyze(e);
		return lastState;
	}
	public static Modifier2KeysState get() {
		return lastState;
	}
	public static boolean checkForChange(InputEvent e) {
		Modifier2KeysState newState = analyze(e);
		if (newState == lastState)
			return false;
		lastState = newState;
		return true;
	}
	public static void reset() {
		lastState	= Modifier2KeysState.NONE;
		isShiftDown	= false;
		isCtrlDown	= false;
	}

	private static Modifier2KeysState analyze(InputEvent e) {
		if (e == null) {
			isShiftDown	= false;
			isCtrlDown	= false;
			return Modifier2KeysState.NONE;
		}
		isShiftDown	= e.isShiftDown();
		isCtrlDown	= e.isControlDown();
		if (isShiftDown)
			if (isCtrlDown)
				return Modifier2KeysState.CTRL_SHIFT;
			else
				return Modifier2KeysState.SHIFT;
		if (isCtrlDown)
			return Modifier2KeysState.CTRL;
		return Modifier2KeysState.NONE;
	}
}