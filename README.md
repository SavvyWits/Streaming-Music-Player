# Streaming-Music-Player
A simple, robust streaming music application for Android.

The Media Player in this app is in a Service to enable smooth, robust handling of configuration changes, since the Service's lifecycle is independent of the Activity's. 

Many examples of using IBinder to get a Handler for interprocess communication exist, so an alternative approach of LocalBroadcastManager is used here. Deep down inside, LocalBroadcastManager uses a Handler, so this could be considered a cleaner coding example.
