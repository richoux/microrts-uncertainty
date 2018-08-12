/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import ai.core.AI;
import ai.*;
import gui.PhysicalGameStateJFrame;
import gui.PhysicalGameStatePanel;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JFrame;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import rts.GameState;
import rts.PartiallyObservableGameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.Trace;
import rts.TraceEntry;
import rts.units.UnitTypeTable;
import util.XMLWriter;

/**
 *
 * @author santi
 */
public class Experimenter {
    public static int DEBUG = 0;
    public static boolean GC_EACH_FRAME = true;
    
    public static void runExperiments(List<AI> bots, List<PhysicalGameState> maps, UnitTypeTable utt, int iterations, int max_cycles, int max_inactive_cycles, boolean visualize) throws Exception {
        runExperiments(bots, maps, utt,iterations, max_cycles, max_inactive_cycles, visualize, System.out, -1, false);
    }

    public static void runExperimentsPartiallyObservable(List<AI> bots, List<PhysicalGameState> maps, UnitTypeTable utt, int iterations, int max_cycles, int max_inactive_cycles, boolean visualize) throws Exception {
        runExperiments(bots, maps, utt, iterations, max_cycles, max_inactive_cycles, visualize, System.out, -1, true);
    }

    public static void runExperiments(List<AI> bots, List<PhysicalGameState> maps, UnitTypeTable utt, int iterations, int max_cycles, int max_inactive_cycles, boolean visualize, PrintStream out) throws Exception {
        runExperiments(bots, maps, utt, iterations, max_cycles, max_inactive_cycles, visualize, out, -1, false);
    }

    public static void runExperimentsPartiallyObservable(List<AI> bots, List<PhysicalGameState> maps, UnitTypeTable utt, int iterations, int max_cycles, int max_inactive_cycles, boolean visualize, PrintStream out) throws Exception {
        runExperiments(bots, maps, utt, iterations, max_cycles, max_inactive_cycles, visualize, out, -1, true);
    }

    public static void runExperiments(List<AI> bots, List<PhysicalGameState> maps, UnitTypeTable utt, int iterations, int max_cycles, int max_inactive_cycles, boolean visualize, PrintStream out, 
                                      int run_only_those_involving_this_AI, boolean partiallyObservable) throws Exception {
        runExperiments(bots, maps, utt, iterations, max_cycles, max_inactive_cycles, visualize, out, run_only_those_involving_this_AI, false, partiallyObservable);
    }
 
    public static void runExperiments(List<AI> bots, List<PhysicalGameState> maps, UnitTypeTable utt, int iterations, int max_cycles, int max_inactive_cycles, boolean visualize, PrintStream out, 
            int run_only_those_involving_this_AI, boolean skip_self_play, boolean partiallyObservable) throws Exception {
    	runExperiments(bots, maps, utt, iterations, max_cycles, max_inactive_cycles, visualize, out, null, run_only_those_involving_this_AI, skip_self_play, partiallyObservable,
        		false, false, "");
    }
    public static void runExperiments(List<AI> bots, List<PhysicalGameState> maps, UnitTypeTable utt, int iterations, int max_cycles, int max_inactive_cycles, boolean visualize, PrintStream out, 
            int run_only_those_involving_this_AI, boolean skip_self_play, boolean partiallyObservable, boolean saveTrace, String traceDir) throws Exception {
    	runExperiments(bots, maps, utt, iterations, max_cycles, max_inactive_cycles, visualize, out, null, run_only_those_involving_this_AI, skip_self_play, partiallyObservable,
        		saveTrace, false, traceDir);
    }

    public static void runExperiments(List<AI> bots, List<PhysicalGameState> maps, UnitTypeTable utt, int iterations, int max_cycles, int max_inactive_cycles, boolean visualize, PrintStream out1, PrintStream out2, 
            int run_only_those_involving_this_AI, boolean skip_self_play, boolean partiallyObservable, boolean saveTrace, String traceDir) throws Exception {
    	runExperiments(bots, maps, utt, iterations, max_cycles, max_inactive_cycles, visualize, out1, out2, run_only_those_involving_this_AI, skip_self_play, partiallyObservable,
        		saveTrace, false, traceDir);
    }
    
