import io.github.cdimascio.dotenv.dotenv
import mu.KotlinLogging

private val logger = KotlinLogging.logger {  }

object Impostazioni {
    /*
    Dotenv permette di leggere i valori delle variabili contenute nel file .env
    Si usa il file .env perchè ci sono variabili che devono rimanere private, come il token di discord perchè serve per l'autenticazione,
    che non devono essere pubblicate su github ed accessebili a tutti, ma devono essere gestite in modo privato
     */
    private val dotenv = dotenv {
        this.filename =  ".env"
    }


    val discordToken = leggi("discord.token")
    val numeroDiPod = leggiIntero("numero.di.pod")
    val numeroDiShard = leggiIntero("numero.di.shard")


    // Legge una variabile dal file .env
    @Throws(NoSuchElementException::class)
    private fun leggi(path: String): String {
        // Per comodità in automatico formatto il testo in stampatello e rimpiazzo i punti con gli underscore
        val nomeVariabile = path.uppercase().replace(".", "_")
        // Leggo la variabile dal file, se essa è null e quindi non esiste lancio un'eccezione
        return dotenv[nomeVariabile] ?: run {
            logger.error("Non ho trovato la chiave $path nel file .env.")
            throw NoSuchElementException("Non ho trovato la chiave $path nel file .env")
        }
    }

    // Legge una variabile dal file .env e la converte in intero
    @Throws(NoSuchElementException::class)
    private fun leggiIntero(path: String): Int = try {
        leggi(path).toInt()
    } catch (e: NumberFormatException) {
        throw NoSuchElementException("Non ho trovato la chiave INTERA $path nel file .env")
    }
}
