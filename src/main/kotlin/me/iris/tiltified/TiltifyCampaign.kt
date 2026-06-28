package me.iris.tiltified

import com.google.gson.JsonParser
import me.iris.tiltified.events.TiltifyDonationEvent
import org.bukkit.scheduler.BukkitTask
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class TiltifyCampaignRequest(val apiKey: String, val campaignId: String) {
    init {
        if (apiKey.isEmpty() || campaignId.isEmpty()) {
            throw IllegalArgumentException("apiKey or campaignId cannot be empty")
        }
        if (apiKey == "YOUR_TILTIFY_API_KEY" || campaignId == "YOUR_TILTIFY_CAMPAIGN_ID") {
            throw IllegalArgumentException("You forgot to change the config or this is the first launch!")
        }
    }

    private lateinit var requestTask: BukkitTask
    private val processedDonationIds = mutableSetOf<String>()

    fun startRequests() {
        requestTask = Tiltified.instance.server.scheduler.runTaskTimerAsynchronously(Tiltified.instance, Runnable
            {
                fetchDonations()
            },
            0L,
            Tiltified.instance.config.getInt("requestInterval").toLong()
        )
    }

    fun stopRequests() {
        requestTask.cancel()
    }

    fun fetchDonations() {
        try {
            val url = "https://v5api.tiltify.com/api/public/campaigns/$campaignId/donations?limit=10"
            val request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer $apiKey")
                .header("Accept", "application/json")
                .GET()
                .build()

            val response = Tiltified.httpClient.send(request, HttpResponse.BodyHandlers.ofString())

            if (response.statusCode() != 200) {
                Tiltified.instance.logger.warning("Failed to fetch donations. HTTP Status Code: ${response.statusCode()}")
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

                val donorName = if (donation.has("donor_name")) donation["donor_name"].asString else "Anonymous"
                val amount = donation["amount"].asJsonObject["value"].asDouble
                val comment = if (donation.has("comment")) donation["comment"].asString else ""

                Tiltified.instance.server.scheduler.runTask(Tiltified.instance, Runnable {
                    Tiltified.instance.server.pluginManager.callEvent(
                        TiltifyDonationEvent(donorName, amount, comment)
                    )
                })
            }

        } catch (e: Exception) {
            Tiltified.instance.logger.severe("Error fetching dontaions: ${e.message}")
        }
    }
}