/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.poadaptive;

import java.util.concurrent.ThreadLocalRandom;
import java.lang.NumberFormatException;

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
import rts.UnitActionAssignment;
import rts.UnitAction;

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
 * @author Valentin Antuari
 * (modifications from Florian Richoux)
 */
public class POAdaptive extends AbstractionLayerAI
{
	Random r = new Random();
	protected UnitTypeTable utt;

	public static boolean INFO = false;
	public static boolean DEBUG = false;

	public static int NB_SAMPLE = 50;

	public static float WORKER_PER_LIGHT = 5.f;
	public static float WORKER_PER_RANGED = 5.f;
	public static float HEAVY_PER_HEAVY = 0.5f;
	public static float HEAVY_PER_RANGED = 0.5f;
	public static float RANGED_PER_LIGHT = 1.5f;
	public static float LIGHT_PER_HEAVY = 1.5f;

	String solver_path;
	String solver_name;
	double[][] heat_map;

	int observedWorker = 0;
	int observedLight = 0;
	int observedHeavy = 0;
	int observedRanged = 0;

	boolean random_version = false;
	boolean scout = false;
	// not BASIC BEHAVIOR
	// long scout_ID = -1;

	int my_resource_patches;
	int number_melee_units;
	List<Unit> workers;

	int nbSamples;
	HashMap<Integer, HashMap> distribution_b;
	HashMap<Integer, HashMap> distribution_woutb;
	String distribution_file_b;
	String distribution_file_woutb;
	UnitType workerType;
	UnitType baseType;
	UnitType barracksType;
	UnitType lightType;
	UnitType heavyType;
	UnitType rangedType;

	boolean barracks;

	public POAdaptive( UnitTypeTable a_utt,
	                   PathFinding a_pf,
	                   String distribution_file_b,
	                   String distribution_file_wb,
	                   String solver_path,
	                   double[][] heat_map )
	{
		this( a_utt, new AStarPathFinding(), distribution_file_b, distribution_file_wb, solver_path );
		if( heat_map != null )
		{
			this.heat_map = new double[ heat_map.length ][];
			for( int i = 0 ; i < heat_map.length ; ++i )
				this.heat_map[i] = heat_map[i].clone();
		}
	}

	public POAdaptive( UnitTypeTable a_utt,
	                   String distribution_file_b,
	                   String distribution_file_wb,
	                   String solver_path )
	{
		this( a_utt, new AStarPathFinding(), distribution_file_b, distribution_file_wb, solver_path );
	}

