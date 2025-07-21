package me.orange.enderEyeExpansion.listeners

import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.Material
import org.bukkit.event.Listener

class ThrowListener : Listener {
    @EventHandler
    fun onThrowEnderEye(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK) return
        if (event.item?.type != Material.ENDER_EYE) return
        val block = event.clickedBlock?.state?.type
        val item = event.item!!

        if (block == Material.END_PORTAL_FRAME) return

        val itemModelData = item.getData(DataComponentTypes.CUSTOM_MODEL_DATA)?.strings()?.getOrNull(0)
        if (itemModelData != null)
            event.isCancelled = true
    }
}