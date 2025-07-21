package me.orange.enderEyeExpansion.listeners

import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.entity.EntityType
import org.bukkit.entity.Item
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent

class DarkEyeInvincibilityListener : Listener {
    @EventHandler
    fun onItemExplode(event: EntityDamageEvent) {
        if (event.entityType != EntityType.ITEM) return
        if ((event.entity as Item).itemStack.getData(DataComponentTypes.CUSTOM_MODEL_DATA)?.strings()[0] != "dark") return
        event.entity.isInvulnerable = true
        event.isCancelled = true
    }
}