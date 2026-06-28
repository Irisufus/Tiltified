package me.iris.tiltified.commands

import io.papermc.paper.command.brigadier.BasicCommand
import io.papermc.paper.command.brigadier.CommandSourceStack
import me.iris.tiltified.TiltifiedPlugin

class CampaignCommand : BasicCommand {

    override fun execute(source: CommandSourceStack, args: Array<out String>) {
        val sender = source.sender

        if (args.isEmpty()) {
            sender.sendMessage("Usage: /campaign <add|remove|start|stop> <campaignId>")
            return
        }

        when (args[0].lowercase()) {
            "add" -> {
                if (args.size < 2) {
                    sender.sendMessage("Usage: /campaign add <campaignId>")
                    return
                }
                val campaignId = args[1]
                TiltifiedPlugin.tiltified.addCampaign(campaignId)
                sender.sendMessage("Campaign '$campaignId' added.")
            }
            "remove" -> {
                if (args.size < 2) {
                    sender.sendMessage("Usage: /campaign remove <campaignId>")
                    return
                }
                val campaignId = args[1]
                TiltifiedPlugin.tiltified.removeCampaign(campaignId)
                sender.sendMessage("Campaign '$campaignId' removed.")
            }
            "start" -> {
                if (args.size < 2) {
                    sender.sendMessage("Usage: /campaign start <campaignId>")
                    return
                }
                val campaignId = args[1]
                val campaign = TiltifiedPlugin.tiltified.campaigns[campaignId]
                if (campaign != null) {
                    campaign.startRequests()
                    sender.sendMessage("Campaign '$campaignId' started.")
                } else {
                    sender.sendMessage("Campaign '$campaignId' not found.")
                }
            }
            "stop" -> {
                if (args.size < 2) {
                    sender.sendMessage("Usage: /campaign stop <campaignId>")
                    return
                }
                val campaignId = args[1]
                val campaign = TiltifiedPlugin.tiltified.campaigns[campaignId]
                if (campaign != null) {
                    campaign.stopRequests()
                    sender.sendMessage("Campaign '$campaignId' stopped.")
                } else {
                    sender.sendMessage("Campaign '$campaignId' not found.")
                }
            }
            else -> {
                sender.sendMessage("Unknown subcommand. Usage: /campaign <add|remove|start|stop> <campaignId>")
            }
        }
    }

    override fun suggest(source: CommandSourceStack, args: Array<out String>): Collection<String> {
        return when (args.size) {
            1 -> listOf("add", "remove", "start", "stop").filter { it.startsWith(args[0], ignoreCase = true) }
            2 -> {
                val subCommand = args[0].lowercase()
                if (subCommand in listOf("remove", "start", "stop")) {
                    TiltifiedPlugin.tiltified.campaigns.keys.filter { it.startsWith(args[1], ignoreCase = true) }
                } else emptyList()
            }
            else -> emptyList()
        }
    }

    override fun permission(): String? {
        return "tiltified.command.campaign"
    }
}