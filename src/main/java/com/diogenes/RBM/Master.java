package com.diogenes.RBM;

import org.apache.giraph.conf.ImmutableClassesGiraphConfiguration;
import org.apache.giraph.master.*;
import org.apache.giraph.conf.IntConfOption;

/**
 * Created by Alexandr Shcherbakov.
 * This class is implementation of master compute.
 * First superstep: InitialNode runs visible layer
 * Second superstep: VisibleInitialize initialize visible layer and runs hidden layer
 * Third superstep: HiddenInitialize initialize hidden layer and start learn
 * Schedule to learn:
 *      VisibleNeuronZero
 *      HiddenNeuronFirst
 *      VisibleNeuronFirst
 *      (maxStepInSample - 5) times NeuronDefault
 *      VisibleNeuronLast
 *      HiddenNeuronLast
 */

public class Master extends DefaultMasterCompute {

    /**
     * State constants
     */
    private enum STATE {
        CREATE_NETWORK,
        INIT_VISIBLE_LAYER,
        INIT_HIDDEN_LAYER,
        READ_DATA,
        FIRST_HIDDEN_STEP,
        FIRST_VISIBLE_STEP,
        UPDATE_ACTIVATE_VALUE,
        UPDATE_VISIBLE_LAYER,
        UPDATE_HIDDEN_LAYER,
        ONE_VISIBLE_STEP,
    }

    private STATE current_state = STATE.CREATE_NETWORK;

	private int epochNumber;
	private int batchNumber;
	private int stepInSample;
	/**
	 * Constants from context
	 * **/
	private static Integer maxEpochNumber = 1;
	private static Integer maxBatchNumber = 1;
	private static Integer maxStepInSample = 6;

	private static IntConfOption maxEpochNumber_OPTION = new IntConfOption(Master.class.getPackage().getName() + ".maxEpochNumber", 1,
			"Maximum number of epoch");
	private static IntConfOption maxBatchNumber_OPTION = new IntConfOption(Master.class.getPackage().getName() + ".maxBatchNumber", 1,
			"Maximum number of batches");
	private static IntConfOption maxStepInSample_OPTION = new IntConfOption(Master.class.getPackage().getName() + ".maxStepInSample", 6,
			"Maximum number of steps for one batch");

	public void initialize() throws InstantiationException, IllegalAccessException {
		epochNumber = 0;
		batchNumber = 0;
		stepInSample = 0;

		ImmutableClassesGiraphConfiguration conf = getConf();
		maxEpochNumber = maxEpochNumber_OPTION.get(conf);
		maxBatchNumber = maxBatchNumber_OPTION.get(conf);
		maxStepInSample = (maxStepInSample_OPTION.get(conf) + 1) * 2;
	}

	public void compute() {
		if (epochNumber == maxEpochNumber)
			haltComputation();

		UpdateState();
	}

    private void UpdateState() {
        if (current_state == STATE.CREATE_NETWORK) {
            setComputation(InitialNode.class);
            current_state = STATE.INIT_VISIBLE_LAYER;
        } else if (current_state == STATE.INIT_VISIBLE_LAYER) {
            setComputation(VisibleInitialize.class);
            current_state = STATE.INIT_HIDDEN_LAYER;
        } else if (current_state == STATE.INIT_HIDDEN_LAYER) {
            setComputation(HiddenInitialize.class);
            current_state = STATE.READ_DATA;
        } else {
            /**
             * Main loop
             */
            if (current_state == STATE.READ_DATA) {
                setComputation(VisibleNeuronZero.class);
                current_state = STATE.FIRST_HIDDEN_STEP;
            } else if (current_state == STATE.FIRST_HIDDEN_STEP) {
                setComputation(HiddenNeuronFirst.class);
                if (maxStepInSample == 4) //Only one step to update edges
                    current_state = STATE.ONE_VISIBLE_STEP;
                else
                    current_state = STATE.FIRST_VISIBLE_STEP;
            } else if (current_state == STATE.ONE_VISIBLE_STEP) {
                setComputation(VisibleNeuronOneStep.class);
                current_state = STATE.UPDATE_HIDDEN_LAYER;
            } else if (current_state == STATE.FIRST_VISIBLE_STEP) {
                setComputation(VisibleNeuronFirst.class);
                current_state = STATE.UPDATE_ACTIVATE_VALUE;
            } else if (current_state == STATE.UPDATE_ACTIVATE_VALUE) {
                setComputation(NeuronDefault.class);
            } else if (current_state == STATE.UPDATE_VISIBLE_LAYER) {
                setComputation(VisibleNeuronLast.class);
                current_state = STATE.UPDATE_HIDDEN_LAYER;
            } else if (current_state == STATE.UPDATE_HIDDEN_LAYER) {
                setComputation(HiddenNeuronLast.class);
                current_state = STATE.READ_DATA;
            }
            stepInSample++;
            batchNumber += stepInSample / maxStepInSample;
            stepInSample = stepInSample % maxStepInSample;
            epochNumber += batchNumber / maxBatchNumber;
            batchNumber = batchNumber % maxBatchNumber;
            if (current_state != STATE.ONE_VISIBLE_STEP && stepInSample + 2 == maxStepInSample) {
                current_state = STATE.UPDATE_VISIBLE_LAYER;
            }
        }
    }
}