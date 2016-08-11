package com.diogenes.RBM;

import org.apache.giraph.graph.BasicComputation;
import org.apache.giraph.graph.Vertex;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;

import java.io.IOException;

/**
 * Created by Alexandr Shcherbakov on 13.04.2016.
 * This class is implementation of node which initialize visible layer.
 * CreateVisibleLayer - send messages for create visible layer.
 */
public class InitialNode extends BasicComputation<LongWritable, JSONWritable, DoubleWritable, JSONWritable> {

    @Override
    public void compute(Vertex<LongWritable, JSONWritable, DoubleWritable> vertex, Iterable<JSONWritable> iterable) throws IOException {

        CreateVisibleLayer();

        vertex.voteToHalt();
    }

    private void CreateVisibleLayer() throws IOException {
        MyWorkerContext context = getWorkerContext();
        int vSize = context.getVisibleLayerSize();
        JSONWritable emptyMessage = new JSONWritable("[]");

        /**
         * Create visible layer
         */
        for (int i = 1; i <= vSize; ++i) {
            sendMessage(new LongWritable(i), emptyMessage);
        }
    }
}
