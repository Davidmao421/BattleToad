package simpleplayer;

import java.util.*;
import battlecode.common.*;

public class ArchonBrain implements Brain {
// still doesn't account for own location
	private boolean firstRun = true;
	private Map<Integer, MapLocation> archonStarts = new HashMap<>(6);
	private RobotController rc;
	private static Direction[] directions = { Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
			Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST };
	private static RobotType[] robotTypes = { RobotType.SCOUT, RobotType.SOLDIER, RobotType.SOLDIER, RobotType.SOLDIER,
			RobotType.GUARD, RobotType.GUARD, RobotType.VIPER, RobotType.TURRET };
	private static int myAttackRange = 24;
	private static int sightRange = 35;
	private MapLocation com(Collection<MapLocation> locs){
		int x = rc.getLocation().x;
		int y = rc.getLocation().y;
		for (MapLocation loc : locs){
			x+=loc.x;
			y+=loc.y;
		}
		if (locs.size() != 0){
			return new MapLocation(x/(locs.size()+1), y/(locs.size()+1));
		}
		else {
			return null;
		}
	}
	
	@Override
	public void run(RobotController rcI) {
		rc=rcI;
		
		MapLocation start = rc.getLocation();
		
		try {
			rc.broadcastSignal( 20000);//2*sightRange ); //maximize broadcast range without costing extra stuff
			Clock.yield();
			
			Signal[] signals = rc.emptySignalQueue();
			for (Signal s : signals)
				archonStarts.put(s.getID(), s.getLocation());
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}

		while (true) {
			try {
				//rc.broadcastSignal(sightRange * 2); // free broadcasting ( should add to everything )
				Signal[] signals = rc.emptySignalQueue();
				int numGuards = 0;
				for (Signal naw: signals){
					//if (naw.){ //type = guard
						//numGuards +=1;
						// IDK HOW TO GET ROBOT TYPE FROM SIGNALS
					//}
				}
				MapLocation com = com(archonStarts.values());
				if (com.distanceSquaredTo(rc.getLocation()) <= 4) {
					for (int i = 0; i< 8 ; i++){
					if (numGuards <= 4 && rc.canBuild(directions[i], RobotType.GUARD)){
						rc.build(directions[i], RobotType.GUARD);
					}
					if (rc.canBuild(Direction.SOUTH, RobotType.SOLDIER)){ // change it so the person can build more efficiently. Currently ONLY south
						rc.build(directions[i], RobotType.SOLDIER);
					}
					}
					
				}
				else {
					int index=0;
					for (int i = 0; i < 8;i++){
						if (directions[i].equals(rc.getLocation().directionTo(com))){
							index = i;
						}
					}
					if (rc.canMove(rc.getLocation().directionTo(com))){
						rc.move(rc.getLocation().directionTo(com));
					}
					else if (rc.canMove(directions[(index+1)%8])){
						rc.move(directions[(index+1)%8]);
					}
					else{
						rc.move(directions[(index-1)%8]);
					}
					
				}
				Clock.yield();
			} catch (Exception e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}
	}

}


/*				int fate = rand.nextInt(1000);
				// Check if this ARCHON's core is ready
				if (fate % 10 == 2) {
					// Send a message signal containing the data (6370, 6147)
					rc.broadcastMessageSignal(6370, 6147, 80);
				}
				Signal[] signals = rc.emptySignalQueue();
				if (signals.length > 0) {
					// Set an indicator string that can be viewed in the client
					rc.setIndicatorString(0, "I received a signal this turn!");
				} else {
					rc.setIndicatorString(0, "I don't any signal buddies");
				}
				if (rc.isCoreReady()) {
					if (fate < 800) {
						// Choose a random direction to try to move in
						Direction dirToMove = directions[fate % 8];
						// Check the rubble in that direction
						if (rc.senseRubble(
								rc.getLocation().add(dirToMove)) >= GameConstants.RUBBLE_OBSTRUCTION_THRESH) {
							// Too much rubble, so I should clear it
							rc.clearRubble(dirToMove);
							// Check if I can move in this direction
						} else if (rc.canMove(dirToMove)) {
							// Move
							rc.move(dirToMove);
						}
					} else {
						// Choose a random unit to build
						RobotType typeToBuild = robotTypes[fate % 8];
						// Check for sufficient parts
						if (rc.hasBuildRequirements(typeToBuild)) {
							// Choose a random direction to try to build in
							Direction dirToBuild = directions[rand.nextInt(8)];
							for (int i = 0; i < 8; i++) {
								// If possible, build in this direction
								if (rc.canBuild(dirToBuild, typeToBuild)) {
									rc.build(dirToBuild, typeToBuild);
									break;
								} else {
									// Rotate the direction to try
									dirToBuild = dirToBuild.rotateLeft();
								}
							}
						}
					}
				}*/
