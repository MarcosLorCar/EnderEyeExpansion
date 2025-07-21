package me.orange.enderEyeExpansion

import me.orange.enderEyeExpansion.listeners.*
import org.bukkit.plugin.java.JavaPlugin

class EnderEyeExpansionPlugin : JavaPlugin() {

    override fun onEnable() {
        val pluginManager = server.pluginManager
        pluginManager.registerEvents(BeaconListener(), this)
        pluginManager.registerEvents(EndPortalFrameListener(this), this)
        pluginManager.registerEvents(DarkEyeInvincibilityListener(), this)
        pluginManager.registerEvents(RaidWinningListener(this), this)
        pluginManager.registerEvents(ThrowListener(), this)
        pluginManager.registerEvents(WorldListener(), this)
        if (server.worlds.isNotEmpty()) {
            server.worlds.filter { it.environment == org.bukkit.World.Environment.NORMAL }.forEach {
                WorldListener.injectDatapack(it)
            }
        }
    }

    override fun onDisable() {
    }

    companion object {
        const val NAMESPACE = "ender_eye_expansion"
    }
}
