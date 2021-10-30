// Classe per le informazioni riguardanti il pod su cui lavora l'istanza del bot
// È necessario dividere le shard sui diversi pod e per farlo si assegna un numero preciso di shard ad ogni singolo bot
class PodInfo(numeroPods: Int, numeroShards: Int, podID: Int) {
    // Numero di shard per singolo pod
    var shardsPerPod: Int = numeroShards / numeroPods
    // Shard di partenza del pod corrente
    var shardIDMinima: Int = podID * shardsPerPod
    // Ultima shard del pod corrente
    var shardIDMassima: Int = podID * shardsPerPod + shardsPerPod - 1
}
