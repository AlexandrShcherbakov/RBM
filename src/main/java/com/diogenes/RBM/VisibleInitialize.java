package com.diogenes.RBM;

import org.apache.giraph.edge.EdgeFactory;
import org.apache.giraph.graph.BasicComputation;
import org.apache.giraph.graph.Vertex;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;

import java.io.IOException;
import java.util.Random;

/**
 * Created by Alexandr Shcherbakov on 16.04.2016.
 * This class is implementation of initialize stage for neurons of visible layers.
 * CreateAndSendEdges - create edges with random weight.
 * AddBias - add zero bias to neuron.
 */
public class VisibleInitialize extends NeuronCompute {

    @Override
    public void compute(Vertex<LongWritable, JSONWritable, DoubleWritable> vertex, Iterable<JSONWritable> messages) throws IOException {

        CreateAndSendEdges(vertex);

        AddBias(vertex);

        vertex.voteToHalt();
    }

    private void CreateAndSendEdges(Vertex<LongWritable, JSONWritable, DoubleWritable> vertex) {
        MyWorkerContext context = getWorkerContext();
        int hSize = context.getHiddenLayerSize();
        Random rand = new Random(vertex.getId().get());

        for (int i = -1; -i <= hSize; --i) {
            JSONWritable message = new JSONWritable();
            message.addLong("Sender", vertex.getId().get());
            message.addDouble("Value", rand.nextGaussian());
            sendMessage(new LongWritable(i), message);
            vertex.addEdge(EdgeFactory.create(new LongWritable(i), new DoubleWritable(message.getDouble("Value"))));
        }
    }
}