	public POAdaptive( UnitTypeTable a_utt,
	                   PathFinding a_pf,
	                   String distribution_file_b,
	                   String distribution_file_wb,
	                   String solver_path )
	{
		super( a_pf );
		reset( a_utt );
		this.solver_path = solver_path;
		this.nbSamples = NB_SAMPLE;
		this.distribution_file_b = distribution_file_b;
		this.distribution_file_woutb = distribution_file_wb;

		SAXBuilder sxb = new SAXBuilder();
		org.jdom.Document document;
		try
		{
			document = sxb.build( new File( distribution_file_b ) );
		}
		catch( Exception e )
		{
			document = null;
		}

		distribution_b = new HashMap<Integer, HashMap>();

		Element root = document.getRootElement();
		List<Element> time = root.getChildren( "time" );
		for( Element item : time )
		{
			String w [] = item.getChild("worker").getText().split(" ");
			String r [] = item.getChild("ranged").getText().split(" ");
			String h [] = item.getChild("heavy").getText().split(" ");
			String l [] = item.getChild("light").getText().split(" ");

			HashMap<String, List> tmp = new HashMap<String, List>();
			ArrayList<Float> listW = new ArrayList<Float>();
			ArrayList<Float> listR = new ArrayList<Float>();
			ArrayList<Float> listL = new ArrayList<Float>();
			ArrayList<Float> listH = new ArrayList<Float>();

			int end = w.length;
			for( int i = 0 ; i < end ; ++i )
				listW.add(Float.parseFloat(w[i]));

			end = r.length;
			for( int i = 0 ; i < end ; ++i )
				listR.add(Float.parseFloat(r[i]));

			end = l.length;
			for( int i = 0 ; i < end ; ++i )
				listL.add(Float.parseFloat(l[i]));

			end = h.length;
			for( int i = 0 ; i < end ; ++i )
				listH.add(Float.parseFloat(h[i]));

			tmp.put( "worker", listW );
			tmp.put( "ranged", listR );
			tmp.put( "light", listL );
			tmp.put( "heavy", listH );

			distribution_b.put( Integer.parseInt( item.getAttribute( "time" ).getValue() ), tmp );
		}

		try
		{
			document = sxb.build( new File( distribution_file_woutb ) );
		}
		catch( Exception e )
		{
			document = null;
		}

		distribution_woutb = new HashMap<Integer, HashMap>();

		root = document.getRootElement();
		time = root.getChildren( "time" );
		for( Element item : time )
		{
			String w [] = item.getChild( "worker" ).getText().split(" ");
			String r [] = item.getChild( "ranged" ).getText().split(" ");
			String h [] = item.getChild( "heavy" ).getText().split(" ");
			String l [] = item.getChild( "light" ).getText().split(" ");

			HashMap<String, List> tmp = new HashMap<String, List>();
			ArrayList<Float> listW = new ArrayList<Float>();
			ArrayList<Float> listR = new ArrayList<Float>();
			ArrayList<Float> listL = new ArrayList<Float>();
			ArrayList<Float> listH = new ArrayList<Float>();

			int end = w.length;
			for( int i = 0 ; i < end ; ++i )
				listW.add( Float.parseFloat( w[i] ) );

			end = r.length;
			for( int i = 0 ; i < end ; ++i )
				listR.add( Float.parseFloat( r[i] ) );

			end = l.length;
			for( int i = 0 ; i < end ; ++i )
				listL.add( Float.parseFloat( l[i] ) );

			end = h.length;
			for( int i = 0 ; i < end ; ++i )
				listH.add( Float.parseFloat( h[i] ) );

			tmp.put( "worker", listW );
			tmp.put( "ranged", listR );
			tmp.put( "light", listL );
			tmp.put( "heavy", listH );

			distribution_woutb.put( Integer.parseInt( item.getAttribute( "time" ).getValue() ), tmp );
		}
		// System.out.println(distribution);
	}

	public AI clone()
	{
		return new POAdaptive( utt, pf, distribution_file_b, distribution_file_woutb, solver_path, heat_map );
	}

	public void reset()
	{
		observedWorker = 0;
		observedLight = 0;
		observedHeavy = 0;
		observedRanged = 0;
		heat_map = null;
		workerType = utt.getUnitType( "Worker" );
		baseType = utt.getUnitType( "Base" );
		barracksType = utt.getUnitType( "Barracks" );
		lightType = utt.getUnitType( "Light" );
		heavyType = utt.getUnitType( "Heavy" );
		rangedType = utt.getUnitType( "Ranged" );
		barracks = false;
		super.reset();
	}

	private void init_heat_map( PhysicalGameState pgs, GameState gs )
	{
		int map_width = pgs.getWidth();
		int map_height = pgs.getHeight();
		heat_map = new double[ map_width ][ map_height ];
		if( gs instanceof PartiallyObservableGameState )
		{
			PartiallyObservableGameState pogs = (PartiallyObservableGameState)gs;
			for( int i = 0 ; i < map_width ; ++i )
				for( int j = 0 ; j < map_height ; ++j )
					if( pogs.observable( i, j ) )
						heat_map[i][j] = gs.getTime();
					else
						heat_map[i][j] = -1.0;
		}
	}

	private void update_heat_map( PhysicalGameState pgs, GameState gs )
	{
		int map_width = pgs.getWidth();
		int map_height = pgs.getHeight();
		if( gs instanceof PartiallyObservableGameState )
		{
			PartiallyObservableGameState pogs = (PartiallyObservableGameState)gs;
			for( int i = 0 ; i < map_width ; ++i )
				for( int j = 0 ; j < map_height ; ++j )
					if( pogs.observable( i, j ) )
						heat_map[i][j] = gs.getTime();
		}
	}

