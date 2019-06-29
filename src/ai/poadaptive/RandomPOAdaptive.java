/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.poadaptive;


import java.util.concurrent.ThreadLocalRandom;

import ai.abstraction.pathfinding.AStarPathFinding;
import ai.abstraction.*;
import ai.abstraction.pathfinding.PathFinding;
import ai.core.AI;
import ai.core.ParameterSpecification;
import rts.GameState;
import rts.PartiallyObservableGameState;
import rts.PhysicalGameState;
import rts.Player;
import rts.GameState;
import rts.PlayerAction;
import rts.units.*;

import org.jdom.*;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Random;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import java.io.File;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
// import java.lang.Float.parseFloat;


/**
 *
 * @author santi
 */
public class RandomPOAdaptive extends POAdaptive {

    boolean heavy=false;
    
    public RandomPOAdaptive(UnitTypeTable a_utt, String distribution_file_b, String distribution_file_wb, String solver) {
        this(a_utt, new AStarPathFinding(), distribution_file_b, distribution_file_wb, solver);
    }

    public RandomPOAdaptive(UnitTypeTable a_utt, PathFinding a_pf, String distribution_file_b, String distribution_file_wb, String solver) {
        super(a_utt, a_pf, distribution_file_b, distribution_file_wb, solver);
        
    }

    @Override
    public AI clone() {
        return new RandomPOAdaptive(utt, pf, distribution_file_b, distribution_file_woutb, solver_name);
    }

    @Override
    public void barracksBehavior(Unit u, Player p, PhysicalGameState pgs, int time) {

        if(heavy){
            if(p.getResources() >= heavyType.cost){
                train(u, heavyType);
                heavy = false;
            }
        }else{
            if(p.getResources() >= 2){
                
                int randomNum = ThreadLocalRandom.current().nextInt(0, 3);
                // int randomNum = 1;

                switch(randomNum){
                    case 0:
                        if(p.getResources() >= heavyType.cost)
                            train(u, heavyType);
                        else{
                            heavy=true;
                        }
                        break;
                    case 1:
                        if(p.getResources() >= rangedType.cost)
                            train(u, rangedType);
                        break;
                    case 2:
                        if(p.getResources() >= lightType.cost)
                            train(u, lightType);
                        break;
                }
            }
        }
    }
 
   
}
