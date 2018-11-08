 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import ai.poadaptive.POAdaptiverush;
import ai.poadaptive.RandomPOAdaptiverush;


import ai.core.AI;
import ai.*;
import ai.abstraction.LightRush;
import ai.mcts.naivemcts.NaiveMCTS;
import ai.mcts.believestatemcts.BS3_NaiveMCTS;
import ai.mcts.informedmcts.InformedNaiveMCTS;
import ai.core.ContinuingAI;
import ai.abstraction.partialobservability.POLightRush;
import ai.abstraction.partialobservability.POWorkerRush;
import ai.abstraction.partialobservability.POHeavyRush;
import ai.abstraction.partialobservability.PORangedRush;

import ai.abstraction.pathfinding.BFSPathFinding;
import gui.PhysicalGameStatePanel;
import java.io.OutputStreamWriter;
import javax.swing.JFrame;
import rts.GameState;
import rts.PartiallyObservableGameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.units.UnitTypeTable;
import util.XMLWriter;

/**
 *
 * @author santi
 */
public class POGameVisualSimulationTest {
    public static void main(String args[]) throws Exception {
        UnitTypeTable utt = new UnitTypeTable(UnitTypeTable.VERSION_ORIGINAL_FINETUNED);

	// microRTS competition maps
	// PhysicalGameState pgs = PhysicalGameState.load("maps/8x8/basesWorkers8x8A.xml", utt);
	// PhysicalGameState pgs = PhysicalGameState.load("maps/16x16/basesWorkers16x16A.xml", utt);
	// PhysicalGameState pgs = PhysicalGameState.load("maps/BWDistantResources32x32.xml", utt);
	// PhysicalGameState pgs = PhysicalGameState.load("maps/BroodWar/(4)BloodBath.scmB.xml", utt);
	// PhysicalGameState pgs = PhysicalGameState.load("maps/8x8/FourBasesWorkers8x8.xml", utt);
	// PhysicalGameState pgs = PhysicalGameState.load("maps/16x16/TwoBasesBarracks16x16.xml", utt);
	// PhysicalGameState pgs = PhysicalGameState.load("maps/NoWhereToRun9x8.xml", utt);
	// PhysicalGameState pgs = PhysicalGameState.load("maps/DoubleGame24x24.xml", utt);

	// not in the competition
	//PhysicalGameState pgs = PhysicalGameState.load("maps/24x24/basesWorkers24x24A.xml", utt);
	PhysicalGameState pgs = PhysicalGameState.load("maps/32x32/basesWorkers32x32A.xml", utt);
	// PhysicalGameState pgs = PhysicalGameState.load("maps/BroodWar/(2)Benzene.scxA.xml", utt);
	
        GameState gs = new GameState(pgs, utt);
        int MAXCYCLES = 5000;
        int PERIOD = 20;
        boolean gameover = false;

	AI ai1 = new POAdaptiverush(utt, "src/ai/poadaptive/distributions.xml", "src/ai/poadaptive/distribution_woutb.xml", "src/ai/poadaptive/solver_cpp");
        // AI ai1 = new POAdaptiverush(utt, "src/ai/poadaptive/distributions.xml", "src/ai/poadaptive/solver_cpp");
	// AI ai1 = new RandomPOAdaptiverush(utt, "src/ai/poadaptive/distributions.xml", "src/ai/poadaptive/distribution_woutb.xml", "src/ai/poadaptive/solver_cpp");


	// AI ai2 = new RandomAI();
        // AI ai2 = new POWorkerRush(utt, new BFSPathFinding());
        AI ai2 = new POLightRush(utt, new BFSPathFinding());
        // AI ai2 = new POHeavyRush(utt, new BFSPathFinding());
	// AI ai2 = new PORangedRush(utt, new BFSPathFinding());
	// AI ai2 = new ContinuingNaiveMC(PERIOD, 200, 0.33f, 0.2f, new RandomBiasedAI(), new SimpleEvaluationFunction());
        // AI ai2 = new RandomBiasedAI();
	// AI ai2 = new LightRush();
        // AI ai2 = new BS3_NaiveMCTS(utt);
        ai2.preGameAnalysis(gs, 100);

        
        // XMLWriter xml = new XMLWriter(new OutputStreamWriter(System.out));
        // pgs.toxml(xml);
        // xml.flush();

        System.out.println("####################\n#######################\n");

        JFrame w = PhysicalGameStatePanel.newVisualizer(gs,640,640, true, 2);


        long nextTimeToUpdate = System.currentTimeMillis() + PERIOD;

        do{
            if (System.currentTimeMillis()>=nextTimeToUpdate) {
                PlayerAction pa1 = ai1.getAction(0, new PartiallyObservableGameState(gs,0));
                PlayerAction pa2 = ai2.getAction(1, new PartiallyObservableGameState(gs,1));
                gs.issueSafe(pa1);
                gs.issueSafe(pa2);

                // simulate:
                gameover = gs.cycle();
                w.repaint();
                nextTimeToUpdate+=PERIOD;
            } else {
                try {
                    Thread.sleep(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }while(!gameover && gs.getTime()<MAXCYCLES);
        ai1.gameOver(gs.winner());
        ai2.gameOver(gs.winner());
        
        System.out.println("Game Over");
    }    
}
