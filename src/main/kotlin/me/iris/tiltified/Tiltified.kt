package me.iris.tiltified

import com.google.gson.JsonParser
import org.bukkit.plugin.java.JavaPlugin
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class Tiltified(
    private val plugin: JavaPlugin,
    val clientId: String,
    val clientSecret: String
) {

    companion object {
        lateinit var plugin: JavaPlugin
        lateinit var instance: Tiltified
            private set
        lateinit var httpClient: HttpClient
            private set
    }

    init {
        Companion.plugin = plugin
        instance = this
    }

    val campaigns = mutableMapOf<String, TiltifyCampaign>()
    private var accessToken: String? = null
    private var tokenExpirationTime: Long = 0

    init {
        httpClient = HttpClient.newHttpClient()
        getAccessToken()
    }

    fun getAccessToken() : String? {
        if (accessToken != null && System.currentTimeMillis() < tokenExpirationTime) {
            return accessToken
        }

        try {
            val url = "https://v5api.tiltify.com/oauth/token"
            val jsonObject = com.google.gson.JsonObject().apply {
                addProperty("client_id", clientId)
                addProperty("client_secret", clientSecret)
                addProperty("grant_type", "client_credentials")
                addProperty("scope", "public")
            }
            val jsonPayload = jsonObject.toString()

            val request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build()

            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

            if (response.statusCode() == 200) {
                val json = JsonParser.parseString(response.body()).asJsonObject
                accessToken = json["access_token"].asString

                val expiresInSeconds = json["expires_in"].asLong
                tokenExpirationTime = System.currentTimeMillis() + ((expiresInSeconds - 60) * 1000)

                return accessToken
            } else {
                plugin.logger.warning("Failed to get access token! HTTP Status: ${response.statusCode()}")
                plugin.logger.warning("Response body: ${response.body()}")
            }
        } catch (e: Exception) {
            plugin.logger.severe("Error getting access token: ${e.message}")
        }
        return null
    }

    fun addCampaign(campaignId: String) : TiltifyCampaign? {
        if (campaigns.containsKey(campaignId)) return campaigns[campaignId]!!
        if (accessToken == null) {
            plugin.logger.severe("Could not fetch access token. Campaign '$campaignId' can not be added.")
            return null
        }
        val campaign = TiltifyCampaign(accessToken!!, campaignId)
        campaigns[campaignId] = campaign
        return campaign
    }

    fun removeCampaign(campaignId: String) {
        if (!campaigns.containsKey(campaignId)) return
        campaigns[campaignId]?.stopRequests()
        campaigns.remove(campaignId)
    }

    fun stop() {
        campaigns.forEach { (_, campaign) ->
            campaign.stopRequests()
        }
    }
}