    public static void runExperiments(List<AI> bots, List<PhysicalGameState> maps, UnitTypeTable utt, int iterations, int max_cycles, int max_inactive_cycles, boolean visualize, PrintStream out1, PrintStream out2, 
                                      int run_only_those_involving_this_AI, boolean skip_self_play, boolean partiallyObservable,
                                      boolean saveTrace, boolean saveZip, String traceDir) throws Exception {
        int top_wins[][] = new int[bots.size()][bots.size()];
        int top_ties[][] = new int[bots.size()][bots.size()];
        int top_loses[][] = new int[bots.size()][bots.size()];

        int bottom_wins[][] = new int[bots.size()][bots.size()];
        int bottom_ties[][] = new int[bots.size()][bots.size()];
        int bottom_loses[][] = new int[bots.size()][bots.size()];

        int current_wins[][] = top_wins;
        int current_ties[][] = top_ties;
        int current_loses[][] = top_loses;
        
        double top_win_time[][] = new double[bots.size()][bots.size()];
        double top_tie_time[][] = new double[bots.size()][bots.size()];
        double top_lose_time[][] = new double[bots.size()][bots.size()];

        double bottom_win_time[][] = new double[bots.size()][bots.size()];
        double bottom_tie_time[][] = new double[bots.size()][bots.size()];
        double bottom_lose_time[][] = new double[bots.size()][bots.size()];
        
        double current_win_time[][] = top_win_time;
        double current_tie_time[][] = top_tie_time;
        double current_lose_time[][] = top_lose_time;


        List<AI> bots2 = new LinkedList<>();

        PrintStream out = out1;

	System.out.println("Timeout: " + max_cycles);

        for(AI bot:bots) bots2.add(bot.clone());
        
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) 
        {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) 
            {
                if (run_only_those_involving_this_AI!=-1 &&
                    ai1_idx!=run_only_those_involving_this_AI &&
                    ai2_idx!=run_only_those_involving_this_AI) continue;
                // if (ai1_idx==0 && ai2_idx==0) continue;
                if (skip_self_play && ai1_idx==ai2_idx) continue;

                if(run_only_those_involving_this_AI == ai1_idx || out2 == null){
                    out = out1;
                    current_win_time = top_win_time;
                    current_tie_time = top_tie_time;
                    current_lose_time = top_lose_time;
                    
                    current_wins = top_wins;
                    current_ties = top_ties;
                    current_loses = top_loses;
                    System.out.println("AI top");
                }
                else if(run_only_those_involving_this_AI == ai2_idx){
                    System.out.println("AI bottom");
                    out = out2;
                    current_win_time = bottom_win_time;
                    current_tie_time = bottom_tie_time;
                    current_lose_time = bottom_lose_time;
                    
                    current_wins = bottom_wins;
                    current_ties = bottom_ties;
                    current_loses = bottom_loses;
                }
                int m=0;
                for(PhysicalGameState pgs:maps) {
                    
                    for (int i = 0; i < iterations; i++) {
                    	//cloning just in case an AI has a memory leak
                    	//by using a clone, it is discarded, along with the leaked memory,
                    	//after each game, rather than accumulating
                    	//over several games
                        AI ai1 = bots.get(ai1_idx).clone();
                        AI ai2 = bots2.get(ai2_idx).clone();


                        long lastTimeActionIssued = 0;

                        ai1.reset();
                        ai2.reset();

                        GameState gs = new GameState(pgs.clone(),utt);
                        PhysicalGameStateJFrame w = null;

                        ai1.preGameAnalysis(gs, 100);
                        ai2.preGameAnalysis(gs, 100);
                       
                        if (visualize) w = PhysicalGameStatePanel.newVisualizer(gs, 600, 600, partiallyObservable);

                        out.println("MATCH UP: " + ai1 + " vs " + ai2);
                        System.out.println("MATCH UP: " + ai1 + " vs " + ai2);
                        
                        boolean gameover = false;
                        Trace trace = null;
                        TraceEntry te;
                        if(saveTrace){
                        	trace = new Trace(utt);
                        	te = new TraceEntry(gs.getPhysicalGameState().clone(),gs.getTime());
                            trace.addEntry(te);
                        }


                        int PERIOD = 10;
                        long nextTimeToUpdate = System.currentTimeMillis() + PERIOD;

                        do {

                                nextTimeToUpdate+=PERIOD;
                            
                                // if (GC_EACH_FRAME) System.gc();
                                PlayerAction pa1 = null, pa2 = null;
                                if (partiallyObservable) {
                                    pa1 = ai1.getAction(0, new PartiallyObservableGameState(gs,0));
    //                                if (DEBUG>=1) {System.out.println("AI1 done.");out.flush();}
                                    pa2 = ai2.getAction(1, new PartiallyObservableGameState(gs,1));
    //                                if (DEBUG>=1) {System.out.println("AI2 done.");out.flush();}
                                } else {
                                    pa1 = ai1.getAction(0, gs);
                                    if (DEBUG>=1) {System.out.println("AI1 done.");out.flush();}
                                    pa2 = ai2.getAction(1, gs);
                                    if (DEBUG>=1) {System.out.println("AI2 done.");out.flush();}
                                }
                                if (saveTrace && (!pa1.isEmpty() || !pa2.isEmpty())) {
                                    te = new TraceEntry(gs.getPhysicalGameState().clone(),gs.getTime());
                                    te.addPlayerAction(pa1.clone());
                                    te.addPlayerAction(pa2.clone());
                                    trace.addEntry(te);
                                }
                                
                                if (gs.issueSafe(pa1)) lastTimeActionIssued = gs.getTime();
    //                            if (DEBUG>=1) {System.out.println("issue action AI1 done: " + pa1);out.flush();}
                                if (gs.issueSafe(pa2)) lastTimeActionIssued = gs.getTime();
    //                            if (DEBUG>=1) {System.out.println("issue action AI2 done:" + pa2);out.flush();}
                                gameover = gs.cycle();
                                if (DEBUG>=1) {System.out.println("cycle done.");out.flush();}
                                if (w!=null) {
                                    w.setStateCloning(gs);
                                    w.repaint();
                                    try {
                                        Thread.sleep(1);    // give time to the window to repaint
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
    //                              if (DEBUG>=1) {System.out.println("repaint done.");out.flush();}
                                }

                        } while (!gameover && 
                                 (gs.getTime() < max_cycles) && 
                                 (gs.getTime() - lastTimeActionIssued < max_inactive_cycles));
                        System.gc();
                        ai1.gameOver(gs.winner());
                        ai2.gameOver(gs.winner());
                        if(saveTrace){
                        	te = new TraceEntry(gs.getPhysicalGameState().clone(), gs.getTime());
                        	trace.addEntry(te);
                        	XMLWriter xml;
                        	ZipOutputStream zip = null;
                        	String filename=ai1.toString()+"Vs"+ai2.toString()+"-"+m+"-"+i;
                        	filename=filename.replace("/", "");
                        	filename=filename.replace(")", "");
                        	filename=filename.replace("(", "");
                        	filename=traceDir+"/"+filename;
                        	if(saveZip){
                        		zip=new ZipOutputStream(new FileOutputStream("/home/stagiaire-tasc/Documents/Code/replays_micro_rts" +filename+".zip"));
                        		zip.putNextEntry(new ZipEntry("game.xml"));
                        		xml = new XMLWriter(new OutputStreamWriter(zip));
                        	}else{
                                // File f = new File("/home/Document/Code/replays_micro_rts" +filename+".xml");
                                FileWriter f = new FileWriter(filename+".xml");
                        		xml = new XMLWriter(f);
                        	}
                        	trace.toxml(xml);
                        	xml.flush();
                        	if(saveZip){
                        		zip.closeEntry();
                        		zip.close();
                        	}
                        }
                        if (w!=null) w.dispose();
                        int winner = gs.winner();
                        out.println("Winner: " + winner + "  in " + gs.getTime() + " cycles");
                        out.println(ai1 + " : " + ai1.statisticsString());
                        out.println(ai2 + " : " + ai2.statisticsString());
                        out.flush();
                        if (winner == -1) {
                            current_ties[ai1_idx][ai2_idx]++;
                            current_tie_time[ai1_idx][ai2_idx]+=gs.getTime();

                            current_ties[ai2_idx][ai1_idx]++;
                            current_tie_time[ai2_idx][ai1_idx]+=gs.getTime();
                        } else if (winner == 0) {
                            current_wins[ai1_idx][ai2_idx]++;
                            current_win_time[ai1_idx][ai2_idx]+=gs.getTime();

                            current_loses[ai2_idx][ai1_idx]++;
                            current_lose_time[ai2_idx][ai1_idx]+=gs.getTime();
                        } else if (winner == 1) {
                            current_loses[ai1_idx][ai2_idx]++;
                            current_lose_time[ai1_idx][ai2_idx]+=gs.getTime();

                            current_wins[ai2_idx][ai1_idx]++;
                            current_win_time[ai2_idx][ai1_idx]+=gs.getTime();
                        }                        
                    }  
                    m++;
                }
            }
        }

