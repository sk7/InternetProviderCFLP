package ads2.ss15.cflp;

import java.util.Arrays;

public class CFLP extends AbstractCFLP {
    private CFLPInstance instance;

    private int[] solution;

    private Integer[] customers;
    private Integer[] indexedCustomers;

    private int[] closestFacilities;
    private int[] availableCustomers;
    private int[] availableBandwidths;

    private int facilitiesAmount, customersAmount;

    private int upperBound = Integer.MAX_VALUE;
    private int lowerBound = 0;

    public CFLP(final CFLPInstance instance) {
        this.instance = instance;
        this.facilitiesAmount = instance.getNumFacilities();
        this.customersAmount = instance.getNumCustomers();

        this.solution = new int[customersAmount];
        Arrays.fill(solution, -1);

        this.availableCustomers = instance.maxCustomers.clone();

        this.availableBandwidths = new int[facilitiesAmount];
        Arrays.fill(availableBandwidths, instance.maxBandwidth);

        //
        //
        // FILL OUT CUSTOMERS

        this.customers = new Integer[customersAmount];
        this.indexedCustomers = new Integer[customersAmount];

        for (int i = 0; i < customersAmount; ++i) {
            customers[i] = instance.bandwidthOf(i);
            indexedCustomers[i] = i;
        }

        sort(customers, indexedCustomers);

        //
        //
        // FIND CLOSEST FACILITIES

        this.closestFacilities = new int[customersAmount];
        for (int i = 0; i < customersAmount; ++i) {
            int min = Integer.MAX_VALUE;
            for (int j = 0; j < facilitiesAmount; ++j) {
                if (instance.distances[j][i] < min) {
                    min = instance.distances[j][i];
                }
            }
            closestFacilities[i] = min;
        }
    }

    private void sort(Integer[] arr, Integer[] indices) {
        for (int i = 1, len = arr.length; i < len; ++i) {
            int temp = arr[i];
            int itemp = indices[i];
            int j = i;

            while (j > 0 && arr[j - 1] < temp) {
                arr[j] = arr[j - 1];
                indices[j] = indices[j - 1];
                --j;
            }

            arr[j] = temp;
            indices[j] = itemp;
        }
    }

    private void solveProblem(int customer) {
        if (customer < customersAmount) {
            for (int i = 0; i < facilitiesAmount; ++i) {
                int icustomer = indexedCustomers[customer];

                if (availableCustomers[i] > 0 && availableBandwidths[i] >= instance.bandwidths[icustomer]) {
                    solution[icustomer] = i;
                    availableBandwidths[i] -= instance.bandwidths[icustomer];
                    --availableCustomers[i];

                    if (customer + 1 < customersAmount) {
                        int localLower = 0;

                        for (int j = 0; j < customersAmount; ++j) {
                            if (solution[j] == -1) {
                                localLower += closestFacilities[j] * instance.distanceCosts;
                            }
                        }

                        lowerBound = instance.calcObjectiveValue(solution) + localLower;
                    }

                    if (lowerBound < upperBound) {
                        solveProblem(customer + 1);
                    }

                    // RESET
                    solution[icustomer] = -1;
                    availableBandwidths[i] += instance.bandwidths[icustomer];
                    ++availableCustomers[i];
                }
            }
        } else {
            int currentSolution = instance.calcObjectiveValue(solution);
            if (currentSolution < upperBound) {
                upperBound = currentSolution;
                setSolution(upperBound, solution);
            }
        }
    }

    @Override
    public void run() {
        solveProblem(0);
    }
}
