package me.orange.enderEyeExpansion.listeners

import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Orientation
import org.bukkit.block.data.type.EndPortalFrame
import org.bukkit.block.data.type.Jigsaw
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.Vector

private const val META_KEY = "CustomEye"
val eyeList = mapOf(
    "jungle" to Orientation.UP_NORTH,
    "gold" to Orientation.UP_SOUTH,
    "dark" to Orientation.UP_EAST,
    "resin" to Orientation.UP_WEST,
    "elder" to Orientation.DOWN_NORTH,
    "warden" to Orientation.DOWN_SOUTH,
    "guardian" to Orientation.DOWN_EAST,
    "heavy" to Orientation.DOWN_WEST,
    "ancient" to Orientation.NORTH_UP,
    "greed" to Orientation.SOUTH_UP,
    "wealth" to Orientation.EAST_UP,
    "hero" to Orientation.WEST_UP
)

class EndPortalFrameListener(
    val plugin: JavaPlugin,
) : Listener {

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        if (event.clickedBlock?.state?.type != Material.END_PORTAL_FRAME) return
        print("CLICK")
        if (event.item?.type != Material.ENDER_EYE) return

        val item = event.item!!
        val block = event.clickedBlock!!
        val blockData = block.blockData as EndPortalFrame

        if (blockData.hasEye()) return

        val itemModelData = item.getData(DataComponentTypes.CUSTOM_MODEL_DATA)?.strings()?.getOrNull(0)

        val isCustomEye = itemModelData != null && eyeList.keys.contains(itemModelData)

        if (!isCustomEye) {
            errorFeedback(block)

            event.isCancelled = true
            return
        }

        val eyeType = itemModelData

        val direction = blockData.facing.direction
        val perpA = Vector(direction.z, direction.y, -direction.x)
        val perpB = Vector(-direction.z, direction.y, direction.x)

        val blockLocation = block.location
        val center = blockLocation.clone().add(direction.multiply(2))

        val world = block.world
        val blockA = world.getBlockAt(blockLocation.clone().add(perpA))
        if (blockA.blockData.material == Material.END_PORTAL_FRAME) center.add(perpA)
        val blockB = world.getBlockAt(blockLocation.clone().add(perpB))
        if (blockB.blockData.material == Material.END_PORTAL_FRAME) center.add(perpB)

        var placedCount = 0
        var alreadyPlaced = false
        var errorBlock: Block? = null

        iterateEndPortalFrames(center) { location ->
            val frame = location.block

            val blockDisplay = getNearbyDisplay(frame)

            if (blockDisplay.isEmpty()) return@iterateEndPortalFrames
            val thisEyeType = eyeList.filterValues { it == (blockDisplay[0].block as Jigsaw).orientation }.keys.firstOrNull() ?: return@iterateEndPortalFrames

            placedCount++

            if (eyeType == thisEyeType) {
                alreadyPlaced = true

                errorBlock = frame
            }
        }

        if (alreadyPlaced) {
            errorFeedback(block)
            alreadyPlacedFeedback(errorBlock!!)

            event.isCancelled = true
            return
        }

        // Summoning the block display with corresponding texture
        val eyeEntity = world.spawnEntity(blockLocation, EntityType.BLOCK_DISPLAY) as BlockDisplay
        val displayState = Bukkit.createBlockData(Material.JIGSAW) as Jigsaw
        displayState.orientation = eyeList[itemModelData]!!
        eyeEntity.block = displayState

        when(blockData.facing) {
            BlockFace.NORTH -> {
                eyeEntity.teleport(blockLocation.clone().add(1.0, 0.0, 1.0))
                eyeEntity.setRotation(180f, 0f)
            }
            BlockFace.EAST -> {
                eyeEntity.teleport(blockLocation.clone().add(0.0, 0.0, 1.0))
                eyeEntity.setRotation(-90f, 0f)
            }
            BlockFace.WEST -> {
                eyeEntity.teleport(blockLocation.clone().add(1.0, 0.0, 0.0))
                eyeEntity.setRotation(90f, 0f)
            }
            else -> {}
        }
    }

    @EventHandler
    fun onBreak(event: BlockBreakEvent) {
        val block = event.block
        if (block.type != Material.END_PORTAL_FRAME) return

        val blockDisplay = getNearbyDisplay(block)

        if (blockDisplay.isEmpty()) return

        blockDisplay.forEach {
            it.remove()
        }

        block.removeMetadata(META_KEY, plugin)
    }

    private fun getNearbyDisplay(block: Block): List<BlockDisplay> {
        if (block.blockData !is EndPortalFrame) return emptyList()
        val blockData = block.blockData as EndPortalFrame
        val location = block.location

        val newLocation = applyDirection(blockData, location)

        val radius = 0.1
        val nearby = newLocation.world.getNearbyEntities(newLocation, radius, radius, radius)

        val blockDisplay = nearby
            .filterIsInstance<BlockDisplay>()
        return blockDisplay
    }

    private fun applyDirection(blockData: EndPortalFrame, location: Location): Location {
        return when (blockData.facing) {
            BlockFace.NORTH -> {
                location.add(1.0, 0.0, 1.0)
            }

            BlockFace.EAST -> {
                location.add(0.0, 0.0, 1.0)
            }

            BlockFace.WEST -> {
                location.add(1.0, 0.0, 0.0)
            }

            else -> location
        }
    }

    private fun alreadyPlacedFeedback(block: Block) {
        block.world.spawnParticle(
            Particle.HAPPY_VILLAGER,
            block.location.toCenterLocation().add(0.0, 0.5, 0.0),
            10,
            0.2, 0.2, 0.2
        )
    }

    private fun errorFeedback(block: Block) {
        block.world.spawnParticle(
            Particle.ANGRY_VILLAGER,
            block.location.add(0.5, 0.2, 0.5),
            1
        )
        block.world.playSound(
            block.location,
            Sound.BLOCK_CANDLE_EXTINGUISH,
            SoundCategory.BLOCKS,
            1.0f,
            0.9f
        )
    }

    fun iterateEndPortalFrames(center: Location, action: (Location) -> Unit) {
        val offsets = listOf(
            Vector(-1, 0, -2), Vector( 0, 0, -2), Vector( 1, 0, -2),
            Vector(-2, 0, -1),                               Vector( 2, 0, -1),
            Vector(-2, 0,  0),                               Vector( 2, 0,  0),
            Vector(-2, 0,  1),                               Vector( 2, 0,  1),
            Vector(-1, 0,  2), Vector( 0, 0,  2), Vector( 1, 0,  2)
        )

        for (offset in offsets) {
            val frameLocation = center.clone().add(offset)
            action(frameLocation)
        }
    }
}