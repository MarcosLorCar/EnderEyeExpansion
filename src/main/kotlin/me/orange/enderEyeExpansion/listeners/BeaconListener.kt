package me.orange.enderEyeExpansion.listeners

import io.papermc.paper.event.block.BeaconActivatedEvent
import me.orange.enderEyeExpansion.EnderEyeExpansionPlugin
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.advancement.Advancement
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.loot.LootContext
import org.bukkit.loot.LootTable
import org.bukkit.util.Vector
import java.util.*

class BeaconListener : Listener {
    val advancement: Advancement by lazy {
        Bukkit.getAdvancement(
            NamespacedKey(EnderEyeExpansionPlugin.Companion.NAMESPACE, "eyes/greed")
        ) ?: error("Datapack not loaded")
    }
    val lootTable: LootTable by lazy {
        Bukkit.getLootTable(
            NamespacedKey(EnderEyeExpansionPlugin.Companion.NAMESPACE, "custom_eye_greed")
        ) ?: error("Datapack not loaded")
    }

    @EventHandler
    fun onBeaconActivate(event: BeaconActivatedEvent) {
        val beacon = event.beacon
        if (beacon.tier != 4) return

        val entirelyGold = isFullBeaconPyramidOfType(event.block.location, Material.GOLD_BLOCK)

        if (!entirelyGold) return


        val players = beacon.entitiesInRange.filterIsInstance<Player>()



        if (players.any { it.getAdvancementProgress(advancement).isDone }) return

        val dropLocation = beacon.location.clone().add(0.0, -1.0, 0.0)
        val context = LootContext.Builder(dropLocation).build()
        val loot = lootTable.populateLoot(Random(), context)
        val droppedItem = beacon.world.dropItem(dropLocation.toCenterLocation(), loot.first())
        droppedItem.velocity = Vector(0, 0, 0)
        droppedItem.isPersistent = true

        iterateFullBeaconPyramid(event.block.location) { block, _ ->
            val data = block.blockData.clone()
            block.type = Material.AIR
            block.world.spawnParticle(Particle.BLOCK, block.location.add(0.5, 0.5, 0.5), 5, 0.2, 0.2, 0.2, data)
        }
    }

    fun iterateFullBeaconPyramid(
        beaconLocation: Location,
        maxTier: Int = 4,
        action: (block: Block, tier: Int) -> Unit
    ) {
        val world = beaconLocation.world ?: return

        for (tier in 1..maxTier) {
            for (dx in -tier..tier) {
                for (dz in -tier..tier) {
                    val x = beaconLocation.blockX + dx
                    val y = beaconLocation.blockY - tier
                    val z = beaconLocation.blockZ + dz

                    val block = world.getBlockAt(x, y, z)
                    action(block, tier)
                }
            }
        }
    }

    fun isFullBeaconPyramidOfType(beaconLocation: Location, blockType: Material): Boolean {
        var valid = true

        iterateFullBeaconPyramid(beaconLocation) { block, _ ->
            if (block.type != blockType) {
                valid = false
            }
        }

        return valid
    }
}