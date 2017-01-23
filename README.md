Streaming Cassandra to a Browser
================================

This is an experimental project to try out streaming from Cassandra to a browser. 

The cassandra code was taken from the Phantom examples: https://github.com/outworkers/phantom/tree/develop/phantom-example/src

The Scala-JS examples were taken from this example: https://github.com/jrudolph/akka-http-scala-js-websocket-chat

The goal is to stream data from a DB query to the browser, with some kind of manual backpressure. This allows retrieving data from the database without the hassle of paging the results. 

To allow the developer control over the number of records returned, a "manual" backpressure mechanism is used. The developer can signal the number of records desired, which is the demand. To implement this, a custom akka-streams graph stage is used with 2 inputs and one output. The inputs are the data stream, and a stream of demand messages, and the output is the requested number of data elements. 
