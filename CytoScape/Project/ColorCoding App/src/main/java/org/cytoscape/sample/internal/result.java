package org.cytoscape.sample.internal;

import java.util.ArrayList;

public class result {
    ArrayList<Integer> path;
    double pathcost;

    public result( ArrayList<Integer> path,double pathcost){
         this.path=path;
         this.pathcost=pathcost;
    }
    
}
