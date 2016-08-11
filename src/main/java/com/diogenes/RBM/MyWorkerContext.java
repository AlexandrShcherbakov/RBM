package com.diogenes.RBM;

import org.apache.giraph.conf.*;
import org.apache.giraph.worker.DefaultWorkerContext;

/**
 * Created by Alexandr Shcherbakov on 06.04.2016.
 * This class contains parameters of network
 * Available parameters:
 *      Maximum number of epoch
 *      Maximum number of batches
 *      Maximum step for one stage
 *      Size of visible layer
 *      Size of hidden layer
 *      Learning rate
 */

public class MyWorkerContext extends DefaultWorkerContext {
    static int epochNumber;
    static int batchNumber;
    static int stepInSample;

    static Integer visibleLayerSize = 64;
    static Integer hiddenLayerSize = 15;

    static Integer maxEpochNumber = 1;
    static Integer maxBatchNumber = 1;
    static Integer maxStepInSample = 6;

    static Double learningRate = 1.0;
    static Boolean useSampling = true;

    static String inputPath;

    static IntConfOption maxEpochNumber_OPTION = new IntConfOption(Master.class.getPackage().getName() + ".maxEpochNumber", 1,
            "Maximum number of epoch");
    static IntConfOption maxBatchNumber_OPTION = new IntConfOption(Master.class.getPackage().getName() + ".maxBatchNumber", 1,
            "Maximum number of batches");
    static IntConfOption maxStepInSample_OPTION = new IntConfOption(Master.class.getPackage().getName() + ".maxStepInSample", 6,
            "Maximum number of steps for one batch");
    static FloatConfOption learningRate_OPTION= new FloatConfOption(MyWorkerContext.class.getPackage().getName() + ".learningRate",
            learningRate.floatValue(), "Learning rate for weight updates");
    static IntConfOption visibleLayerSize_OPTION = new IntConfOption(MyWorkerContext.class.getPackage().getName() + ".visibleLayerSize",
            visibleLayerSize, "Number of neurons in visible layer");
    static IntConfOption hiddenLayerSize_OPTION = new IntConfOption(MyWorkerContext.class.getPackage().getName() + ".hiddenLayerSize",
            hiddenLayerSize, "Number of neurons in hidden layer");
    static BooleanConfOption useSampling_OPTION = new BooleanConfOption(MyWorkerContext.class.getPackage().getName() + ".useSampling",
            useSampling, "On/Off sampling on hidden layer");
    static StrConfOption inputPath_OPTION = new StrConfOption(MyWorkerContext.class.getPackage().getName() + ".inputPath",
            "", "Path to input files");

    @Override
    public void preApplication() throws InstantiationException, IllegalAccessException {
        epochNumber = 0;
        batchNumber = 0;
        stepInSample = 0;

        ImmutableClassesGiraphConfiguration conf = getConf();
        learningRate = (double)learningRate_OPTION.get(conf);
        maxEpochNumber = maxEpochNumber_OPTION.get(conf);
        maxBatchNumber = maxBatchNumber_OPTION.get(conf);
        maxStepInSample = (maxStepInSample_OPTION.get(conf) + 1) * 2;
        visibleLayerSize = visibleLayerSize_OPTION.get(conf);
        hiddenLayerSize = hiddenLayerSize_OPTION.get(conf);
        useSampling = useSampling_OPTION.get(conf);
        inputPath = inputPath_OPTION.get(conf);
    }


    public Double getLearningRate() {
        return learningRate;
    }

    @Override
    public void postSuperstep() {
        if (getSuperstep() < 3) {//Init phase
            return;
        }

        stepInSample++;
        batchNumber += stepInSample / maxStepInSample;
        stepInSample = stepInSample % maxStepInSample;
        epochNumber += batchNumber / maxBatchNumber;
        batchNumber = batchNumber % maxBatchNumber;
    }

    public Integer getBatchNumber() {
        return batchNumber;
    }

    public Integer getVisibleLayerSize() { return visibleLayerSize; }

    public Integer getHiddenLayerSize() { return hiddenLayerSize; }

    public Boolean useSampling() { return useSampling && stepInSample % 2 == 1; }

    public String getInputPath() { return inputPath; }
}
