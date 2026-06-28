package me.iris.tiltified.events

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class TiltifyDonationEvent(
    val campaignId: String,
    val donorName: String,
    val amount: Double,
    val comment: String
) : Event() {

    override fun getHandlers(): HandlerList = HANDLERS

    companion object {
        @JvmStatic
        val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = HANDLERS
    }
}