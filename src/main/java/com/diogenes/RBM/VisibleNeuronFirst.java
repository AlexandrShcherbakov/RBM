package com.diogenes.RBM;

import org.apache.giraph.graph.*;
import org.apache.hadoop.io.*;
import java.io.*;
import java.lang.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Alexandr Shcherbakov on 16.04.2016.
 * This class is implementation of first stage of batch processing for neurons of visible layers.
 * ComputeActivateValuesAndPositivePartOfGradient - use input messages for computing activate value for each sample in
 * batch and positive part of gradient.
 * SendActivateValues - sends activate values to visible layer.
 */

public class VisibleNeuronFirst extends NeuronCompute {


	@Override
	public void compute(Vertex<LongWritable, JSONWritable, DoubleWritable> vertex,
		Iterable<JSONWritable> messages) throws IOException {
		/**
		 * Values of vertex
		 */
		double bias = vertex.getValue().getDouble("Bias");
		double[] activateValues = vertex.getValue().getArray("Value");
		HashMap<Long, Double> positiveGradient;

		ArrayList valueAndGrad = ComputeActivateValuesAndPositivePartOfGradient(vertex, messages, activateValues, bias);
		activateValues = (double[])valueAndGrad.get(0);
		positiveGradient = (HashMap<Long, Double>)valueAndGrad.get(1);


		SendActivateValues(vertex, activateValues);

		/**
		 * Update vertex value
		 */
		JSONWritable value = vertex.getValue();
		value.addArray("Value", activateValues);
		value.addHashMap("Positive gradient", positiveGradient);
		vertex.setValue(value);

		vertex.voteToHalt();
	}

	private ArrayList ComputeActivateValuesAndPositivePartOfGradient(Vertex<LongWritable, JSONWritable, DoubleWritable> vertex,
																	 Iterable<JSONWritable> messages, double[] oldActivateValues,
																	 double bias) {
		HashMap<Long, Double> positiveGradient = new HashMap<Long, Double>();
		double[] activateValues = new double[oldActivateValues.length];

		for (JSONWritable message: messages) {
			long sender = message.getLong("Sender");
			double edgeWeight = vertex.getEdgeValue(new LongWritable(message.getLong("Sender"))).get();
			double[] values = message.getArray("Value");

			smile.math.Math.axpy(edgeWeight, values, activateValues);

			double positiveGradAverage = smile.math.Math.dot(values, oldActivateValues);
			positiveGradient.put(sender, positiveGradAverage / oldActivateValues.length);
		}

		for (int i = 0; i < activateValues.length; ++i) {
			activateValues[i] = sigma(activateValues[i] + bias);
		}

		ArrayList result = new ArrayList();
		result.add(activateValues);
		result.add(positiveGradient);
		return result;
	}
}