# Kubernetes Discord Bot
## Cos'è Discord?
[Discord](https://discord.com) è un'applicazione, molto simile a whatsapp, che viene utilizzata principalmente per messaggistica.  
Come whatsapp vi è una divisione in gruppi, ossia un utente Discord può essere presente in più gruppi.
#### Perchè abbiamo usato Discord?
Abbiamo scelto Discord per questo progetto perchè esso mette a disposizione delle API che rendono facile e veloce la creazione di bot.  
I bot sono sostanzialmente degli utenti "automatici", ossia possono svolgere delle funzioni predefinite dal programmatore, per esempio rispondere con "ciao" ogni volta che ricevono un messaggio (esempio banale).  
Discord permette ai bot di registrare inoltre i propri "comandi" così che siano facilmente accessibili agli utenti, come sarà mostrato in seguito.  
#### Cosa sono le shard
È importante capire anche cosa siano le shard.  
Essendo che il nostro bot può essere presente in più gruppi di Discord (comparabili a gruppi di whatsapp), Discord permette di separare il nostro bot su diverse shard.  
Una shard è una divisione logica del carico di lavoro del bot, ad esempio se il nostro bot è presente in 50 gruppi Discord, attraverso le shard possiamo dire a Discord di mandare tutti gli eventi che accadono nei primi 10 gruppi alla shard numero 1 del nostro bot, nei secondi 20 gruppi alla shard numero 2, e così via in modo da poter dividere il carico di richieste logicamente su più "processi", in questo caso chiamati "shard".  
Questo è fondamentale per il deployment su kubernetes perchè essendo che il nostro bot lavorerà in un cluster kubernetes con più pods (unità di lavoro di kubernetes) non potremo avere più istanze dello stesso bot uguali su tutti i pod ma dovremo dividere le varie richieste provenienti dai vari gruppi Discord in modo tale che solo un'istanza riceva la richiesta. Altrimenti se tutte le istanze ricevessero la stessa richiesta, ad esempio di rispondere con "ciao" al messaggio, allora se avessimo 4 pod che lavorano il nostro bot risponderà 4 volte con "ciao" invece che una sola. Mentre utilizzando le shard possiamo fare come segue: creiamo ad esempio 4 shard e le dividiamo per i vari pod, quindi il primo pod riceverà tutte le richieste indirizzate alla prima shard (che gestisce un `n` numero di gruppi), il secondo pod riceverà le richieste indirizzate alla seconda shard (che gestisce un altro numero di gruppi **diversi**), e così via in modo da poter separare le richieste sui vari pod. Questa divisione va ovviamente implementata a livello di codice e sarà più chiaro in seguito.

## Come abbiamo creato il bot di Discord
Per prima cosa siamo andati sul [Discord Developer Portal](https://discord.com/developers/applications) ed abbiamo creato una nuova "applicazione" in cui abbiamo poi abilitato l'opzione "bot".  
Discord ci ha quindi forniti di un _token_ che servirà per autenticarsi nell'account del bot, come una sorta di login solo che invece di username e password ci ha fornito un token d'autenticazione che potremo usare nel nostro codice.  
Ora che abbiamo registrato questo bot su Discord dobbiamo creare il codice che deciderà effettivamente quello che farà il bot.  

## Il codice
Come linguaggio abbiamo utilizzato Kotlin, creando un progetto kotlin con gradle per gestire le dipendenze.  
Questo perchè esiste una libreria fatta in Java (e quindi compatibile con kotlin) per interfacciarsi con le API di Discord: la libreria è [JDA](https://github.com/DV8FromTheWorld/JDA).  
Questa libreria rende molto facile la comunicazione con le API di Discord, riducendo il nostro codice a poche righe rispetto a quante ne servirebbero per gestire noi tutte le richieste http da fare alle API.  
Ecco spiegato a cosa servono tutti i file del nostro progetto:

### build.gradle.kts
Contiene le informazioni più importanti del progetto, ossia contiente:  
- I plugin, in questo caso kotlin e "shadow-jar", quest'ultimo è un plugin che permette di creare un file .jar del nostro progetto che potremo successivamente eseguire attraverso java. È importante perchè shadow-jar oltre a creare una jar del nostro codice sorgente include in esso anche tutte le dipendenze descritte in seguito in questo file.  
- `application.mainClass.set("BotKt")` che serve alla jar per capire dove si trova il metodo `main` per eseguire successivamente la jar, in questo caso nel file `Bot.kt` (essendo il file un file kotlin esso verrà compilato e convertito in codice java per questo si chiamerà poi BotKt.java invece che rimanere Bot.kt). 
- Le repositories, ossia i luoghi da cui andare a prendere e scaricare le dipendenze descritte in seguito
- Le dependencies (dipendenze), cioè tutte quelle librerie aggiuntive che servono al nostro progetto per funzionare, in questo caso JDA, JDA-ktx (che aggiunge dei metodi di utilità a JDA per l'utilizzo con Kotlin) e dotenv, che permette la lettura delle variabili presente nel file .env

### .env
`.env` è un file che contiene le variabili "d'ambiente" e "segrete". Sono quelle variabili che non devono essere rese pubbliche per questioni di sicurezza, come ad esempio il `token` che ci ha fornito Discord per autenticarci nell'account del bot crreato in precedenza.  

### src/
Questa cartella contiene effettivamente il nostro codice sorgente, al cui interno troviamo tre file
### Impostazioni.kt
È il file che si occupa di leggere le variabili d'ambiente contenute nel file `.env`, utilizzando la libreria dotenv descritta precedentemente.  
Questo file serve solo per leggere le tre variabili del file `.env` quali:
- Il token di Discord per l'autenticazione
- Il numero di shard che utilizzerà il bot
- Il numero di pod che utilizzerà il bot

### Bot.kt
Contiene il metodo `main` e sarà il file da cui viene avviato il nostro bot.  
Avvengono diverse operazioni in questo file e sono: 
- Identificazione del pod su cui sta lavorando il bot. Essendo il bot distribuito su un cluster kubernetes avremo diverse istanze di esso eseguite su diversi pod (unità di lavoro del cluster) e per capire quali shard dovrà gestire questa istanza allora dovremo identificare il pod su cui sta lavorando. Questo lo possiamo fare attraverso l'hostname in quanto i pod avranno un hostname incrementale pod-0, pod-1, pod-2 e così via (è spiegato in seguito come abbiamo fatto ad avere questo hostname incrementale). Quindi deve sostanzialmente leggere l'hostname della macchina per identificare il pod su cui sta lavorando.  
- Viene poi inizializzata la classe `PodInfo` che calcola quali shard dovrà gestire il pod con un semplice calcolo: per capire quante shard dovrà gestire il nostro pod basterà fare `numero di shard / numero di pod` (entrambe sono variabili lette dal file `.env`). Oltre definire quante shard deve gestire il pod dobbiamo anche dire quali, quindi andiamo a calcolare la prima e l'ultima shard che dovrà gestire questa istanza con altri due calcoli, il primo `prima shard da gestire = podID * numero di shard per POD` e `ultima shard da gestire = prima shard da gestire + numero di shard per pod - 1`, quindi supponendo che ci siano 2 shard per pod, il primo pod gestirà le shard dalla `0` (`0 * 2`) alla `1` (`0 + 2 - 1`), il secondo pod dalla `2` (`1 * 2`) alla `3` (`2 + 2 - 1`), etc...
- Dopo vi è la vera e propria interazione con Discord, viene creato un gestore delle shard a cui forniamo il nostro `token` d'autenticazione, il numero di shard da gestire e quali shard gestire.
- Inviamo poi a Discord i comandi disponibili agli utenti per il nostro bot, nel nostro caso solo uno chiamato "pod" che permetterà all'utente di visualizzare l'ID del pod su cui sta lavorando il bot in quel preciso gruppo Discord su cui è stato eseguito il comando.
- Viene poi definito un ascoltatore che esegue del codice specifico ogni volta che un utente utilizza il nostro comando, all'interno dell'ascoltatore si può vedere che controlliamo se il comando si chiama `pod` ed in caso rispondiamo all'utente con il numero del pod su cui sta lavorando il bot.

Questo è tutto il codice necessario.

## Preparazione al deployment su kubernetes
Siccome kubernetes gestisce applicazioni conteneirizzate dobbiamo implementare docker per il nostro bot.  
Per farlo ci è bastato creare il file `Dockerfile` che permette di creare un immagine docker del nostro progetto.  
Le operazioni effettuate sono divise in due fasi nel file e sono: 
- Utilizzo openjdk-16 per creare la jar, prima copio tutti i file del progetto, poi entro in modalità root, rendo il file `gradlew` eseguibile e con esso eseguo il comando gradle `shadow-jar` che andrà a creare la jar del nostro progetto.  
- Utilizzo sempre openjdk-16 per stavolta avviare la jar, copiando la jar creata nello step precedente nella directory /opt/helpdesk e copiando anche il file `.env`, poi avvio la jar con il comando `java -jar {file.jar}`

Ora è necessario pubblicare l'immagine docker del nostro progetto per poterla poi utilizzare in kubernetes, e per questo abbiamo utilizzato docker hub, una sorta di github però per le immagini docker, è sostanzialmente un sito dove tu come utente puoi pubblicare le tue immagini docker.  
Quindi abbiamo installato Docker sul nostro pc, ci siamo autenticati con il nostro account e successivamente abbiamo usato i comandi:
- `docker build -t giuliopime/k8s-bot:1.0.0 .` dove con il tag `-t` abbiamo indicato il nome della nostra immagine e la versione (`1.0.0`) e con `.` abbiamo indicato dove si trova il file `Dockerfile`, in questo caso nella cartella corrente del progetto, il comando andrà quindi eseguito sulla riga di comando all'interno della cartella del progetto.  
- `docker push giuliopime/k8s-bot:1.0.0` per pubblicare l'immagine su docker hub
L'immagine docker del bot è quindi ora presente [qui](https://hub.docker.com/repository/docker/giuliopime/k8s-bot).


## Creazione del cluster kubernetes
### Creazione del progetto su GCloud
Abbiamo optato per google cloud computing come provider del cluster.  
Abbiamo quindi creato un nuovo progetto su [GCloud](https://console.cloud.google.com/home/).  
### Creazione del cluster
Poi abbiamo installato sul nostro pc il programma a riga di comando (CLI) `gcloud`, ci siamo autenticati con il comando `gcloud auth login` ed abbiamo creato il nostro cluster, con il comando:
```bash
gcloud container clusters create gke-cluster
        --project=kubernetes-test-329821 # ID del progetto
        --zone=us-west1 # La zona dove si trova il nostro cluster (costa di meno in america)
        --num-nodes=2 # Definiamo il numero di nodi del cluster
```
Siamo poi andati sulla [dashboard](https://console.cloud.google.com/marketplace/product/google/container.googleapis.com?returnUrl=%2Fkubernetes%3FreturnUrl%3D%252Fcompute%252Finstances%253Fproject%253Dside-projects-298709%2526instancessize%253D50%2526pli%253D1%26project%3Dside-projects-298709&project=side-projects-298709) di google cloud computing per modificare la potenza dei nodi delle nostre macchine, siccome a noi non servono macchine potenti in quanto il carico di lavoro per ogni nodo è poco.

### Deployment attraverso kubectl
Abbiamo installato sul nostro pc `kubectl`, un tool che permette la gestione dei cluster kubernetesd attraverso riga di comando, seguendo [questa guida](https://cloud.google.com/kubernetes-engine/docs/how-to/cluster-access-for-kubectl).  

Successivamente abbiamo creato il deployment con il comando kubectl
```bash
kubectl create deployment discord-bot --image giuliopime/k8s-bot:1.0.0
```

Per definire come deve essere strutturato il nostro cluster c'è bisogno di un file chiamato `deployment.yml`, che definisce appunto la struttura del nostro cluster.  
Abbiamo quindi creato questo file al cui interno viene detto che:
- Il cluster è di tipo `StatefulSet`, ovvero ogni pod avrà un hostname incrementale e kubernetes si occuperà di mantenere integri tutti i pod con il loro giusto hostname
- Viene definito il numero di repliche, ovvero di pod, che utilizzerà il cluster, nel nostro caso `2`
- Viene definita l'immagine docker da eseguire all'interno dei pod, nel nostro caso ci basterà indicare il nome dell'immagine pubblicata su docker hub (`giuliopime/k8s-bot:1.0.0`)

(Informazioni sullo [StatefulSet](https://kubernetes.io/docs/concepts/workloads/controllers/statefulset/))  

Dopo aver creato il file `deployment.yml` abbiamo applicato la configurazione al nostro cluster con il comando di kubectl:
```bash
kubectl apply -f deployment.yml
```

Ed abbiamo poi visualizzato lo stato del deployment con 

```bash
kubectl rollout status deployment/discord-bot
```

Possiamo anche vedere i logs dei vari pod con
```bash
kubectl logs pod/bot-0 # Per il primo pod ad esempio
```

Oppure i pod esistenti con
```bash
kubectl get pods
```

##Risultato
