class PodInfo(numeroPods: Int, numeroShards: Int, podID: Int) {
    var shardsPerPod: Int = numeroShards / numeroPods
    var shardIDMinima: Int = podID * shardsPerPod
    var shardIDMassima: Int = podID * shardsPerPod + shardsPerPod - 1
}