	public void reset( UnitTypeTable a_utt )
	{
		utt = a_utt;
		this.reset();
	}

/*
  This is the main function of the AI. It is called at each game cycle with the most up to date game state and
  returns which actions the AI wants to execute in this cycle.
  The input parameters are:
  - player: the player that the AI controls (0 or 1)
  - gs: the current game state
  This method returns the actions to be sent to each of the units in the gamestate controlled by the player,
  packaged as a PlayerAction.
*/
	public PlayerAction getAction( int player, GameState gs )
	{
		PhysicalGameState pgs = gs.getPhysicalGameState();
		Player p = gs.getPlayer( player );

		if( heat_map == null )
			init_heat_map( pgs, gs );
		else
			update_heat_map( pgs, gs );

		// System.out.println("LightRushAI for player " + player + " (cycle " + gs.getTime() + ")");

		// map tiles
		long map_tiles = pgs.getWidth() * pgs.getHeight();
		double distance_threshold = Math.sqrt( map_tiles ) / 4;

		// determine how many resource patches I have near my bases, given a distance threshold
		my_resource_patches = 0;
		number_melee_units = 0;
		workers = new LinkedList<Unit>();

		for( Unit u : pgs.getUnits() )
		{
			if( u.getType().isResource )
				for( Unit b : pgs.getUnits() )
					if( b.getType().isStockpile && b.getPlayer() == p.getID() )
						if( Math.sqrt( Math.pow( b.getX() - u.getX(), 2 ) + Math.pow( b.getY() - u.getY(), 2 ) ) <= distance_threshold )
							++my_resource_patches;

			if( u.getType().canAttack && !u.getType().canHarvest && u.getPlayer() == player && gs.getActionAssignment(u) == null )
				++number_melee_units;

			if( u.getType().canHarvest && u.getPlayer() == player )
				workers.add(u);
		}

		for( Unit u : pgs.getUnits() )
		{
			// behavior of bases:
			if( u.getType() == baseType
			    && u.getPlayer() == player
			    && gs.getActionAssignment(u) == null )
			{
				baseBehavior( u, p, pgs );
			}

			// behavior of barracks:
			if( u.getType() == barracksType
			    && u.getPlayer() == player
			    && gs.getActionAssignment(u) == null )
			{
				barracksBehavior( u, p, pgs, gs.getTime() );
			}

			// behavior of melee units:
			if( u.getType().canAttack && !u.getType().canHarvest
			    && u.getPlayer() == player
			    && gs.getActionAssignment(u) == null )
			{
				// BASIC BEHAVIOR
				meleeUnitBehavior_heatmap(u, p, gs);

				// not BASIC BEHAVIOR
				// if( number_melee_units >= 4 )
				//	 meleeUnitBehavior_heatmap(u, p, gs);
				// else
				//	 meleeUnitBehavior(u, p, gs);
			}
		}

		// behavior of workers:
		workersBehavior( workers, p, gs );

		// This method simply takes all the unit actions executed so far, and packages them into a PlayerAction
		return translateActions(player, gs);
	}

	public void baseBehavior( Unit u, Player p, PhysicalGameState pgs )
	{
		int nworkers = 0;
		for( Unit u2 : pgs.getUnits() )
			// BASIC BEHAVIOR
			if( u2.getType().ID == workerType.ID && u2.getPlayer() == p.getID() )
				// not BASIC BEHAVIOR
				//if( u2.getType() == workerType && u2.getPlayer() == p.getID() && u2.getID() != scout_ID )
				nworkers++;

		// BASIC BEHAVIOR
		if( nworkers < 1 && p.getResources() >= workerType.cost )
			train( u, workerType );
		// not BASIC BEHAVIOR
		// train 1 worker for each resource patch, excluding the scout
		// if( nworkers < my_resource_patches && p.getResources() >= workerType.cost )

		// not BASIC BEHAVIOR
		// else if( !scout )
		// {
		//		 train(u, workerType);
		//		 scout = true;
		// }
	}

