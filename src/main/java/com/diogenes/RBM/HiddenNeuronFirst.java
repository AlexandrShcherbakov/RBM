package com.diogenes.RBM;

import org.apache.giraph.graph.Vertex;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;

import java.io.IOException;

/**
 * Created by Alexandr Shcherbakov on 16.04.2016.
 * This class is implementation of first stage of batch processing for neurons of hidden layers.
 * ComputeActivateValues - use input messages for computing activate value for each sample in batch.
 * ComputePositivePartOfBiasGradient - computes positive part of gradient for edges and biases.
 * SendActivateValues - sends activate values to visible layer.
 */
public class HiddenNeuronFirst extends NeuronCompute {

	@Override
	public void compute(Vertex<LongWritable, JSONWritable, DoubleWritable> vertex,
		Iterable<JSONWritable> messages) throws IOException {
		/**
		 * Values of vertex
		 */
		double bias = vertex.getValue().getDouble("Bias");
		double[] activateValues;
		double positiveBias;

		activateValues = ComputeActivateValues(vertex, messages, bias);

		positiveBias = ComputePositivePartOfBiasGradient(activateValues);

		SendActivateValues(vertex, activateValues);

		/**
		 * Update vertex value
		 */
		JSONWritable value = vertex.getValue();
		value.addDouble("Positive bias", positiveBias);
		value.addArray("Value", activateValues);
		vertex.setValue(value);

		vertex.voteToHalt();
	}
}