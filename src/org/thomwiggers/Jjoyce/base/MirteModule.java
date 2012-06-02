/**
 * @licence GNU General Public licence http://www.gnu.org/copyleft/gpl.html
 * @Copyright (C) 2012 Thom Wiggers
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.thomwiggers.Jjoyce.base;

import java.util.HashMap;

/**
 * @author Thom Wiggers
 *
 */
public class MirteModule {
    private HashMap<String,Object> settings;
    
    private HashMap<String, SettingsEvent> onSettingChanged;
    
    public MirteModule(HashMap <String, Object> settings){
	this.settings = settings;
    }
    
    /**
     * 
     */
    public MirteModule() {
	this.settings = new HashMap<String, Object>();
    }

    public Object getSetting(String key) {
	return settings.get(key);
    }
    
    
    public void changeSetting(String key, Object value) {
	if(onSettingChanged.containsKey(key))
	    onSettingChanged.get(key).call();
    }
}