	public void meleeUnitCommonBehavior( Unit u, Player p, GameState gs, PhysicalGameState pgs, Unit closestEnemy, int closestDistance )
	{
		if( DEBUG )
		{
			UnitAction currentAction = gs.getUnitAction( u );
			System.out.println( "Action: " + currentAction );
		}

		if( u.getType() == rangedType && closestDistance <= 2 )
		{
			// if( DEBUG )
			// 	System.out.println( "RUN" );

			// In case we do not run away
			UnitActionAssignment ua = gs.getUnitActions().get( closestEnemy );
			// if( DEBUG )
			// 	System.out.println( ua );

			if( closestEnemy.getType() == rangedType || ua == null || ( ua.action.getType() != UnitAction.TYPE_MOVE && closestDistance > 1 ) )
				attack( u, closestEnemy );
			else
			{
				// run
				// we compute for each possible position the danger level
				int danger_up = 0;
				int danger_right = 0;
				int danger_down = 0;
				int danger_left = 0;

				for( Unit u2:pgs.getUnits() )
				{
					if( u2.getPlayer() >= 0 && u2.getPlayer() != p.getID() && u2.getType().canAttack )
					{
						//left
						if( u2.getX() == u.getX() - 2 && u2.getY() == u.getY() )
							++danger_right;

						//top-left
						if( u2.getX() == u.getX() - 1 && u2.getY() == u.getY() - 1 )
						{
							++danger_right;
							++danger_up;
						}

						//top
						if( u2.getX() == u.getX() && u2.getY() == u.getY() - 2 )
							++danger_up;

						//top-right
						if( u2.getX() == u.getX() + 1 && u2.getY() == u.getY() - 1 )
						{
							++danger_up;
							++danger_right;
						}

						//right
						if( u2.getX() == u.getX() + 2 && u2.getY() == u.getY() )
							++danger_right;

						//bottom-right
						if( u2.getX() == u.getX() + 1 && u2.getY() == u.getY() + 1 )
						{
							++danger_right;
							++danger_down;
						}

						//bottom
						if( u2.getX() == u.getX() && u2.getY() == u.getY() + 2 )
							++danger_down;

						//bottom-left
						if( u2.getX() == u.getX() - 1 && u2.getY() == u.getY() + 1 )
						{
							++danger_down;
							++danger_left;
						}
					}
				}

				// up
				if( u.getY() - 1 <= 0 || !gs.free( u.getX(), u.getY() - 1 ) )
					danger_up = 10000;

				// right
				if( u.getX() + 1 >= gs.getPhysicalGameState().getWidth() || !gs.free( u.getX() + 1, u.getY() ) ) 
					danger_right = 10000;

				// down
				if( u.getY() + 1 >= gs.getPhysicalGameState().getHeight() || !gs.free( u.getX(), u.getY() + 1 ) )
					danger_down = 10000;

				// left
				if( u.getX() - 1 <= 0 || !gs.free( u.getX() - 1, u.getY() ) )
					danger_left = 10000;

				// We take the safer position
				if( danger_up <= danger_left )
				{
					if( danger_up <= danger_down )
					{
						if( danger_up <= danger_right )
						{
							// move up
							move( u, u.getX(), u.getY() - 1 );
						}
						else
						{
							// move right
							move( u, u.getX() + 1, u.getY() );
						}
					}
					else
					{
						if( danger_down <= danger_right )
						{
							// move down
							move( u, u.getX(), u.getY() + 1 );
						}
						else
						{
							// move right
							move( u, u.getX() + 1, u.getY() );
						}
					}
				}
				else
				{
					if( danger_left <= danger_down )
					{
						if( danger_left <= danger_right )
						{
							// move left
							move( u, u.getX() - 1, u.getY() );
						}
						else
						{
							// move right
							move( u, u.getX() + 1, u.getY() );
						}
					}
					else
					{
						if( danger_down <= danger_right )
						{
							// move down
							move( u, u.getX(), u.getY() + 1 );
						}
						else
						{
							// move right
							move( u, u.getX() + 1, u.getY() );
						}
					}
				}

				// if( DEBUG )
				// {
				// 	System.out.println( "Position : " + u.getX() + ", " + u.getY() );
				// 	System.out.println( "Danger: URDL : " + danger_up + " " + danger_right + " " + danger_down + " " + danger_left );
				// 	if( danger_up <= danger_left )
				// 	{
				// 		if( danger_up <= danger_down )
				// 		{
				// 			if( danger_up <= danger_right )
				// 			{
				// 				// move up
				// 				System.out.println( "Move to : " + u.getX() + " " + ( u.getY() - 1 ) );
				// 			}
				// 			else
				// 			{
				// 				// move right
				// 				System.out.println( "Move to : " + ( u.getX() + 1 ) + " " + u.getY() );
				// 			}
				// 		}
				// 		else
				// 		{
				// 			if( danger_down <= danger_right )
				// 			{
				// 				// move down
				// 				System.out.println( "Move to : " + u.getX() + " " + ( u.getY() + 1 ) );
				// 			}
				// 			else
				// 			{
				// 				// move right
				// 				System.out.println( "Move to : " +( u.getX() + 1 ) + " " + u.getY() ); 
				// 			}
				// 		}
				// 	}
				// 	else
				// 	{
				// 		if( danger_left <= danger_down )
				// 		{
				// 			if( danger_left <= danger_right )
				// 			{
				// 				// move left
				// 				System.out.println( "Move to : " + ( u.getX() - 1 ) + " " + u.getY() );
				// 			}
				// 			else
				// 			{
				// 				// move right
				// 				System.out.println( "Move to : " + ( u.getX() + 1 ) + " " + u.getY() );
				// 			}
				// 		}
				// 		else
				// 		{
				// 			if( danger_down <= danger_right )
				// 			{
				// 				// move down
				// 				System.out.println( "Move to : " + u.getX() + " " + ( u.getY() + 1 ) );
				// 			}
				// 			else
				// 			{
				// 				// move right
				// 				System.out.println( "Move to : " + ( u.getX() + 1 ) + " " + u.getY() ); 
				// 			}
				// 		}
				// 	}

				// 	try
				// 	{
				// 		System.in.read();
				// 	}
				// 	catch( Exception e )
				// 	{
				// 		//TODO: handle exception
				// 	}
				// }
			}
		} //if( u.getType() == rangedType && closestDistance <= 2 )
		else
		{
			UnitAction currentAction = gs.getUnitAction( u );
			if( currentAction != null && currentAction.getType() == UnitAction.TYPE_ATTACK_LOCATION )
			{
				int x = currentAction.getLocationX();
				int y = currentAction.getLocationY();
				int d = Math.abs( x - u.getX() ) + Math.abs( y - u.getY() );

				if( d <= 2 )
				{
					Unit enemy = pgs.getUnitAt( x, y );
					if( enemy != null )
						attack( u, enemy );
					else
						attack( u, closestEnemy );
				}
				else
					attack( u, closestEnemy );
			}
			else
				attack( u, closestEnemy );
		}
	}

