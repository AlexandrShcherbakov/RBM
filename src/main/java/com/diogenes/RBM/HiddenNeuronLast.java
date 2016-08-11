package com.diogenes.RBM;

import org.apache.giraph.graph.*;
import org.apache.hadoop.io.*;
import java.io.*;

/**
 * Created by Alexandr Shcherbakov on 16.04.2016.
 * This class is implementation of last stage of batch processing for neurons of hidden layers.
 * UpdateEdgesByMessages - iterates by messages and set new values of edges.
 * UpdateBiases - set new value of bias in neuron.
 */
public class HiddenNeuronLast extends NeuronCompute {

	@Override
	public void compute(Vertex<LongWritable, JSONWritable, DoubleWritable> vertex,
		Iterable<JSONWritable> messages) throws IOException {
		/**
		 * Values of vertex
		 */
		double bias = vertex.getValue().getDouble("Bias");
		double[] activateValues = vertex.getValue().getArray("Value");
		double positiveBias = vertex.getValue().getDouble("Positive bias");

		UpdateEdgesByMessages(vertex, messages);

		bias = UpdateBiases(activateValues, bias, positiveBias);

        /**
         * Update vertex value
         */
        JSONWritable value = vertex.getValue();
        value.addDouble("Bias", bias);
        vertex.setValue(value);

        vertex.voteToHalt();
	}

	//Functional blocks
	private void UpdateEdgesByMessages(Vertex<LongWritable, JSONWritable, DoubleWritable> vertex,
										 Iterable<JSONWritable> messages) {
		JSONWritable emptyMessage = new JSONWritable();

		for (JSONWritable message: messages) {
			vertex.setEdgeValue(new LongWritable(message.getLong("Sender")), new DoubleWritable(message.getDouble("Value")));
			sendMessage(new LongWritable(message.getLong("Sender")), emptyMessage);
		}
	}
}