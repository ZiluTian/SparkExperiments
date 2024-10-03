# -*- coding: utf-8 -*-
import json
import subprocess
import os
import sys
from datetime import datetime

# user input
MICRO_BENCHMARK = ""
UBER_JAR = "target/scala-2.12/SparkExperiments-assembly-1.0-SNAPSHOT.jar"

def run():
    for experiment in EXPERIMENTS:    
        for totalAgents in config[experiment]["totalAgents"]:
            core = abs(int(totalAgents / 1000))
            print(f"Running {experiment} cores {core} total agents {totalAgents}")
            cp = config[experiment]["classpath"]
            now = datetime.now()
            current_time = now.strftime("%H%M%S")
            if os.path.exists(f"{LOG_DIR}/{ITERATION}"):
                print(f"Directory {LOG_DIR}/{ITERATION} exists")
            else:
                os.makedirs(f"{LOG_DIR}/{ITERATION}")
                print(f"Make directory {LOG_DIR}/{ITERATION}")
            log_file = open(f"{LOG_DIR}/{ITERATION}/{experiment}Test.log_{MICRO_BENCHMARK}_cores{core}_{current_time}", 'a')
            process = subprocess.run([f'{SPARK_HOME}/bin/spark-submit', '--master', SPARK_MASTER, '--executor-cores', str(core), '--driver-memory', str(SPARK_DRIVER_MEM), '--executor-memory', str(SPARK_EXECUTOR_MEM), '--class', cp, UBER_JAR, str(totalAgents)], text=True, stdout=subprocess.PIPE, check=True)
            print(process.stdout, file=log_file)
            # os.system('echo 3 > /proc/sys/vm/drop_caches')
            log_file.flush()
            log_file.close()

if (__name__ == "__main__"):
    for i in range(1, len(sys.argv)):
        arg = sys.argv[i]
        if arg == '-t':
            MICRO_BENCHMARK = sys.argv[i+1]
    
    if (MICRO_BENCHMARK == "" or (not os.path.exists(f"conf/{MICRO_BENCHMARK}.json"))):
        print("The conf file for the micro-benchmark is not found")
        exit(1)

    f = open(f"conf/{MICRO_BENCHMARK}.json")
    config = json.load(f)

    EXPERIMENTS = config['experiments']
    LOG_DIR = config['log_dir']

    SPARK_HOME=config['spark']['home']
    SPARK_MASTER=config['spark']['master']
    SPARK_DRIVER_MEM=config['spark']['driver_mem']
    SPARK_EXECUTOR_MEM=config['spark']['executor_mem']

    if (not os.path.exists(LOG_DIR)):
        os.makedirs(f"{LOG_DIR}")

    # # Always clean the build before running experiments, which removes the uber jar
    # subprocess.run(['sbt', 'clean'])
    # # Generate the uber jar after 
    # subprocess.run(['sbt', '-DsparkDeploy=Cluster', 'assembly'], text=True, stdout=subprocess.PIPE, check=True)

    for i in [0, 1, 2]:
        ITERATION = i
        run()