	public void meleeUnitBehavior_heatmap( Unit u, Player p, GameState gs )
	{
		PhysicalGameState pgs = gs.getPhysicalGameState();
		Unit closestEnemy = null;
		int closestDistance = 0;

		for( Unit u2 : pgs.getUnits() )
			if( u2.getPlayer() >= 0 && u2.getPlayer() != p.getID() )
			{
				int d = Math.abs( u2.getX() - u.getX() ) + Math.abs( u2.getY() - u.getY() );
				if( closestEnemy == null || d < closestDistance )
				{
					closestEnemy = u2;
					closestDistance = d;
				}
			}

		if( closestEnemy != null )
			meleeUnitCommonBehavior( u, p, gs, pgs, closestEnemy, closestDistance );
		else
			if( gs instanceof PartiallyObservableGameState )
			{
				PartiallyObservableGameState pogs = (PartiallyObservableGameState)gs;
				// there are no enemies, so we need to explore (find the nearest non-observable place):
				int min_x = 0;
				int min_y = 0;
				// closestDistance = -1;
				double heat_point = -10000.0;

				for( int i = 0 ; i < pgs.getWidth() ; ++i )
					for( int j = 0 ; j < pgs.getHeight() ; ++j )
					{
						if( heat_map[i][j] < heat_point || heat_point <= -10000.0 )
						{
							heat_point = heat_map[i][j];
							min_x = i;
							min_y = j;
						}
						else
							if( heat_map[i][j] <= heat_point )
								if( Math.random() <= 0.5 )
								{
									heat_point = heat_map[i][j];
									min_x = i;
									min_y = j;
								}
					}

				if( heat_point > -10000.0 )
				{
					move( u, min_x, min_y );
					if( DEBUG )
						System.out.println("Unit " + u + " moves to (" + min_x + ", " + min_y + ")");
				}
				else
				{
					int rand_x = (int)(pgs.getWidth() * Math.random() );
					int rand_y = (int)(pgs.getHeight() * Math.random() );
					move( u, rand_x, rand_y );
					if( DEBUG )
						System.out.println("Unit " + u + " moves RANDOM to (" + rand_x + ", " + rand_y + ")");
				}

				if( DEBUG )
				{
					UnitAction currentAction = gs.getUnitAction( u );
					System.out.println("Action: " + currentAction );
				}
			}
	}

