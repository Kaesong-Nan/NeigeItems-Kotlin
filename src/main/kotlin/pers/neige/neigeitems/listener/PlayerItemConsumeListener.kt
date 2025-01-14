package pers.neige.neigeitems.listener

import bot.inker.bukkit.nbt.NbtCompound
import bot.inker.bukkit.nbt.NbtItemStack
import bot.inker.bukkit.nbt.NbtUtils
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.EquipmentSlot
import pers.neige.neigeitems.item.ItemDurability
import pers.neige.neigeitems.manager.ActionManager
import pers.neige.neigeitems.utils.ActionUtils.consumeAndReturn
import pers.neige.neigeitems.utils.ItemUtils.isNiItem
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent

object PlayerItemConsumeListener {
    @SubscribeEvent(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun listener(event: PlayerItemConsumeEvent) {
        // 获取玩家
        val player = event.player
        // 获取手持物品
        val itemStack = event.item
        // 获取NI物品信息(不是NI物品就停止操作)
        val itemInfo = itemStack.isNiItem(true) ?: return
        // NBT物品
        val nbtItemStack: NbtItemStack = itemInfo.nbtItemStack
        // 物品NBT
        val itemTag: NbtCompound = itemInfo.itemTag
        // NI物品数据
        val neigeItems: NbtCompound = itemInfo.neigeItems
        // NI物品id
        val id: String = itemInfo.id
        // NI节点数据
        val data: HashMap<String, String> = itemInfo.data!!

        // 检测东西在哪只手上
        val hand = if (itemStack.isSimilar(player.inventory.itemInMainHand)) {
            EquipmentSlot.HAND
        } else {
            EquipmentSlot.OFF_HAND
        }

        try {
            // 检测已损坏物品
            ItemDurability.consume(player, neigeItems, event)
            if (event.isCancelled) return
            // 执行物品动作
            ActionManager.eatListener(player, itemStack, itemInfo, event)
        } catch (error: Throwable) {
            error.printStackTrace()
        }

        // 他妈的。。。。。
        if (!NbtUtils.isCraftItemStack(itemStack)) {
            itemTag.saveTo(itemStack)
        }

        // 设置物品
        if (hand == EquipmentSlot.HAND) {
            player.inventory.setItemInMainHand(itemStack)
        } else {
            player.inventory.setItemInOffHand(itemStack)
        }
    }
}