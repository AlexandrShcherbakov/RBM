package com.diogenes.RBM;

import org.apache.giraph.edge.Edge;
import org.apache.giraph.graph.BasicComputation;
import org.apache.giraph.graph.Vertex;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;

import java.util.Random;

/**
 * Created by Alexandr Shcherbakov on 06.04.2016.
 * This class implements functions which are used in several classes of neurons
 */
public abstract class NeuronCompute extends BasicComputation<LongWritable, JSONWritable, DoubleWritable, JSONWritable> {

    double sigma(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

    double[] ComputeActivateValues(Vertex<LongWritable, JSONWritable, DoubleWritable> vertex,
                               Iterable<JSONWritable> messages, double bias) {
        MyWorkerContext context = getWorkerContext();
        Random rand = new Random(vertex.getId().get());

        double[] activateValues = new double[0];
        for (JSONWritable message: messages) {
            double edgeWeight = vertex.getEdgeValue(new LongWritable(message.getLong("Sender"))).get();
            double[] values = message.getArray("Value");
            if (activateValues.length == 0) activateValues = new double[values.length];
            smile.math.Math.axpy(edgeWeight, values, activateValues);
        }

        for (int i = 0; i < activateValues.length; ++i) {
            activateValues[i] = sigma(activateValues[i] + bias);
            if (context.useSampling())
                activateValues[i] = (activateValues[i] > rand.nextDouble()) ? 1.0 : 0.0;
        }

        return activateValues;
    }

    void SendActivateValues(Vertex<LongWritable, JSONWritable, DoubleWritable> vertex, double[] activateValues) {
        JSONWritable message = new JSONWritable();
        message.addLong("Sender", vertex.getId().get());
        for (int i = 0; i < activateValues.length; ++i)
            activateValues[i] = Math.round(activateValues[i] * 100) / 100;
        message.addArray("Value", activateValues);
        sendMessageToAllEdges(vertex, message);
    }

    double ComputePositivePartOfBiasGradient(double[] activateValues) {
        double positiveBias =  smile.math.Math.sum(activateValues);
        return positiveBias / activateValues.length;
    }

    double UpdateBiases(double[] activateValues, double bias, double positiveBias) {
        MyWorkerContext context = getWorkerContext();
        double learningRate = context.getLearningRate();

        double biasGrad = -1.0 * smile.math.Math.sum(activateValues);
        biasGrad /= activateValues.length;
        biasGrad += positiveBias;

        return biasGrad * learningRate + bias;
    }

    void AddBias(Vertex<LongWritable, JSONWritable, DoubleWritable> vertex) {
        JSONWritable value = vertex.getValue();
        value.addDouble("Bias", 0.0);
        vertex.setValue(value);
    }

    void SendNewEdges(Vertex<LongWritable, JSONWritable, DoubleWritable> vertex) {
        JSONWritable message = new JSONWritable();
        message.addLong("Sender", vertex.getId().get());
        for (Edge<LongWritable, DoubleWritable> edge: vertex.getEdges()) {
            message.addDouble("Value", edge.getValue().get());
            sendMessage(edge.getTargetVertexId(), message);
        }
    }
}
