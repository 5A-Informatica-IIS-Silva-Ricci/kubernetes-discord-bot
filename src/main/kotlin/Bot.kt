import dev.minn.jda.ktx.listener
import mu.KotlinLogging
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import java.net.InetAddress
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger {  }

fun main() {
    val podID = identificaIDPod()
    val podInfo = PodInfo(Impostazioni.numeroDiPod, Impostazioni.numeroDiShard, podID)

    val shardsManager = DefaultShardManagerBuilder
        .create(GatewayIntent.GUILD_MESSAGES)
        .setShardsTotal(Impostazioni.numeroDiShard)
        .setShards(podInfo.shardIDMinima, podInfo.shardIDMassima)
        .setToken(Impostazioni.discordToken)
        .setActivity(Activity.watching("con kubernetes.."))
        .build()

    logger.info("Bot avviato (con shard dalla ${podInfo.shardIDMinima} alla ${podInfo.shardIDMassima})")

    shardsManager.shards.first().updateCommands().addCommands(
        CommandData("pod", "Visualizza l'ID del pod su cui sta lavorando il bot")
    )

    shardsManager.listener<SlashCommandEvent> {
        if (it.name == "pod")
            it.reply("Sto lavorando sul pod con ID **${podID}**")
    }
}


fun identificaIDPod(): Int = try {
    val hostName = InetAddress.getLocalHost().hostName
    logger.info("[hostName] $hostName")

    hostName.split("-").last().toInt()
} catch (t: Throwable) {
    logger.warn("Non riesco a trovare l'ID del pod dall'hostname", t)
    Thread.sleep(1000)
    exitProcess(404)
}
