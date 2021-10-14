/*
 * MIT License
 *
 * Copyright (c) 2021 Demeng Chen
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.demeng.rankgrantplus;

import dev.demeng.pluginbase.Common;
import dev.demeng.pluginbase.TaskUtils;
import dev.demeng.pluginbase.UpdateChecker;
import dev.demeng.pluginbase.UpdateChecker.Result;
import dev.demeng.pluginbase.YamlConfig;
import dev.demeng.pluginbase.chat.ChatUtils;
import dev.demeng.pluginbase.plugin.BasePlugin;
import java.io.IOException;
import java.security.Permission;
import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * The main class for RankGrant+.
 */
public final class RankGrantPlus extends BasePlugin {

  // Managers for the corresponding configuration file.
  @Getter private YamlConfig settingsFile;
  @Getter private YamlConfig messagesFile;
  @Getter private YamlConfig ranksFile;
  @Getter private YamlConfig dataFile;

  // Versions of the corresponding configuration file.
  private static final int SETTINGS_VERSION = 6;
  private static final int MESSAGES_VERSION = 7;
  private static final int RANKS_VERSION = 3;
  private static final int DATA_VERSION = 2;

  // Vault API permission hook.
  @Getter private Permission permissionHook;

  @Override
  public void enable() {

    final long startTime = System.currentTimeMillis();

    getLogger().info("Loading configuration files...");
    if (!loadFiles()) {
      return;
    }

    getLogger().info("Hooking into Vault...");
    if (!hookPermission()) {
      return;
    }

    getLogger().info("Loading metrics...");
    new Metrics(this, 3766);

    getLogger().info("Checking for updates...");
    checkUpdates();

    ChatUtils.console("&aRankGrant+ v" + Common.getVersion()
        + " by Demeng has been enabled in "
        + (System.currentTimeMillis() - startTime) + " ms.");

    ChatUtils.coloredConsole("&6Enjoying RG+? Check out GrantX! &ehttps://demeng.dev/grantx");
  }

  @Override
  public void disable() {
    ChatUtils.console("&cRankGrant+ v" + Common.getVersion() + " by Demeng has been disabled.");
  }

  /**
   * Loads all configuration files and performs a quick version check to make sure the file is not
   * outdated.
   *
   * @return true if successful, false otherwise
   */
  private boolean loadFiles() {

    // Name of the file that is currently being loading, used for the error message.
    String currentlyLoading = "configuration files";

    try {
      currentlyLoading = "settings.yml";
      settingsFile = new YamlConfig(currentlyLoading);

      if (settingsFile.isOutdated(SETTINGS_VERSION)) {
        Common.error(null, "Outdated settings.yml file.", true);
        return false;
      }

      currentlyLoading = "messages.yml";
      messagesFile = new YamlConfig(currentlyLoading);

      if (messagesFile.isOutdated(MESSAGES_VERSION)) {
        Common.error(null, "Outdated messages.yml file.", true);
        return false;
      }

      currentlyLoading = "ranks.yml";
      ranksFile = new YamlConfig(currentlyLoading);

      if (ranksFile.isOutdated(RANKS_VERSION)) {
        Common.error(null, "Outdated ranks.yml file.", true);
        return false;
      }

      currentlyLoading = "data.yml";
      dataFile = new YamlConfig(currentlyLoading);

      if (dataFile.isOutdated(DATA_VERSION)) {
        Common.error(null, "Outdated data.yml file.", true);
        return false;
      }

    } catch (IOException | InvalidConfigurationException ex) {
      Common.error(ex, "Failed to load " + currentlyLoading + ".", true);
      return false;
    }

    return true;
  }

  /**
   * Hooks into a permission plugin using Vault.
   *
   * @return true if successful, false otherwise
   */
  private boolean hookPermission() {

    final RegisteredServiceProvider<Permission> provider =
        Bukkit.getServer().getServicesManager().getRegistration(Permission.class);

    if (provider == null) {
      Common.error(null, "Failed to hook into Vault and/or a permission plugin.", true);
      return false;
    }

    permissionHook = provider.getProvider();
    return true;
  }

  private void checkUpdates() {
    TaskUtils.runAsync(task -> {
      final UpdateChecker checker = new UpdateChecker(63403);

      if (checker.getResult() == Result.OUTDATED) {
        ChatUtils.coloredConsole(
            "&2" + ChatUtils.CONSOLE_LINE,
            "&aA newer version of RankGrant+ is available!",
            "&aCurrent version: &r" + Common.getVersion(),
            "&aLatest version: &r" + checker.getLatestVersion(),
            "&aGet the update: &rhttps://spigotmc.org/resources/63403",
            "&2" + ChatUtils.CONSOLE_LINE);
        return;
      }

      if (checker.getResult() == Result.ERROR) {
        getLogger().warning("Failed to check for updates.");
      }
    });
  }

  public FileConfiguration getSettings() {
    return settingsFile.getConfig();
  }

  public FileConfiguration getMessages() {
    return messagesFile.getConfig();
  }

  public FileConfiguration getRanks() {
    return ranksFile.getConfig();
  }

  public FileConfiguration getData() {
    return dataFile.getConfig();
  }
}