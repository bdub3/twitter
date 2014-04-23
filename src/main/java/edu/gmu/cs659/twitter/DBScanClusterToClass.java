package edu.gmu.cs659.twitter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.clusterers.DBSCAN;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.converters.ArffSaver;

public class DBScanClusterToClass {

	private static final Logger logger = LoggerFactory
			.getLogger(DBScanClusterToClass.class);

	public static void main(String [] args) {
		Instances instances = readFile("stream_1398031191504.csv_idf.arff");
		Instances copy = readFile("stream_1398031191504.csv_idf.arff");
	
		try {
			runDBScan(instances, copy);
		} catch (Exception e) {
			logger.warn("whatever: ", e);
		}
	}
	
	protected static Instances readFile(String file) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			Instances data = new Instances(reader);
			reader.close();
			return data;
		} catch (FileNotFoundException e) {
			logger.warn("Failed to find file: {}", file, e);
		} catch (IOException e) {
			logger.warn("IOException reading file", e);
		}
		return null;
	}

	protected static void runDBScan(Instances instances, Instances copy) throws Exception {
		DBSCAN dbScan = new DBSCAN();
		dbScan.setOptions(Utils
				.splitOptions("weka.clusterers.DBSCAN -E 0.9 -M 3 "
						+ "-I weka.clusterers.forOPTICSAndDBScan.Databases.SequentialDatabase "
						+ "-D weka.clusterers.forOPTICSAndDBScan.DataObjects.EuclideanDataObject"));

		logger.debug("filtering for DBScan");
		

		/*
		ClusterMembership filter = new ClusterMembership();
		filter.setInputFormat(instances);
		filter.setIgnoredAttributeIndices("1-18");
		EM em = new EM();
		em.setOptions(Utils.splitOptions("-I 100 -N 2 -M 1.0E-6 -S 100"));
		filter.setDensityBasedClusterer(em);
		Instances filteredInstances = Filter.useFilter(instances, filter);
		 */

		// above filter is hanging, we will just delete attributes from a copy
		for(int i = 0; i < 18;i++) {
			copy.deleteAttributeAt(0);
		}
		Instances filteredInstances = copy;
		

		logger.debug("Building clusterer");
		dbScan.buildClusterer(filteredInstances);

		List<String> clusters = new ArrayList<String>();
		// 0-indexing the trends even though 1-indexing them when using... 
		// because the setValue doesn't seem to want to work for the 1st trend value
		for(int i = 0; i <= dbScan.numberOfClusters(); i++) {
			clusters.add("trend_" + i);
		}
		Attribute attr = new Attribute("trend", clusters);

		instances.insertAttributeAt(attr, 0);
		logger.debug("Labeling instances");

		List<Integer> deleteList = new LinkedList<Integer>();
		
		for (int i = 0; i < filteredInstances.size(); i++) {
			Instance instance = instances.instance(i);
			Instance filteredInstance = filteredInstances.instance(i);
			int cluster;
			try {
				cluster = dbScan.clusterInstance(filteredInstance);
				instance.setValue(0, "trend_" + (cluster + 1));
				logger.debug("Labeled instance {} with trend_{}", i, (cluster + 1));
			} catch (Exception e) {
				// tracking elements to remove in reverse order, then can remove them in this order by index;
				deleteList.add(0, i);
			}
		}
		
		logger.debug("Removing bad instances");
		for(Integer i : deleteList) {
			instances.delete(i);
		}

		logger.debug("Saving arff");
		ArffSaver saver = new ArffSaver();
		saver.setInstances(instances);
		saver.setFile(new File("clustered.arff"));
		saver.writeBatch();
	}
}
