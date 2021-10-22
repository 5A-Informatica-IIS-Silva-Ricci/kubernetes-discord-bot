import io.github.cdimascio.dotenv.dotenv
import mu.KotlinLogging

private val logger = KotlinLogging.logger {  }

object Impostazioni {
    private val dotenv = dotenv {
        this.filename =  ".env"
    }


    val discordToken = leggi("discord.token")
    val numeroDiPod = leggiIntero("numero.di.pod")
    val numeroDiShard = leggiIntero("numero.di.shard")


    @Throws(NoSuchElementException::class)
    private fun leggi(path: String): String {
        val value = dotenv[path.uppercase().replace(".", "_")]

        if (value == null) {
            logger.error("Non ho trovato la chiave $path nel file .env.")
            throw NoSuchElementException("Non ho trovato la chiave $path nel file .env")
        }

        return value
    }

    @Throws(NoSuchElementException::class)
    private fun leggiIntero(path: String): Int = try {
        leggi(path).toInt()
    } catch (e: NumberFormatException) {
        throw NoSuchElementException("Non ho trovato la chiave INTERA $path nel file .env")
    }
}
