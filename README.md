fastcast-sample
===============

shows a simple distributed multicast based application: a KeyValue service.

Clients can put key and values and/or listen to changes happening in the key value service. Since fast-cast uses
multicast, there can be an arbitrary number of listeners as no traffic is doubled.
