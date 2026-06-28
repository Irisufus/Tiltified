package me.iris.tiltified

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import me.iris.tiltified.commands.CampaignCommand
import me.iris.tiltified.listeners.TiltifyDonationListener
import org.bukkit.plugin.java.JavaPlugin

class TiltifiedPlugin : JavaPlugin() {

    companion object {
        lateinit var instance: TiltifiedPlugin
            private set
        lateinit var defaultCampaign: TiltifyCampaign
            private set
        lateinit var tiltified: Tiltified
            private set
    }

    override fun onEnable() {
        instance = this
        saveDefaultConfig()

        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS, { commands ->
            run {
                commands.registrar().register("campaign", CampaignCommand())
            }
        })
        val campaignId = config.getString("campaign-id") ?: ""
        tiltified = Tiltified(this, config.getString("client-id") ?: "", config.getString("client-secret") ?: "")

        server.pluginManager.registerEvents(TiltifyDonationListener(), this)
        defaultCampaign = tiltified.addCampaign(campaignId) ?: return
        defaultCampaign.startRequests()
    }

    override fun onDisable() {
        tiltified.stop()
    }
}