        out1.println("Wins: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                out1.print(top_wins[ai1_idx][ai2_idx] + ", ");
            }
            out1.println("");
        }
        out1.println("Ties: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                out1.print(top_ties[ai1_idx][ai2_idx] + ", ");
            }
            out1.println("");
        }
        out1.println("Loses: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                out1.print(top_loses[ai1_idx][ai2_idx] + ", ");
            }
            out1.println("");
        }        
        out1.println("Win average time: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                if (top_wins[ai1_idx][ai2_idx]>0) {
                    out1.print((top_win_time[ai1_idx][ai2_idx]/top_wins[ai1_idx][ai2_idx]) + ", ");
                } else {
                    out1.print("-, ");
                }
            }
            out1.println("");
        }
        out1.println("Tie average time: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                if (top_ties[ai1_idx][ai2_idx]>0) {
                    out1.print((top_tie_time[ai1_idx][ai2_idx]/top_ties[ai1_idx][ai2_idx]) + ", ");
                } else {
                    out1.print("-, ");
                }
            }
            out1.println("");
        }
        out1.println("Lose average time: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                if (top_loses[ai1_idx][ai2_idx]>0) {
                    out1.print((top_lose_time[ai1_idx][ai2_idx]/top_loses[ai1_idx][ai2_idx]) + ", ");
                } else {
                    out1.print("-, ");
                }
            }
            out1.println("");
        }              
        out1.flush();



        if(out2 != null){
            out2.println("Wins: ");
            for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
                for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                    out2.print(bottom_wins[ai1_idx][ai2_idx] + ", ");
                }
                out2.println("");
            }
            out2.println("Ties: ");
            for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
                for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                    out2.print(bottom_ties[ai1_idx][ai2_idx] + ", ");
                }
                out2.println("");
            }
            out2.println("Loses: ");
            for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
                for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                    out2.print(bottom_loses[ai1_idx][ai2_idx] + ", ");
                }
                out2.println("");
            }        
            out2.println("Win average time: ");
            for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
                for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                    if (bottom_wins[ai1_idx][ai2_idx]>0) {
                        out2.print((bottom_win_time[ai1_idx][ai2_idx]/bottom_wins[ai1_idx][ai2_idx]) + ", ");
                    } else {
                        out2.print("-, ");
                    }
                }
                out2.println("");
            }
            out2.println("Tie average time: ");
            for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
                for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                    if (bottom_ties[ai1_idx][ai2_idx]>0) {
                        out2.print((bottom_tie_time[ai1_idx][ai2_idx]/bottom_ties[ai1_idx][ai2_idx]) + ", ");
                    } else {
                        out2.print("-, ");
                    }
                }
                out2.println("");
            }
            out2.println("Lose average time: ");
            for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
                for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                    if (bottom_loses[ai1_idx][ai2_idx]>0) {
                        out2.print((bottom_lose_time[ai1_idx][ai2_idx]/bottom_loses[ai1_idx][ai2_idx]) + ", ");
                    } else {
                        out2.print("-, ");
                    }
                }
                out2.println("");
            }              
            out2.flush();
        }


        System.out.println("RESULT TOP");
        System.out.println("Wins: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                System.out.print(top_wins[ai1_idx][ai2_idx] + ", ");
            }
            System.out.println("");
        }
        System.out.println("Ties: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                System.out.print(top_ties[ai1_idx][ai2_idx] + ", ");
            }
            System.out.println("");
        }
        System.out.println("Loses: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                System.out.print(top_loses[ai1_idx][ai2_idx] + ", ");
            }
            System.out.println("");
        }        
        System.out.println("Win average time: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                if (top_wins[ai1_idx][ai2_idx]>0) {
                    System.out.print((top_win_time[ai1_idx][ai2_idx]/top_wins[ai1_idx][ai2_idx]) + ", ");
                } else {
                    System.out.print("-, ");
                }
            }
            System.out.println("");
        }
        System.out.println("Tie average time: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                if (top_ties[ai1_idx][ai2_idx]>0) {
                    System.out.print((top_tie_time[ai1_idx][ai2_idx]/top_ties[ai1_idx][ai2_idx]) + ", ");
                } else {
                    System.out.print("-, ");
                }
            }
            System.out.println("");
        }
        System.out.println("Lose average time: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                if (top_loses[ai1_idx][ai2_idx]>0) {
                    System.out.print((top_lose_time[ai1_idx][ai2_idx]/top_loses[ai1_idx][ai2_idx]) + ", ");
                } else {
                    System.out.print("-, ");
                }
            }
            System.out.println("");
        }  


        System.out.println("#####\nRESULT BOTTOM");
        System.out.println("Wins: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                System.out.print(bottom_wins[ai1_idx][ai2_idx] + ", ");
            }
            System.out.println("");
        }
        System.out.println("Ties: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                System.out.print(bottom_ties[ai1_idx][ai2_idx] + ", ");
            }
            System.out.println("");
        }
        System.out.println("Loses: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                System.out.print(bottom_loses[ai1_idx][ai2_idx] + ", ");
            }
            System.out.println("");
        }        
        System.out.println("Win average time: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                if (bottom_wins[ai1_idx][ai2_idx]>0) {
                    System.out.print((bottom_win_time[ai1_idx][ai2_idx]/bottom_wins[ai1_idx][ai2_idx]) + ", ");
                } else {
                    System.out.print("-, ");
                }
            }
            System.out.println("");
        }
        System.out.println("Tie average time: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                if (bottom_ties[ai1_idx][ai2_idx]>0) {
                    System.out.print((bottom_tie_time[ai1_idx][ai2_idx]/bottom_ties[ai1_idx][ai2_idx]) + ", ");
                } else {
                    System.out.print("-, ");
                }
            }
            System.out.println("");
        }
        System.out.println("Lose average time: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                if (bottom_loses[ai1_idx][ai2_idx]>0) {
                    System.out.print((bottom_lose_time[ai1_idx][ai2_idx]/bottom_loses[ai1_idx][ai2_idx]) + ", ");
                } else {
                    System.out.print("-, ");
                }
            }
            System.out.println("");
        }


	System.out.println("#####\nGLOBAL RESULTS");
	System.out.println("Wins: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                System.out.print(bottom_wins[ai1_idx][ai2_idx]+top_wins[ai1_idx][ai2_idx] + ", ");
            }
            System.out.println("");
        }
        System.out.println("Ties: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                System.out.print(bottom_ties[ai1_idx][ai2_idx]+top_ties[ai1_idx][ai2_idx] + ", ");
            }
            System.out.println("");
        }
        System.out.println("Loses: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                System.out.print(bottom_loses[ai1_idx][ai2_idx]+top_loses[ai1_idx][ai2_idx] + ", ");
            }
            System.out.println("");
        }        
        System.out.println("Win average time: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                if (bottom_wins[ai1_idx][ai2_idx]>0) {
		  System.out.print(((bottom_win_time[ai1_idx][ai2_idx]+top_win_time[ai1_idx][ai2_idx])/(bottom_wins[ai1_idx][ai2_idx]+top_wins[ai1_idx][ai2_idx])) + ", ");
                } else {
                    System.out.print("-, ");
                }
            }
            System.out.println("");
        }
        System.out.println("Tie average time: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                if (bottom_ties[ai1_idx][ai2_idx]>0) {
		  System.out.print(((bottom_tie_time[ai1_idx][ai2_idx]+top_tie_time[ai1_idx][ai2_idx])/(bottom_ties[ai1_idx][ai2_idx]+top_ties[ai1_idx][ai2_idx])) + ", ");
                } else {
                    System.out.print("-, ");
                }
            }
            System.out.println("");
        }
        System.out.println("Lose average time: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                if (bottom_loses[ai1_idx][ai2_idx]>0) {
		  System.out.print(((bottom_lose_time[ai1_idx][ai2_idx]+top_lose_time[ai1_idx][ai2_idx])/(bottom_loses[ai1_idx][ai2_idx]+top_loses[ai1_idx][ai2_idx])) + ", ");
                } else {
                    System.out.print("-, ");
                }
            }
            System.out.println("");
        }
    }
}
