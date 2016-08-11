package com.diogenes.RBM;

import com.google.common.collect.Lists;
import org.apache.giraph.edge.Edge;
import org.apache.giraph.edge.EdgeFactory;
import org.apache.giraph.graph.Vertex;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.apache.giraph.io.formats.*;

import java.io.IOException;
import java.util.List;

/**
  * This is a VertexInputFormat for a graph with long vertex IDs, 
  * double edge values and vertex values and messages of type
  * JSONWritable, given in JSON format, like this:
  * [ID, Vertex_value, [[Destination_ID, Edge_value], ...]]
  * This is a modified version of JsonLongDoubleFloatDoubleVertexInputFormat
  */
public class RBMInputFormat extends
  TextVertexInputFormat<LongWritable, JSONWritable, DoubleWritable> {

  @Override
  public TextVertexReader createVertexReader(InputSplit split,
      TaskAttemptContext context) {
    return new RBMVertexReader();
  }

  class RBMVertexReader extends
    TextVertexReaderFromEachLineProcessedHandlingExceptions<JSONArray,
    JSONException> {

    @Override
    protected JSONArray preprocessLine(Text line) throws JSONException {
      return new JSONArray(line.toString());
    }

    @Override
    protected LongWritable getId(JSONArray jsonVertex) throws JSONException,
              IOException {
      return new LongWritable(jsonVertex.getLong(0));
    }

    @Override
    protected JSONWritable getValue(JSONArray jsonVertex) throws
      JSONException, IOException {
      return new JSONWritable(jsonVertex.getString(1));
    }

    @Override
    protected Iterable<Edge<LongWritable, DoubleWritable>> getEdges(
        JSONArray jsonVertex) throws JSONException, IOException {
      JSONArray jsonEdgeArray = jsonVertex.getJSONArray(2);
      List<Edge<LongWritable, DoubleWritable>> edges =
          Lists.newArrayListWithCapacity(jsonEdgeArray.length());
      for (int i = 0; i < jsonEdgeArray.length(); ++i) {
        JSONArray jsonEdge = jsonEdgeArray.getJSONArray(i);
        edges.add(EdgeFactory.create(new LongWritable(jsonEdge.getLong(0)),
            new DoubleWritable(jsonEdge.getDouble(1))));
      }
      return edges;
    }

    @Override
    protected Vertex<LongWritable, JSONWritable,
              DoubleWritable> handleException(Text line, JSONArray jsonVertex,
                  JSONException e) {
      throw new IllegalArgumentException(
          "Couldn't get vertex from line " + line, e);
    }

  }
}
