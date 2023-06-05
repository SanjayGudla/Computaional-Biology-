package org.cytoscape.sample.internal;

import java.util.*;
import java.util.Random; 


class colorCodingImpl {
    private int greatestSetBit(int n, int k) {
        int t1 = 1 << (k - 1);
        for (int i = k - 1; i >= 0; i--) {
            if (n >= t1)
                return i;
            t1 = t1 >> 1;
        }
        return -1;// this should never be executed
    }

    private int expVal(int k) {
        return 1 << k;
    }

    private int setSpecificBit(int n, int k) {
        return n ^ (1 << k);
    }

    private boolean specificBitSetOrNot(int n, int k) {
        return !((n & (1 << k)) == 0);
    }

    public ArrayList<result> ColorCoding(int numVertices, ArrayList<ArrayList<Integer>> graph_adjlist, double graph_adjMat[][],
            List<Integer> sourcenodes, List<Integer> destinationnodes, int path_length,double prob) {

       
        int[] colorsAssigned = new int[numVertices];
        int numRandomizedTrials;
        double epsilon = 1-prob;
        int expPathLen = expVal(path_length);
        double[] finalMaxPathLen = new double[numVertices];

        ArrayList<Stack<Integer>> finalPath = new ArrayList<Stack<Integer>>(numVertices);
        for (int i = 0; i < numVertices; i++) {
            finalPath.add(new Stack<Integer>());
        }

        boolean[] finalPathFeasible = new boolean[numVertices];
        ArrayList<ArrayList<Integer>> finalPathArr = new ArrayList<ArrayList<Integer>>(numVertices);
        for (int i = 0; i < numVertices; i++) {
            finalPathArr.add(new ArrayList<Integer>());
        }

        for (int i = 0; i < numVertices; i++) {
            finalMaxPathLen[i] = -1.0;
            finalPathFeasible[i] = false;
        }

        // setting the number of randomized trials
        numRandomizedTrials = (int) Math.floor(-Math.log(epsilon) * Math.exp(path_length));
        // numRandomizedTrials=1;
        // repeating the algorithm for a specified number of trials
        for (int i = 0; i < numRandomizedTrials; i++) {
            Random random = new Random();
            // Assigning random colors to vertices
            for (int j = 0; j < numVertices; j++) {
                colorsAssigned[j] = random.nextInt(path_length) + 1;
            }
            // defining the variables required to be used in the algo
            Queue<Integer> q1 = new LinkedList<Integer>(), q2 = new LinkedList<Integer>();
            double[][] maxLenPath = new double[expPathLen][numVertices];
            boolean[][] pathFeasible = new boolean[expPathLen][numVertices];
            int[][] backtrack = new int[expPathLen][numVertices];

            // filling q1 with 2^0, 2^1, ... , 2^{path_length-1}
            int temp1 = 1, temp2, temp3;
            for (int j = 1; j <= path_length; j++) {

                // implementing the algo for 1 length paths
                for (int k = 0; k < numVertices; k++) {
                    if (sourcenodes.contains(k) && (colorsAssigned[k] == j)) {
                        maxLenPath[temp1][k] = 0.0;
                        pathFeasible[temp1][k] = true;
                    } else {
                        maxLenPath[temp1][k] = -1.0;
                        pathFeasible[temp1][k] = false;
                    }
                }
                q1.add(temp1);
                temp1 *= 2;

            }

            // implements the crux of the algorithm
            for (int j = 2; j <= path_length; j++) {

                // filling q2 with required values
                while (!q1.isEmpty()) {
                    temp1 = q1.remove();
                    int gbs = greatestSetBit(temp1, path_length);
                    for (int k = gbs + 1; k <= path_length - 1; k++) {
                        q2.add(setSpecificBit(temp1, k));
                    }
                }

                // taking the values from q2 and implementing the crux of the algo and
                // putting them back into q1

                while (!q2.isEmpty()) {
                    temp1 = q2.remove();
                    q1.add(temp1);
                    for (int k = 0; k < numVertices; k++) {
                        maxLenPath[temp1][k] = -1.0;
                        pathFeasible[temp1][k] = false;

                        // if the color exists in the set then look for neighbours
                        if (specificBitSetOrNot(temp1, colorsAssigned[k] - 1)) {

                            // set the specific to 0 and then see if any of the partners admit a
                            // valid path, if yes then check if the resulting path to it exceeds
                            // its current value, if yes then update
                            temp2 = setSpecificBit(temp1, colorsAssigned[k] - 1);

                            ArrayList<Integer> nbrs = graph_adjlist.get(k);

                            for (Integer nbr : nbrs) {
                                if (pathFeasible[temp2][nbr]) {
                                    pathFeasible[temp1][k] = true;
                                    if (maxLenPath[temp2][nbr] + graph_adjMat[k][nbr] > maxLenPath[temp1][k]) {
                                        maxLenPath[temp1][k] = maxLenPath[temp2][nbr] + graph_adjMat[k][nbr];
                                        backtrack[temp1][k] = nbr;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // printing the dp table

            // updating the path to all vertices if a better one is found

            temp1 = expPathLen - 1;
            for (int j = 0; j < numVertices; j++) {
                if (pathFeasible[temp1][j]) {
                    finalPathFeasible[j] = true;
                    if (finalMaxPathLen[j] < maxLenPath[temp1][j]) {
                        finalMaxPathLen[j] = maxLenPath[temp1][j];
                        finalPath.get(j).clear();
                        finalPath.get(j).push(j);
                        temp2 = j;
                        for (int k = 1; k <= path_length - 1; k++) {
                            finalPath.get(j).push(backtrack[temp1][temp2]);
                            temp3 = backtrack[temp1][temp2];
                            temp1 = setSpecificBit(temp1, colorsAssigned[temp2] - 1);
                            temp2 = temp3;
                        }
                    }
                }
            }

        }

        for (int i = 0; i < numVertices; i++) {
            while (!finalPath.get(i).empty()) {
                finalPathArr.get(i).add(finalPath.get(i).pop());
            }
        }
       
        ArrayList<result> result = new ArrayList<result>();

        for (int i = 0; i < numVertices; i++) {
            if (destinationnodes.contains(i) && finalPathFeasible[i]) {
                ArrayList<Integer> path = finalPathArr.get(i);
                result ans = new result(path, finalMaxPathLen[i]);
                result.add(ans);
            }
        }
        return result;

    }
}