	public void meleeUnitBehavior( Unit u, Player p, GameState gs )
	{
		PhysicalGameState pgs = gs.getPhysicalGameState();
		Unit closestEnemy = null;
		int closestDistance = 0;

		for( Unit u2 : pgs.getUnits() )
			if( u2.getPlayer() >= 0 && u2.getPlayer() != p.getID() )
			{
				int d = Math.abs( u2.getX() - u.getX() ) + Math.abs( u2.getY() - u.getY() );
				if( closestEnemy == null || d < closestDistance )
				{
					closestEnemy = u2;
					closestDistance = d;
				}
			}

		if( closestEnemy != null )
			meleeUnitCommonBehavior( u, p, gs, pgs, closestEnemy, closestDistance );
		else
		{
			if( gs instanceof PartiallyObservableGameState )
			{
				PartiallyObservableGameState pogs = (PartiallyObservableGameState)gs;
				// there are no enemies, so we need to explore (find the nearest non-observable place):
				int closest_x = 0;
				int closest_y = 0;
				closestDistance = -1;

				for( int i = 0 ; i < pgs.getHeight() ; ++i )
					for( int j = 0 ; j < pgs.getWidth() ; ++j )
						if( !pogs.observable( j, i ) )
						{
							int d = ( u.getX() - j ) * ( u.getX() - j ) + ( u.getY() - i ) * ( u.getY() - i );
							if( closestDistance == -1 || d < closestDistance )
							{
								closest_x = j;
								closest_y = i;
								closestDistance = d;
							}
						}

				if( closestDistance != -1 )
					move( u, closest_x, closest_y );
			}
		}
	}

	private int get_sample( List<Float> distribution, int bypass )
	{
		double rnd = Math.random();
		float sum = 0.f;
		boolean start = false;
		int i = 0;

		while( i < bypass )
		{
			sum += distribution.get( i );
			++i;
		}

		rnd *= ( 1.0 - sum );
		sum =0.f;

		while( sum < rnd )
		{
			sum += distribution.get( i );
			++i;
		}

		return i - 1;
	}

