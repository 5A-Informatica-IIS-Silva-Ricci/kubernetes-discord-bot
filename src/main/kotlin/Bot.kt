import dev.minn.jda.ktx.injectKTX
import dev.minn.jda.ktx.listener
import environment.Impostazioni
import environment.PodInfo
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import java.net.InetAddress
import kotlin.system.exitProcess

fun main() {
    val podID = identificaIDPod()
    val podInfo = PodInfo(Impostazioni.numeroDiPod, Impostazioni.numeroDiShard, podID)

    println("Creo ${podInfo.shardsPerPod} shard per pod")


    val gestoreShard = DefaultShardManagerBuilder
        .create(GatewayIntent.GUILD_MESSAGES) // Permette di leggere i messaggi degli utenti
        .setToken(Impostazioni.discordToken) // Token per l'autenticazione su Discord
        .setShardsTotal(Impostazioni.numeroDiShard) // Imposto il numero di shard che lavoreranno su questo pod
        .setShards(podInfo.shardIDMinima, podInfo.shardIDMassima) // Setto la shard di partenza e di fine per questo pod
        .injectKTX()
        .build()

    println("Bot avviato (con shard dalla ${podInfo.shardIDMinima} alla ${podInfo.shardIDMassima})")

    // Registro il comando Discord per controllare il pod su cui sta lavorando il bot
    gestoreShard.shards.first().updateCommands().addCommands(
        CommandData("pod", "Visualizza l'ID del pod su cui sta lavorando il bot")
    ).queue()

    /*
    È un semplice ascoltatore, che ascolta per l'evento SlashCommandEvent,
     che permette di eseguire il codice all'interno delle parentesi quando un utente usa un comando del bot
     (l'unico comando del bot è quello registrato a riga 28, chiamato "pod".
     */
    gestoreShard.listener<SlashCommandEvent> {
        if (it.name == "pod") {
            // Se l'utente ha usato il comando pod rispondo con l'ID del pod su cui sta lavorando il bot
            it.reply("Sto lavorando sul pod con ID **${podID}**").queue()
            println("Un utente ha usato il comando pod")
        }
    }
}


// Identifica l'ID di un pod attraverso l'hostname del pod stesso
fun identificaIDPod(): Int = try {
    // Prendo l'hostname
    val hostName = InetAddress.getLocalHost().hostName
    println("[hostName] $hostName")

    // Prendo l'ultima parte e la converto in intero
    hostName.split("-").last().toInt()
} catch (t: Throwable) {
    println("Non riesco a trovare l'ID del pod dall'hostname\n$t")
    Thread.sleep(1000)
    exitProcess(404)
}
