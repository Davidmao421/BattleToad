package team094old;

import java.util.LinkedList;

import battlecode.common.*;
import scala.Int;

public class GuardBrain implements Brain {
	private static Direction[] directions = { Direction.NORTH, Direction.NORTH_EAST, Direction.EAST,
			Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST };
	private static RobotType[] robotTypes = { RobotType.SCOUT, RobotType.SOLDIER, RobotType.SOLDIER, RobotType.SOLDIER,
			RobotType.GUARD, RobotType.GUARD, RobotType.VIPER, RobotType.TURRET };
	private static MapLocation archon = new MapLocation(0,0);
	@Override
	public void run(RobotController rc) {
		intialize(rc);

		while (true) {
			Clock.yield();
			try {
				runTurn(rc);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void intialize(RobotController rc) {

	}

	public static void runTurn(RobotController rc) throws GameActionException {
		int myAttackRange = rc.getType().attackRadiusSquared;
		Team myTeam = rc.getTeam();
		Team enemyTeam = myTeam.opponent();

		boolean shouldAttack = false;
		// If this robot type can attack, check for enemies within range and
		// attack one
		if(!rc.isCoreReady())
		{
			return;
		}
		if (rc.getType().canAttack() && myAttackRange > 0) {
			RobotInfo[] enemiesWithinRange = rc.senseNearbyRobots(myAttackRange, enemyTeam);
			RobotInfo[] zombiesWithinRange = rc.senseNearbyRobots(myAttackRange, Team.ZOMBIE);
			if (enemiesWithinRange.length > 0) { // CURRENTLY PRIORITIZES
													// MACHINES (PROBABLY WANT
													// ZOMBIES BECAUSE THEY
													// DOUBLE DAMAGE)
				shouldAttack = true;
				// Check if weapon is ready
				if (rc.isWeaponReady()) {
					rc.attackLocation(enemiesWithinRange[0].location);
				}
			} else if (zombiesWithinRange.length > 0) {
				shouldAttack = true;
				// Check if weapon is ready
				if (rc.isWeaponReady()) {
					rc.attackLocation(zombiesWithinRange[0].location);
				}
			}
		}
		if (!shouldAttack) {
			
			RobotInfo[] nearby = rc.senseNearbyRobots();
			int numArchons = 0;
			LinkedList<MapLocation> arcLoc = new LinkedList<MapLocation>();
			for (RobotInfo naw : nearby) {
				if (naw.type.equals(RobotType.ARCHON)) {
					arcLoc.add(naw.location);
					numArchons++;
				}
			}
			int nearestArc = 0;
			int shortestDistance = Int.MaxValue();
			for (int i = 0; i < arcLoc.size(); i++) {
				int distance = rc.getLocation().distanceSquaredTo(arcLoc.get(i));
				if (distance <= shortestDistance) {
					shortestDistance = distance;
					nearestArc = i;
				}
			}
			if (shortestDistance < 4 ){
				if (rc.canMove(arcLoc.get(nearestArc).directionTo(rc.getLocation()))) {
					rc.move(arcLoc.get(nearestArc).directionTo(rc.getLocation()));
				}
				else{
					int rand = (int)(2 * Math.random());
					boolean left = rc.canMove(arcLoc.get(nearestArc).directionTo(rc.getLocation()).rotateLeft());
					boolean right = rc.canMove((arcLoc.get(nearestArc).directionTo(rc.getLocation()).rotateRight()));
					if (left && right){
						if (rand ==1){
							rc.move(arcLoc.get(nearestArc).directionTo(rc.getLocation()).rotateLeft());
						}
						if (rand == 2){
							rc.move(arcLoc.get(nearestArc).directionTo(rc.getLocation()).rotateRight());
						}
					}
					else if (left && !right){
						rc.move(arcLoc.get(nearestArc).directionTo(rc.getLocation()).rotateLeft());
					}
					else if (right && !left){
						rc.move(arcLoc.get(nearestArc).directionTo(rc.getLocation()).rotateRight());
					}
				}
			}
			else if (shortestDistance >= 9) {
				if (rc.canMove(rc.getLocation().directionTo(arcLoc.get(nearestArc)))) {
					rc.move(rc.getLocation().directionTo(arcLoc.get(nearestArc)));
				}
				else{
					int rand = (int)(2 * Math.random());
					boolean left = rc.canMove(rc.getLocation().directionTo(arcLoc.get(nearestArc)).rotateLeft());
					boolean right = rc.canMove(rc.getLocation().directionTo(arcLoc.get(nearestArc)).rotateRight());
					if (left && right){
						if (rand ==1){
							rc.move(rc.getLocation().directionTo(arcLoc.get(nearestArc)).rotateLeft());
						}
						if (rand == 2){
							rc.move(rc.getLocation().directionTo(arcLoc.get(nearestArc)).rotateRight());
						}
					}
					else if (left && !right){
						rc.move(rc.getLocation().directionTo(arcLoc.get(nearestArc)).rotateLeft());
					}
					else if (right && !left){
						rc.move(rc.getLocation().directionTo(arcLoc.get(nearestArc)).rotateRight());
					}
				}
			}
			else {
				for (Direction x : Statics.directions)
					if (rc.canMove(x)) {
						rc.move(x);
						break;
					}
			}
	/*		Signal[] signals = rc.emptySignalQueue();
			for (Signal hej : signals){
				if (hej.getMessage()[0] == rc.getID()){
					archon = hej.getLocation();
				}
			}
			if (rc.getLocation().distanceSquaredTo(archon) <4) {
				if (rc.canMove(archon.directionTo(rc.getLocation()))) {
					rc.move(archon.directionTo(rc.getLocation())); 
				}
			}
			if (rc.getLocation().distanceSquaredTo(archon) > 9) {
				if (rc.canMove(rc.getLocation().directionTo(archon))) {
					rc.move(rc.getLocation().directionTo(archon));
				}
			}
			*/
		}
	}

}