	public void barracksBehavior( Unit u, Player p, PhysicalGameState pgs, int time )
	{
		int enemyWorker = 0;
		int enemyRanged = 0;
		int enemyLight = 0;
		int enemyHeavy = 0;

		int playerHeavy = 0;
		int playerRanged = 0;
		int playerLight = 0;
		int playerWorker = 0;

		int sol_heavy = 0;
		int sol_ranged = 0;
		int sol_light = 0;

		time = time - ( time % 10 );
		if( p.getResources() >= 2 )
		{
			if( INFO )
				System.out.println( "Resources: " + p.getResources() );

			// counts units of each player per type
			for( Unit u2 : pgs.getUnits() )
			{
				if( u2.getPlayer() >= 0 && u2.getPlayer() != p.getID() )
				{
					if( u2.getType().ID == workerType.ID )
						++enemyWorker;
					else if( u2.getType().ID == heavyType.ID )
						++enemyHeavy;
					else if( u2.getType().ID == lightType.ID )
						++enemyLight;
					else if( u2.getType().ID == rangedType.ID )
						++enemyRanged;
				}
				else
					if( u2.getPlayer() >= 0 && u2.getPlayer() == p.getID() )
					{
						if( u2.getType().ID == workerType.ID )
							++playerWorker;
						else if( u2.getType().ID == heavyType.ID )
							++playerHeavy;
						else if( u2.getType().ID == lightType.ID )
							++playerLight;
						else if( u2.getType().ID == rangedType.ID )
							++playerRanged;
					}
			}

			observedWorker =	Math.max( observedWorker, enemyWorker );
			observedHeavy =	 Math.max( observedHeavy, enemyHeavy );
			observedRanged =	Math.max( observedRanged, enemyRanged );
			observedLight =	 Math.max( observedLight, enemyLight );

			if( observedHeavy > 0 || observedLight > 0 || observedRanged > 0 )
				barracks = true;

			if( INFO )
				System.out.println( "Barrack: " + barracks );

			// Draws
			ArrayList<Integer[]> samples = new ArrayList<Integer[]>();
			Double[] info = { 0.0, 0.0, 0.0, 0.0 };

			for( int i = 0 ; i <= nbSamples ; ++i )
			{
				Integer[] tmp = new Integer[4];
				if( barracks )
				{
					tmp[0] = 1;
					tmp[1] = get_sample( (List)distribution_b.get( time ).get( "heavy" ), observedHeavy );
					tmp[2] = get_sample( (List)distribution_b.get( time ).get( "ranged" ), observedRanged );
					tmp[3] = get_sample( (List)distribution_b.get( time ).get( "light" ), observedLight );
				}
				else
				{
					tmp[0] = get_sample( (List)distribution_woutb.get( time ).get( "worker" ), observedWorker );
					tmp[1] = get_sample( (List)distribution_woutb.get( time ).get( "heavy" ), observedHeavy );
					tmp[2] = get_sample( (List)distribution_woutb.get( time ).get( "ranged" ), observedRanged );
					tmp[3] = get_sample( (List)distribution_woutb.get( time ).get( "light" ), observedLight );
				}

				samples.add( tmp );
				info[0] += tmp[0];
				info[1] += tmp[1];
				info[2] += tmp[2];
				info[3] += tmp[3];
			}

			if( INFO )
			{
				System.out.println( "Samples moy = W" + info[0] / nbSamples
				                    + " / H" + info[1] / nbSamples
				                    + " / R" + info[2] / nbSamples
				                    + " / L" + info[3] / nbSamples );
				System.out.println( " Units player(" + ( playerHeavy + playerRanged + playerLight )
				                    + "+" + playerWorker + ") : W" + playerWorker + " / H" + playerHeavy+ " / R" + playerRanged + " / L" + playerLight );
				System.out.println( " Units observed(" + ( observedHeavy + observedRanged + observedLight )
				                    + "+" + observedWorker + ") : W" + observedWorker + " / H" + observedHeavy + " / R"
				                    + observedRanged + " / L" + observedLight );
			}

			// write parameter for solver in a file
			try
			{
				PrintWriter writer = new PrintWriter( "src/ai/poadaptive/data_solver", "UTF-8" );
				for( int i = 0 ; i < nbSamples ; ++i )
					writer.println( samples.get(i)[0] + " " + samples.get(i)[1] + " " + samples.get(i)[2] + " " + samples.get(i)[3] );

				writer.println( playerHeavy );
				writer.println( playerRanged );
				writer.println( playerLight );
				if( p.getResources() <= 2 )
					writer.println( 3 );
				else
					writer.println( p.getResources() );

				writer.close();
			}
			catch( IOException e1 )
			{
				System.out.println( "Exception in printer" );
			}

			// get solutions
			boolean no_train = false;
			try
			{
				// System.out.println("Hello Java");
				Runtime r = Runtime.getRuntime();
				
				if( pgs.getWidth() >= 20 )
					solver_name = solver_path + "solver_cpp_optimistic";
				else
					solver_name = solver_path + "solver_cpp_pessimistic";
					
				Process process = r.exec( String.format( "%s %s %d", solver_name, "src/ai/poadaptive/data_solver", nbSamples ) );
				process.waitFor();

				BufferedReader b = new BufferedReader( new InputStreamReader( process.getInputStream() ) );
				// String line = "";

				// while ((line = b.readLine()) != null) {
				//		 System.out.println(line);
				//		 // System.out.println("LINE");
				// }
				if( INFO )
				{
					System.out.println( "Trace after calling the solver:" );
					System.out.println( b.readLine() );
					System.out.println( b.readLine() );
					System.out.println( b.readLine() );
				}
				else
				{
					b.readLine();
					b.readLine();
					b.readLine();
				}

				sol_heavy = Integer.parseInt( b.readLine() );
				sol_ranged = Integer.parseInt( b.readLine() );
				sol_light = Integer.parseInt( b.readLine() );

				if( INFO )
					System.out.println( "H" + sol_heavy + " R" + sol_ranged + " L" + sol_light + "\n\n" );
				b.close();
			}
			catch( IOException e1 )
			{
				System.out.println( "IO exception in process" );
				System.out.println( e1.getMessage() );
			}
			catch( InterruptedException e2 )
			{
				System.out.println( "interupt exception in process" );
			}
			catch( NumberFormatException e3 )
			{
				no_train = true;
				System.out.println( "No train" );
			}

			if( !no_train )
				if( sol_light >= sol_ranged )
				{
					if( sol_light >= sol_heavy )
						train( u, lightType );
					else
						if( p.getResources() >= heavyType.cost )
							train( u, heavyType );
				}
				else
					if( sol_ranged >= sol_heavy )
						train( u, rangedType );
					else
						if( p.getResources() >= heavyType.cost )
							train( u, heavyType );
		}

		// if (p.getResources() >= lightType.cost) {

	}


