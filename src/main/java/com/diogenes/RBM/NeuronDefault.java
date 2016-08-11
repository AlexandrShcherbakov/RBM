package com.diogenes.RBM;

import org.apache.giraph.graph.Vertex;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;

import java.io.IOException;

/**
 * Created by Alexandr Shcherbakov on 06.04.2016.
 * This class implements neurons which computes activate values and sends it to other layer.
 * ComputeActivateValues - computes activate values by messages
 * SendActivateValues - sends activate values to other layer
 */
public class NeuronDefault extends NeuronCompute {


    @Override
    public void compute(Vertex<LongWritable, JSONWritable, DoubleWritable> vertex,
                        Iterable<JSONWritable> messages) throws IOException {
        /**
         * Values of vertex
         */
        double bias = vertex.getValue().getDouble("Bias");
        double[] activateValues;

        activateValues = ComputeActivateValues(vertex, messages, bias);

        SendActivateValues(vertex, activateValues);

        /**
         * Update vertex value
         */
        JSONWritable value = vertex.getValue();
        value.addArray("Value", activateValues);
        vertex.setValue(value);

        vertex.voteToHalt();
    }
}
