package com.diogenes.RBM;

import org.apache.giraph.graph.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by Alexandr Shcherbakov on 16.04.2016.
 * This class is implementation of input stage of batch processing for neurons of visible layers.
 * ReadBatch - read batch from files in hdfs.
 * ComputePositivePartOfBiasGradient - computes positive part of gradient for edges and biases.
 * SendActivateValues - sends activate values to visible layer.
 */
public class VisibleNeuronZero extends NeuronCompute {

	@Override
	public void compute(Vertex<LongWritable, JSONWritable, DoubleWritable> vertex,
		Iterable<JSONWritable> messages) throws IOException {
		/**
		 * Values of vertex
		 */
		double[] activateValues;
		double positiveBias;

        activateValues = ReadBatch(vertex);

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

	//Functional blocks
	private double[] ReadBatch(Vertex<LongWritable, JSONWritable, DoubleWritable> vertex) {
		Configuration conf = new Configuration();
		conf.addResource(new Path("/etc/hadoop/conf/hdfs-site.xml"));
		conf.addResource(new Path("/etc/hadoop/conf/core-site.xml"));
		MyWorkerContext context = getWorkerContext();
		try{
			FileSystem dfs = FileSystem.get(conf);
			Path pt = new Path(context.getInputPath() + (vertex.getId().get() - 1) + "/" + context.getBatchNumber() + ".txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(dfs.open(pt)));
			String line;
			line = br.readLine();
            ArrayList<Double> activateValues = new ArrayList<>();
            for (String s: line.substring(1, line.length() - 1).split(",")) {
                activateValues.add(Double.parseDouble(s));
            }

            double[] activateArray = new double[activateValues.size()];
            for (int i = 0; i < activateValues.size(); ++i)
                activateArray[i] = activateValues.get(i);
            return activateArray;
		} catch(IOException e) {
			vertex.voteToHalt();
		}
        return new double[0];
    }
}