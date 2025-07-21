package me.orange.enderEyeExpansion.listeners

import me.orange.enderEyeExpansion.EnderEyeExpansionPlugin
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.WorldLoadEvent
import java.io.File

class WorldListener : Listener {
    @EventHandler
    fun onWorldLoad(event: WorldLoadEvent) {
        val world = event.world
        if (world.environment != World.Environment.NORMAL) return
        val datapackFolder = File(world.worldFolder, "datapacks")
        if (!datapackFolder.exists()) datapackFolder.mkdirs()

        val datapackName = "EnderEyeExpansion_Datapack"
        val targetZip = File(datapackFolder, "$datapackName.zip")
        if (!targetZip.exists()) {
            // Copy from your plugin resources
            EnderEyeExpansionPlugin::class.java.getResourceAsStream("/$datapackName.zip").use { input ->
                targetZip.outputStream().use { output ->
                    input?.copyTo(output)
                }
            }
            Bukkit.getLogger().info("Injected datapack '$datapackName' into ${world.name}")
        }
    }
}