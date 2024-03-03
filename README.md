# welcome to poc-cache

 **poc-cache** is a simple in memory distributed cache.

# Features

- A distributed and in memory cache !
- Keeps a copy of data on each node as backup and keeps all of them updated
- Ownership of key determined using consistent hashing. keys are assigned "parents" which are nodes
- Reads / Writes / Modifications are executed on the **parent** first and then are distributed using a tcp connection to the **guardian** nodes
- If the sample-client (invoked as a jmx) sends a request with a key which is a guardian to that key, it is internally forwarded to the parent node. The guardian will collect the response and send it back to the caller
- If a node is lost, as currently all nodes have eventually consistent data - data is not moved around - only the parent - key mappings are updated and sent over multicast to al surviving members.


## How to use
- Build the gradle file to generate the artifact in this zip **poc-cache\build\distributions** 
- after unzipping run the **poc-cache.(bat)** executable to start the cache on the members - ensure they are on the same network and can send and receive multicast messages
- use the SampleClient class to send and receive requests.


## Whats currently missing

The cache is currently in nascent stages and will following work before it becomes an **industrial** product.
- Newly joined members will need to get the existing copy of data from respective key parents
- A dedicated python , Java client and a rest interface written in either of the languages.
- Handle larger messages - currently fixed at 1024 bytes
- Devops : distribution using docker and deployment using Kubernetes
