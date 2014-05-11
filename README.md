fastcast-sample
===============

shows a simple distributed multicast based application: a KeyValue service.

Clients can put values in the servers hashmap and may choose to listen to changes happening in the key value service. Since fast-cast uses
multicast, there can be an arbitrary number of listening processes as no traffic is doubled.
