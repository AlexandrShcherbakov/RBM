package com.diogenes.RBM;

import org.apache.giraph.edge.EdgeFactory;
import org.apache.giraph.graph.Vertex;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;

import java.io.IOException;

/**
 * Created by Alexandr Shcherbakov on 16.04.2016.
 * This class is implementation of initialize stage for neurons of hidden layers.
 * CreateEdgesByMessages - iterates by input messages and set output edges.
 * AddBias - add zero bias to neuron.
 */
public class HiddenInitialize extends NeuronCompute {

    @Override
    public void compute(Vertex<LongWritable, JSONWritable, DoubleWritable> vertex, Iterable<JSONWritable> messages) throws IOException {

        CreateEdgesByMessages(vertex, messages);

        AddBias(vertex);

        vertex.voteToHalt();
    }


    //Functional blocks
    private void CreateEdgesByMessages(Vertex<LongWritable, JSONWritable, DoubleWritable> vertex,
                                      Iterable<JSONWritable> messages) {
        JSONWritable emptyMessage = new JSONWritable();

        for (JSONWritable message: messages) {
            vertex.addEdge(EdgeFactory.create(new LongWritable(message.getLong("Sender")), new DoubleWritable(message.getDouble("Value"))));
            sendMessage(new LongWritable(message.getLong("Sender")), emptyMessage);
        }
    }
}
