# welcome to poc-cache

 **poc-cache** is a simple in memory distributed cache.

# Features

- A distributed and in memory cache !
- Keeps a copy of data on each node as backup and keeps the data updated.
- Ownership of key determined using consistent hashing. Keys are assigned "parents" which are nodes in the cluster.
- Reads / Writes / Modifications are executed on the **parent** first and distributed using a tcp connection to the **guardian** nodes.
- If the client (SampleClient provided here as a class exposed to jmx) sends a request with a key which is a guardian to the node, request is internally forwarded to the parent node. The guardian will collect the response and send it back to the client.
- If a node is lost, key mappings are updated and multicast for all surviving members.


## How to use
- Build the gradle file to generate the artifact in this zip **poc-cache\build\distributions** 
- Run the **poc-cache.(bat)** to start the cache on the members - ensure they are on the same network and can send and receive multicast messages.
- Use the SampleClient class to send and receive requests.


## Whats currently missing

The cache is nascent and following work being done to make it **industrial**.
- Newly added nodes to get the existing copy of data from respective key parents.
- Dedicated python and Java clients.
- Support for much larger messages - currently fixed at 1024 bytes.
- Devops : distribution using docker and deployment using Kubernetes.