	public void workersBehavior( List<Unit> workers, Player p, GameState gs )
	{
		PhysicalGameState pgs = gs.getPhysicalGameState();
		int nbases = 0;
		int nbarracks = 0;

		int resourcesUsed = 0;
		List<Unit> freeWorkers = new LinkedList<Unit>();

		// BASIC BEHAVIOR
		freeWorkers.addAll( workers );

		// not BASIC BEHAVIOR
		// for( Unit w : workers )
		//	 if( w.getID() != scout_ID )
		//		 freeWorkers.add( w );
		//	 else
		//			 meleeUnitBehavior_heatmap(w, p, gs);

		// // if our scout died
		// if( scout && scout_ID != -1 && workers.size() == freeWorkers.size() )
		//	 scout_ID = -1;

		// if( scout && scout_ID == -1 && !freeWorkers.isEmpty() && freeWorkers.get(0) != null )
		// {
		//	 Unit w = freeWorkers.remove(0);
		//	 scout_ID = w.getID();
		//	 meleeUnitBehavior_heatmap(w, p, gs);
		// }

		if( workers.isEmpty() )
			return;

		for( Unit u2 : pgs.getUnits() )
		{
			if( u2.getType() == baseType && u2.getPlayer() == p.getID() )
				nbases++;

			if( u2.getType() == barracksType && u2.getPlayer() == p.getID() )
				nbarracks++;
		}

		List<Integer> reservedPositions = new LinkedList<Integer>();
		if( nbases == 0 && !freeWorkers.isEmpty() )
		{
			// build a base:
			if( p.getResources() >= baseType.cost + resourcesUsed )
			{
				Unit u = freeWorkers.remove( 0 );
				buildIfNotAlreadyBuilding( u, baseType, u.getX(), u.getY(), reservedPositions, p, pgs );
				resourcesUsed += baseType.cost;
			}
		}

		if( nbarracks == 0 )
		{
			// build a barracks:
			if( p.getResources() >= barracksType.cost + resourcesUsed && !freeWorkers.isEmpty() )
			{
				Unit u = freeWorkers.remove( 0 );
				buildIfNotAlreadyBuilding( u, barracksType, u.getX(), u.getY(), reservedPositions, p, pgs );
				resourcesUsed += barracksType.cost;
			}
		}

		// harvest with all the free workers:
		for( Unit u : freeWorkers )
		{
			Unit closestBase = null;
			Unit closestResource = null;
			int closestDistance = 0;

			for( Unit u2 : pgs.getUnits() )
				if( u2.getType().isResource )
				{
					int d = Math.abs( u2.getX() - u.getX() ) + Math.abs( u2.getY() - u.getY() );
					if( closestResource == null || d < closestDistance )
					{
						closestResource = u2;
						closestDistance = d;
					}
				}

			closestDistance = 0;
			for( Unit u2 : pgs.getUnits() )
				if( u2.getType().isStockpile && u2.getPlayer() == p.getID() )
				{
					int d = Math.abs( u2.getX() - u.getX() ) + Math.abs( u2.getY() - u.getY() );
					if( closestBase == null || d < closestDistance )
					{
						closestBase = u2;
						closestDistance = d;
					}
				}

			if( closestResource != null && closestBase != null )
			{
				AbstractAction aa = getAbstractAction( u );
				if( aa instanceof Harvest )
				{
					Harvest h_aa = (Harvest)aa;
					if( h_aa.getTarget() != closestResource || h_aa.getBase() != closestBase )
						harvest( u, closestResource, closestBase );
				}
				else
					harvest( u, closestResource, closestBase );
			}
			// not BASIC behavior
			// explore if no resource around. Remember where were far resources.
		}
	}

	@Override
	public List<ParameterSpecification> getParameters()
	{
		List<ParameterSpecification> parameters = new ArrayList<>();

		parameters.add( new ParameterSpecification( "PathFinding", PathFinding.class, new AStarPathFinding() ) );

		return parameters;
	}
}
