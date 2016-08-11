package com.diogenes.RBM;

import org.apache.giraph.edge.Edge;
import org.apache.giraph.graph.*;
import org.apache.hadoop.io.*;
import java.io.*;
import java.lang.*;
import java.util.HashMap;

/**
 * Created by Alexandr Shcherbakov on 16.04.2016.
 * This class is implementation of last stage of batch processing for neurons of visible layers.
 * ComputeGradientAndUpdateEdges - computes gradient for bias and edges and update it with gradient.
 * UpdateBiases - set new value of bias in neuron.
 * SendNewEdges - sends new values of edges to hidden layer.
 */
public class VisibleNeuronLast extends NeuronCompute {

	@Override
	public void compute(Vertex<LongWritable, JSONWritable, DoubleWritable> vertex,
		Iterable<JSONWritable> messages) throws IOException {

        /**
         * Values of vertex
         */
        double bias = vertex.getValue().getDouble("Bias");
        double[] activateValues = vertex.getValue().getArray("Value");
        double positiveBias = vertex.getValue().getDouble("Positive bias");

        activateValues = ComputeGradientAndUpdateEdges(vertex, messages, activateValues.length, bias);

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

	//Functional blocks
	private double[] ComputeGradientAndUpdateEdges(Vertex<LongWritable, JSONWritable, DoubleWritable> vertex,
												 Iterable<JSONWritable> messages, int batchSize, double bias) {
		MyWorkerContext context = getWorkerContext();
		double learningRate = context.getLearningRate();

		HashMap<Long, Double> gradient = vertex.getValue().getHashMap("Positive gradient");
		HashMap<Long, double[]> hiddenLayer = new HashMap<>();

		// Compute activate value
		double[] activateValues = new double[batchSize];
		for (JSONWritable message: messages) {
			long sender = message.getLong("Sender");
			double edgeWeight = vertex.getEdgeValue(new LongWritable(message.getLong("Sender"))).get();
			double[] values = message.getArray("Value");
            smile.math.Math.axpy(edgeWeight, values, activateValues);
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