package team094;

import java.util.ArrayList;
import java.util.LinkedList;

import battlecode.common.*;
import battlecode.instrumenter.inject.System;
import scala.Int;

public class SoldierBrain implements Brain {

	private static ArrayList<MapLocation> past = new ArrayList<MapLocation>();
	private static Direction[] directions = { Direction.NORTH, Direction.NORTH_EAST, Direction.EAST,
			Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST };
	private static ArrayList <MapLocation> targets = new ArrayList<MapLocation> ();
	@Override
	public void run(RobotController rc) {
		intialize(rc);
		if (Math.random()*10 < 5){
			while (true) {
				try{
					runTurnGuard(rc);
					Clock.yield();
				}catch(Exception e){
					e.printStackTrace();
				}
		}
		}
		else{
			while (true) {
				try{
					runTurn(rc);
					Clock.yield();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}

	public void intialize(RobotController rc) {
	}
	public static void runTurnPack(RobotController rc) throws GameActionException {
		int myAttackRange = rc.getType().attackRadiusSquared;
		Team myTeam = rc.getTeam();
		Team enemyTeam = myTeam.opponent();

		boolean shouldAttack = false;
		// If this robot type can attack, check for enemies within range and
		// attack one
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
		if (!shouldAttack){
			Signal[] signals = rc.emptySignalQueue();
			for (Signal hej: signals){
				MapLocation naw = new MapLocation (hej.getMessage()[0],hej.getMessage()[1]); // TODO: want more efficient bit usage
				if (!targets.contains(naw)){
					targets.add(naw); // TODO: Need someway to remove locations with scout saying that the target is dead.
				}
			}
			MapLocation closest;
			int closestDistance = Integer.MAX_VALUE;
			if (!targets.isEmpty()){
				for (MapLocation i: targets){
					if (rc.getLocation().distanceSquaredTo(i) < closestDistance){
						closest = i;
						closestDistance = rc.getLocation().distanceSquaredTo(i);
					}
				}
			}
			

		}
	}
	
	
	public static void runTurnGuard(RobotController rc) throws GameActionException {
		int myAttackRange = rc.getType().attackRadiusSquared;
		Team myTeam = rc.getTeam();
		Team enemyTeam = myTeam.opponent();

		boolean shouldAttack = false;
		// If this robot type can attack, check for enemies within range and
		// attack one
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

	public static void runTurn(RobotController rc) throws GameActionException {
		int myAttackRange = rc.getType().attackRadiusSquared;
		Team myTeam = rc.getTeam();
		Team enemyTeam = myTeam.opponent();

		boolean shouldAttack = false;
		// If this robot type can attack, check for enemies within range and
		// attack one
		if (rc.getType().canAttack() && myAttackRange > 0) {
			RobotInfo[] enemiesWithinRange = rc.senseNearbyRobots(myAttackRange, enemyTeam);
			RobotInfo[] zombiesWithinRange = rc.senseNearbyRobots(myAttackRange, Team.ZOMBIE);
			if (enemiesWithinRange.length > 0) {
				shouldAttack = true;
				// Check if weapon is ready
				if (rc.isWeaponReady()) {
					RobotInfo lowestHealth = enemiesWithinRange[0];
					RobotInfo closest = enemiesWithinRange[0];
					for (RobotInfo r : enemiesWithinRange) {
						if (lowestHealth.health > r.health)
							lowestHealth = r;
						if (rc.getLocation().distanceSquaredTo(closest.location) > rc.getLocation()
								.distanceSquaredTo(r.location))
							closest = r;
					}
					rc.attackLocation(closest.location); // Attacks closest
															// enemy, change to
															// incorporate
															// signals
				}
			} else if (zombiesWithinRange.length > 0) {
				shouldAttack = true;
				// Check if weapon is ready
				if (rc.isWeaponReady()) {
					RobotInfo lowestHealth = zombiesWithinRange[0];
					RobotInfo closest = zombiesWithinRange[0];
					for (RobotInfo r : zombiesWithinRange) {
						if (lowestHealth.health > r.health)
							lowestHealth = r;
						if (rc.getLocation().distanceSquaredTo(closest.location) > rc.getLocation()
								.distanceSquaredTo(r.location))
							closest = r;
					}
					rc.attackLocation(closest.location); // Attacks closest
															// enemy, change to
															// incorporate
															// signals
				}
			}
		}
		if (!shouldAttack) {
			if (rc.isCoreReady()) {
				Boolean move = false;
				Direction lowest = directions[0];
				double L = rc.senseRubble(rc.getLocation().add(directions[0]));
				for (int i = (int) (directions.length * Math.random()); i < i + directions.length; i++) {
					Direction d = directions[i % 8];
					MapLocation newLoc = rc.getLocation().add(d);
					if (rc.canMove(d) && rc.senseRubble(newLoc) < GameConstants.RUBBLE_OBSTRUCTION_THRESH) {
						if (past.contains(newLoc) == false) {
							move = true;
							rc.move(d);
							past.add(newLoc);
							if (past.size() > 20) {
								past.remove(0);
							}
							break;
						}
					} else {
						if (rc.senseRubble(rc.getLocation().add(d)) < L) {
							lowest = d;
						}
					}
				}
				if (!move) {
					rc.clearRubble(lowest);
				}

			}
		}

	}

}
