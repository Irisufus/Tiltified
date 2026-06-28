package me.iris.tiltified

import me.iris.tiltified.listeners.TiltifyDonationListener
import org.bukkit.plugin.java.JavaPlugin
import java.net.http.HttpClient

class Tiltified : JavaPlugin() {

    companion object {
        lateinit var instance: Tiltified
            private set
        lateinit var httpClient: HttpClient
            private set
        lateinit var defaultCampaign: TiltifyCampaign
            private set
    }

    override fun onEnable() {
        instance = this

        saveDefaultConfig()
        val apiKey = config.getString("apiKey") ?: ""
        val campaignId = config.getString("campaignId") ?: ""

        server.pluginManager.registerEvents(TiltifyDonationListener(), this)

        httpClient = HttpClient.newHttpClient()
        defaultCampaign = TiltifyCampaign(apiKey, campaignId)
        defaultCampaign.startRequests()
    }

    override fun onDisable() {
        defaultCampaign.stopRequests()
    }
}
