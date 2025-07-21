package me.orange.enderEyeExpansion.listeners

import me.orange.enderEyeExpansion.EnderEyeExpansionPlugin
import org.bukkit.*
import org.bukkit.FireworkEffect.Type
import org.bukkit.Raid.RaidStatus
import org.bukkit.entity.Firework
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.raid.RaidFinishEvent
import org.bukkit.loot.LootContext
import org.bukkit.loot.LootTable
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask
import java.util.*

class RaidWinningListener(
    private val plugin: Plugin
) : Listener {

    val lootTable: LootTable by lazy {
        Bukkit.getLootTable(
            NamespacedKey(EnderEyeExpansionPlugin.Companion.NAMESPACE, "custom_eye_hero")
        ) ?: error("Datapack not loaded")
    }


    @EventHandler
    fun onWinning(event: RaidFinishEvent) {
        val raid = event.raid
        if (raid.status != RaidStatus.VICTORY) return
        if (raid.badOmenLevel != 5) return

        val dropLocation = raid.location.toHighestLocation()
        val lootContext = LootContext.Builder(dropLocation).build()
        val loot = lootTable.populateLoot(Random(),  lootContext)
        val droppedItem = event.world.dropItemNaturally(dropLocation, loot.first())
        droppedItem.isPersistent = true

        var task: BukkitTask? = null

        val reminder = Runnable {
            if (!droppedItem.isValid) {
                task?.cancel()
                return@Runnable
            }
            fireworkEffect(droppedItem.location)
        }

        @Suppress("AssignedValueIsNeverRead")
        task = Bukkit.getScheduler().runTaskTimer(plugin, reminder, 0L, 100L)
    }

    private fun fireworkEffect(dropLocation: Location) {
        val firework = dropLocation.world.spawn(dropLocation, Firework::class.java)

        val meta = firework.fireworkMeta

        val effect = FireworkEffect.builder()
            .flicker(true)
            .withColor(Color.YELLOW)
            .withFade(Color.GREEN)
            .with(Type.BALL_LARGE)
            .trail(true)
            .build()

        meta.addEffect(effect)
        meta.power = 6

        firework.fireworkMeta = meta

        firework.detonate()
    }
}