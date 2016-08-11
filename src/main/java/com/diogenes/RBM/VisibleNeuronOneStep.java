package com.diogenes.RBM;

import org.apache.giraph.edge.Edge;
import org.apache.giraph.graph.Vertex;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by Alexandr Shcherbakov on 11.05.2016.
 * This class implements VisibleNeuronFirst and VisibleNeuronLast on one step.
 */
public class VisibleNeuronOneStep extends NeuronCompute {

    @Override
    public void compute(Vertex<LongWritable, JSONWritable, DoubleWritable> vertex, Iterable<JSONWritable> messages) throws IOException {
        /**
         * Values of vertex
         */
        double bias = vertex.getValue().getDouble("Bias");
        double[] activateValues = vertex.getValue().getArray("Value");
        double positiveBias = vertex.getValue().getDouble("Positive bias");

        activateValues = ComputeGradientAndUpdateEdges(vertex, messages, activateValues, bias);

        bias = UpdateBiases(activateValues, bias, positiveBias);

        SendNewEdges(vertex);

        /**
         * Update vertex value
         */
        JSONWritable value = vertex.getValue();
        value.addArray("Value", activateValues);
        value.addDouble("Bias", bias);
        vertex.setValue(value);

        vertex.voteToHalt();
    }

    private double[] ComputeGradientAndUpdateEdges(Vertex<LongWritable, JSONWritable, DoubleWritable> vertex,
                                                                     Iterable<JSONWritable> messages, double[] oldActivateValues,
                                                                     double bias) {
        MyWorkerContext context = getWorkerContext();
        double learningRate = context.getLearningRate();

        HashMap<Long, Double> gradient = new HashMap<>();
        HashMap<Long, double[]> hiddenLayer = new HashMap<>();
        double[] activateValues = new double[oldActivateValues.length];

        for (JSONWritable message: messages) {
            long sender = message.getLong("Sender");
            double edgeWeight = vertex.getEdgeValue(new LongWritable(message.getLong("Sender"))).get();
            double[] values = message.getArray("Value");

            smile.math.Math.axpy(edgeWeight, values, activateValues);

            double positiveGradAverage = smile.math.Math.dot(values, oldActivateValues);
            gradient.put(sender, positiveGradAverage / oldActivateValues.length);

            hiddenLayer.put(sender, values);
        }

        for (int i = 0; i < activateValues.length; ++i) {
            activateValues[i] = sigma(activateValues[i] + bias);
        }

        for (long i: gradient.keySet()) {
            double[] hiddenValues = hiddenLayer.get(i);
            double negativeGradAverage = smile.math.Math.dot(hiddenValues, activateValues);
            gradient.put(i, gradient.get(i) - negativeGradAverage / activateValues.length);
        }

        for (Edge<LongWritable, DoubleWritable> edge: vertex.getEdges()) {
            vertex.setEdgeValue(edge.getTargetVertexId(), new DoubleWritable(edge.getValue().get() + learningRate * gradient.get(edge.getTargetVertexId().get())));
        }

        return activateValues;
    }
}
