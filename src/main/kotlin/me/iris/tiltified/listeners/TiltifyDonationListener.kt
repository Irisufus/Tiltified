package me.iris.tiltified.listeners

import me.iris.tiltified.events.TiltifyDonationEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack

class TiltifyDonationListener : Listener {

    @EventHandler
    fun onTiltifyDonation(event: TiltifyDonationEvent) {
        Bukkit.broadcast(Component.text("[Tiltified] ", NamedTextColor.GREEN)
            .append(Component.text("${event.donorName} donated $${event.amount}", NamedTextColor.WHITE))
        )
        Bukkit.getOnlinePlayers().forEach { player ->
            player.inventory.addItem(ItemStack(Material.APPLE))
        }
    }
}