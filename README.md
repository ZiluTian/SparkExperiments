This project contains the source code for reproducing the experimental results of Spark in our SIGMOD paper 
*"Generalizing Bulk-Synchronous Parallel Processing: From Data to Threads and Agent-Based Simulations"*.

### Workloads
The workloads contain applications selected from areas where simulations are prevalent: population dynamics (gameOfLife), economics (stockMarket), and epidemics. 
The source code for implementing each of these simulations in  as a vertex program can be found in `/src/main/scala/simulations/`.

### Benchmark
Our paper included the following benchmark experiments in Spark: tuning, scaleup, scaleout, communication frequency, and computation interval. For each of the benchmark, you can find its corresponding configuration in `conf/`, which is necessary for launching a benchmark. The input graphs are not included here due to their size, but you should be able to easily generate them based on our description in the paper.

The driver script for starting a benchmark is `/bin/bench.py`. Prior to running a benchmark, you need to compile and assemble the vertex programs. Our benchmark script automates this for you by passing `-a`. In short, to compile and assemble the vertex program and then running a benchmark named {test}, you should enter the following command (tested with python3.9, but you can easily adjust the driver script to use other versions of Python):

```python3 bin/bench.py -t test```.

As a reference, you can checkout our measured performance in `benchmark/`.

### Remark
Before running a benchmark, you should already have the Spark cluster up and running. Our experiments were done using Spark 3.3 on CentOS 7. We used a cluster of servers, each with 24 cores (two Xeon processors, 48 hardware threads, supporting hyper-threading), 256GB of RAM, and 400GB of SSD. To obtain the best performance, you need to tune the configuration of Spark in `conf/spark-defaults.conf` (this is defined in Spark, *not* part of our benchmark configuration). 

