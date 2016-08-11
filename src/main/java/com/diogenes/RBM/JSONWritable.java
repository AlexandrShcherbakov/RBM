package com.diogenes.RBM;

import org.apache.hadoop.io.*;
import java.util.*;
import java.io.*;
import com.google.gson.*;

/**
 * This is a class that stores a JSON object as a Writable.
 * It is used as a class for messages and for vertex values.
 * It is stored in Hadoop as Text.
 */

public class JSONWritable implements Writable {
	private JsonElement json;
	
	public JSONWritable() {
		json = new JsonObject();
	}

	public JSONWritable(JsonElement json) {
		this.json = json;
	}

	public JSONWritable(String jsonstr) throws IOException {
		JsonParser parser = new JsonParser();
		json = parser.parse(jsonstr);
	}

	public JsonElement get() {
		return json;
	}

	public void write(DataOutput out) throws IOException {
		Text text = new Text(json.toString());
		text.write(out);
	}

	public void readFields(DataInput in) throws IOException {
		Text text = new Text();
		text.readFields(in);
		JsonParser parser = new JsonParser();
		json = parser.parse(text.toString());
	}

	public int hashCode() {
		return json.hashCode();
	}

	public void set(JsonElement json) {
		this.json = json;
	}

	public String toString() {
		return json.toString();
	}

	/**
	 * Special functions for simpler usage.
	 * Here we consider JSON to be a dictionary with string keys. 
	 * These functions are used to add long, double, array of doubles, ArrayList of doubles or 
	 * HashMap of doubles to the dictionary and retrieve them back
	 */ 

	public double getDouble(String key) {
		return json.getAsJsonObject().get(key).getAsDouble();
	}

	public long getLong(String key) {
		return json.getAsJsonObject().get(key).getAsLong();
	}

	public ArrayList getArrayList(String key) {
		ArrayList a = new ArrayList();
		Iterator <JsonElement> it = json.getAsJsonObject().getAsJsonArray(key).iterator();
		while (it.hasNext()) {
			a.add(it.next().getAsDouble());
		}
		return a;
	}

	public double[] getArray(String key) {
		JsonArray arr = json.getAsJsonObject().getAsJsonArray(key);
		int len = arr.size();
		double[] a = new double[len];
		for (int i = 0; i < len; i++) {
			a[i] = arr.get(i).getAsDouble();
		}
		return a;
	}

	public HashMap getHashMap(String key) {
		HashMap a = new HashMap();
		Iterator it = json.getAsJsonObject().getAsJsonObject(key).entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry me = (Map.Entry) it.next();
			a.put(Long.valueOf(me.getKey().toString()), ((JsonElement) me.getValue()).getAsDouble());
		}
		return a;
	}

	public void addDouble(String key, double value) {
		json.getAsJsonObject().addProperty(key, value);
	}

	public void addLong(String key, long value) {
		json.getAsJsonObject().addProperty(key, value);
	}
	
	public void addArrayList(String key, ArrayList a) {
		Iterator <Double> it = a.iterator();
		JsonArray arr = new JsonArray();
		while (it.hasNext()) {
			arr.add(new JsonPrimitive(it.next()));
		}
		json.getAsJsonObject().add(key, arr);
	}

	public void addArray(String key, double[] a) {
		JsonArray arr = new JsonArray();
		for (int i = 0; i < a.length; i++) {
			arr.add(new JsonPrimitive(a[i]));
		}
		json.getAsJsonObject().add(key, arr);
	}

	public void addHashMap(String key, HashMap a) {
		Iterator it = a.entrySet().iterator();
		JsonObject obj = new JsonObject();
		while (it.hasNext()) {
			Map.Entry me = (Map.Entry) it.next();
			obj.addProperty(me.getKey().toString(), (double) me.getValue());
		}
		json.getAsJsonObject().add(key, obj);
	}
}
