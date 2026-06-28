package me.iris.tiltified

import com.google.gson.JsonParser
import me.iris.tiltified.events.TiltifyDonationEvent
import org.bukkit.scheduler.BukkitTask
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class TiltifyCampaign(val accessToken: String, val campaignId: String) {
    init {
        if (accessToken.isEmpty() || campaignId.isEmpty()) {
            throw IllegalArgumentException("accessToken or campaignId cannot be empty")
        }
        if (accessToken == "YOUR_TILTIFY_ACCESS_TOKEN" || campaignId == "YOUR_TILTIFY_CAMPAIGN_ID") {
            throw IllegalArgumentException("You forgot to change the config or this is the first launch!")
        }
    }

    private lateinit var requestTask: BukkitTask
    private val processedDonationIds = mutableSetOf<String>()

    fun startRequests() {
        requestTask = Tiltified.plugin.server.scheduler.runTaskTimerAsynchronously(Tiltified.plugin, Runnable
            {
                fetchDonations()
            },
            0L,
            Tiltified.plugin.config.getInt("request-interval").toLong()
        )
    }

    fun stopRequests() {
        requestTask.cancel()
    }

    private fun fetchDonations() {
        try {
            val url = "https://v5api.tiltify.com/api/public/campaigns/$campaignId/donations?limit=10"
            val request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer ${Tiltified.instance.getAccessToken()}")
                .header("Accept", "application/json")
                .GET()
                .build()

            val response = Tiltified.httpClient.send(request, HttpResponse.BodyHandlers.ofString())

            if (response.statusCode() != 200) {
                Tiltified.plugin.logger.warning("Failed to fetch donations. HTTP Status Code: ${response.statusCode()}")
                return
            }

            val jsonResponse = JsonParser.parseString(response.body()).asJsonObject
            val donations = jsonResponse.getAsJsonArray("data") ?: return

            for (element in donations) {
                val donation = element.asJsonObject
                val donationId = donation["id"].asString

                if (processedDonationIds.contains(donationId)) {
                    continue
                } else {
                    processedDonationIds.add(donationId)
                }

                val donorName = if (donation.has("donor_name")) donation["donor_name"].asString else "Unknown"
                val amount = donation["amount"].asJsonObject["value"].asDouble
                val comment = if (donation.has("comment")) donation["comment"].asString else ""

                Tiltified.plugin.server.scheduler.runTask(Tiltified.plugin, Runnable {
                    Tiltified.plugin.server.pluginManager.callEvent(
                        TiltifyDonationEvent(campaignId, donorName, amount, comment)
                    )
                })
            }

        } catch (e: Exception) {
            Tiltified.plugin.logger.severe("Error fetching dontaions: ${e.message}")
        }
    }
}