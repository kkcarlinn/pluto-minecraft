/**
 * ProtocolLib - Bukkit server library that allows access to the Minecraft protocol. Copyright (C) 2012 Kristian S.
 * Stangeland
 * <p>
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.comphenix.protocol;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Represents the configuration of ProtocolLib.
 *
 * @author Kristian
 */
public class ProtocolConfig {

	private static final String LAST_UPDATE_FILE = "lastupdate";

	private static final String SECTION_GLOBAL = "global";
	private static final String SECTION_AUTOUPDATER = "auto updater";

	private static final String METRICS_ENABLED = "metrics";

	private static final String IGNORE_VERSION_CHECK = "ignore version check";

	private static final String DEBUG_MODE_ENABLED = "debug";
	private static final String DETAILED_ERROR = "detailed error";
	private static final String CHAT_WARNINGS = "chat warnings";

	private static final String SCRIPT_ENGINE_NAME = "script engine";
	private static final String SUPPRESSED_REPORTS = "suppressed reports";

	private static final String UPDATER_NOTIFY = "notify";
	private static final String UPDATER_DOWNLAD = "download";
	private static final String UPDATER_DELAY = "delay";

	// Defaults
	private static final long DEFAULT_UPDATER_DELAY = 43200;

	private Plugin plugin;
	private Configuration config;
	private boolean loadingSections;

	private ConfigurationSection global;
	private ConfigurationSection updater;

	// Last update time
	private long lastUpdateTime;
	private boolean configChanged;
	private boolean valuesChanged;

	// Modifications
	private int modCount;

	public ProtocolConfig(Plugin plugin) {

	}

	/**
	 * Reload configuration file.
	 */
	public void reloadConfig() {

	}

	/**
	 * Load the last update time stamp from the file system.
	 *
	 * @return Last update time stamp.
	 */
	private long loadLastUpdate() {
		return 0;
	}

	/**
	 * Store the given time stamp.
	 *
	 * @param value - time stamp to store.
	 */
	private void saveLastUpdate(long value) {

	}

	/**
	 * Retrieve the file that is used to store the update time stamp.
	 *
	 * @return File storing the update time stamp.
	 */
	private File getLastUpdateFile() {
		return new File(plugin.getDataFolder(), LAST_UPDATE_FILE);
	}

	/**
	 * Load data sections.
	 *
	 * @param copyDefaults - whether or not to copy configuration defaults.
	 */
	private void loadSections(boolean copyDefaults) {

	}

	/**
	 * Set a particular configuration key value pair.
	 *
	 * @param section - the configuration root.
	 * @param path - the path to the key.
	 * @param value - the value to set.
	 */
	private void setConfig(ConfigurationSection section, String path, Object value) {

	}

	@SuppressWarnings("unchecked")
	private <T> T getGlobalValue(String path, T def) {
		return def;
	}

	@SuppressWarnings("unchecked")
	private <T> T getUpdaterValue(String path, T def) {
		return def;
	}

	/**
	 * Retrieve a reference to the configuration file.
	 *
	 * @return Configuration file on disk.
	 */
	public File getFile() {
		return null;
	}

	/**
	 * Determine if detailed error reporting is enabled. Default FALSE.
	 *
	 * @return TRUE if it is enabled, FALSE otherwise.
	 */
	public boolean isDetailedErrorReporting() {
		return false;
	}

	/**
	 * Print warnings to players with protocol.info
	 * @return true if enabled, false if not
	 */
	public boolean isChatWarnings() {
		return false;
	}

	/**
	 * Retrieve whether or not ProtocolLib should determine if a new version has been released.
	 *
	 * @return TRUE if it should do this automatically, FALSE otherwise.
	 */
	public boolean isAutoNotify() {
		return false;
	}

	/**
	 * Retrieve whether or not ProtocolLib should automatically download the new version.
	 *
	 * @return TRUE if it should, FALSE otherwise.
	 */
	public boolean isAutoDownload() {
		return false;
	}

	/**
	 * Determine whether or not debug mode is enabled.
	 * <p>
	 * This grants access to the filter command.
	 *
	 * @return TRUE if it is, FALSE otherwise.
	 */
	public boolean isDebug() {
		return false;
	}

	/**
	 * Set whether or not debug mode is enabled.
	 *
	 * @param value - TRUE if it is enabled, FALSE otherwise.
	 */
	public void setDebug(boolean value) {

	}

	/**
	 * Retrieve an immutable list of every suppressed report type.
	 *
	 * @return Every suppressed report type.
	 */
	public ImmutableList<String> getSuppressedReports() {
		return ImmutableList.of();
	}

	/**
	 * Retrieve the amount of time to wait until checking for a new update.
	 *
	 * @return The amount of time to wait.
	 */
	public long getAutoDelay() {
		// Note that the delay must be greater than 59 seconds
		return DEFAULT_UPDATER_DELAY;
	}

	/**
	 * The version of Minecraft to ignore the built-in safety feature.
	 *
	 * @return The version to ignore ProtocolLib's satefy.
	 */
	public String getIgnoreVersionCheck() {
		return "";
	}

	/**
	 * Retrieve whether or not metrics is enabled.
	 *
	 * @return TRUE if metrics is enabled, FALSE otherwise.
	 */
	public boolean isMetricsEnabled() {
		return false;
	}

	/**
	 * Retrieve the last time we updated, in seconds since 1970.01.01 00:00.
	 *
	 * @return Last update time.
	 */
	public long getAutoLastTime() {
		return lastUpdateTime;
	}

	/**
	 * Set the last time we updated, in seconds since 1970.01.01 00:00.
	 * <p>
	 * Note that this is not considered to modify the configuration, so the modification count will not be incremented.
	 *
	 * @param lastTimeSeconds - new last update time.
	 */
	public void setAutoLastTime(long lastTimeSeconds) {

	}

	/**
	 * Retrieve the unique name of the script engine to use for filtering.
	 *
	 * @return Script engine to use.
	 */
	public String getScriptEngineName() {
		return "JavaScript";
	}

	/**
	 * Set the unique name of the script engine to use for filtering.
	 * <p>
	 * This setting will take effect next time ProtocolLib is started.
	 *
	 * @param name - name of the script engine to use.
	 */
	public void setScriptEngineName(String name) {

	}

	/**
	 * Retrieve the number of modifications made to this configuration.
	 *
	 * @return The number of modifications.
	 */
	public int getModificationCount() {
		return 0;
	}

	/**
	 * Save the current configuration file.
	 */
	public void saveAll() {

	}
}
