#!/usr/bin/env bash
hadoop fs -rm -r /user/$USER/output/RBM/
hadoop jar target/RBM-1.0-SNAPSHOT-jar-with-dependencies.jar \
org.apache.giraph.GiraphRunner \
com.diogenes.RBM.InitialNode \
-mc com.diogenes.RBM.Master \
-vif com.diogenes.RBM.RBMInputFormat \
-vip /user/$USER/input/RBM.txt \
-vof org.apache.giraph.io.formats.AdjacencyListTextVertexOutputFormat \
-op /user/$USER/output/RBM/ \
-w 1 \
-ca mapred.job.tracker=localhost:50030 \
-ca com.diogenes.RBM.maxEpochNumber=1 \
-ca com.diogenes.RBM.maxBatchNumber=50 \
-ca com.diogenes.RBM.maxStepInSample=1 \
-ca com.diogenes.RBM.learningRate=0.7 \
-ca giraph.workerContextClass=com.diogenes.RBM.MyWorkerContext \
-ca com.diogenes.RBM.visibleLayerSize=784 \
-ca com.diogenes.RBM.hiddenLayerSize=100 \
-ca com.diogenes.RBM.inputPath=hdfs://localhost:8020/user/$USER/mnist/ \
-ca giraph.SplitMasterWorker=false \
-ca giraph.useSuperstepCounters=false
