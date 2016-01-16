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
	private static ArrayList<MapLocation> targets = new ArrayList<MapLocation>();
	private static MapLocation lastArcLoc;
	RobotController rc;

	@Override
	public void run(RobotController inRc) {
		rc = inRc;
		while (true) {
			try {
				runTurnGuard(rc);
				Clock.yield();
			} catch (Exception e) {
				e.printStackTrace();
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
		RobotInfo[] enemiesWithinRange = rc.senseNearbyRobots(myAttackRange, enemyTeam);
		RobotInfo[] zombiesWithinRange = rc.senseNearbyRobots(myAttackRange, Team.ZOMBIE);
		if (enemiesWithinRange.length > 0) {
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
		if (!shouldAttack) {
			Signal[] signals = rc.emptySignalQueue();
			for (Signal hej : signals) {
				MapLocation naw = new MapLocation(hej.getMessage()[0], hej.getMessage()[1]); // TODO:
																								// want
																								// more
																								// efficient
																								// bit
																								// usage
				if (!targets.contains(naw)) {
					targets.add(naw); // TODO: Need someway to remove locations
										// with scout saying that the target is
										// dead.
				}
			}
			MapLocation closest;
			int closestDistance = Integer.MAX_VALUE;
			if (!targets.isEmpty()) {
				for (MapLocation i : targets) {
					if (rc.getLocation().distanceSquaredTo(i) < closestDistance) {
						closest = i;
						closestDistance = rc.getLocation().distanceSquaredTo(i);
					}
				}
			} // TODO: Finish Everything else

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
			RobotInfo away = CompareStuff.moveAwayFrom(zombiesWithinRange, rc.getLocation());
			if (away != null) {
				if (rc.isCoreReady() && rc.canMove(away.location.directionTo(rc.getLocation())))
					Statics.moveTo(CompareStuff.moveAwayFrom(zombiesWithinRange, rc.getLocation()).location
							.directionTo(rc.getLocation()), rc);
			} 
			else if (enemiesWithinRange.length > 0) { // CURRENTLY PRIORITIZES
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
		if (!shouldAttack && rc.isCoreReady()) {

			RobotInfo[] nearby = rc.senseNearbyRobots();
			int numArchons = 0;
			ArrayList<MapLocation> arcLoc = new ArrayList<MapLocation>();
			for (RobotInfo naw : nearby) {
				if (naw.type.equals(RobotType.ARCHON)) {
					arcLoc.add(naw.location);
					numArchons++;
				}
			}
			int nearestArc = 0;
			int shortestDistance = Int.MaxValue();
			if (arcLoc.size() != 0) {
				for (int i = 0; i < arcLoc.size(); i++) {
					int distance = rc.getLocation().distanceSquaredTo(arcLoc.get(i));
					if (distance <= shortestDistance) {
						shortestDistance = distance;
						nearestArc = i;
					}
				}
				Direction dir = arcLoc.get(nearestArc).directionTo(rc.getLocation());
				if (shortestDistance < 4) {
					Statics.moveTo(dir, rc);
				} else if (shortestDistance >= 9) {
					Statics.moveTo(dir.opposite(), rc);
				} else {
					for(int i=0; i<8; i++) {
						if(rc.senseRubble(rc.getLocation().add(Statics.directions[i]))>GameConstants.RUBBLE_SLOW_THRESH){
							rc.clearRubble(Statics.directions[i]);
							return;
						}			
					}
					boolean left = rc
							.canMove(dir.rotateLeft().rotateLeft());
					boolean right = rc
							.canMove(dir.rotateRight().rotateRight());
					if (left && right) {
						if (Math.random() < 0.5) {
							rc.move(dir.rotateLeft().rotateLeft());
						} else {
							rc.move(dir.rotateRight().rotateRight());
						}
					}
					if (left && !right) {
						rc.move(dir.rotateLeft().rotateLeft());
					}
					if (right && !left) {
						rc.move(dir.rotateRight().rotateRight());
					}
				}
			} else {
				for(int i=0; i<8; i++) {
					if(rc.senseRubble(rc.getLocation().add(Statics.directions[i]))>GameConstants.RUBBLE_SLOW_THRESH){
						rc.clearRubble(Statics.directions[i]);
						break;
					}			
				}
				runTurn(rc);
			}
			/*
			 * Signal[] signals = rc.emptySignalQueue(); for (Signal hej :
			 * signals){ if (hej.getMessage()[0] == rc.getID()){ archon =
			 * hej.getLocation(); } } if
			 * (rc.getLocation().distanceSquaredTo(archon) <4) { if
			 * (rc.canMove(archon.directionTo(rc.getLocation()))) {
			 * rc.move(archon.directionTo(rc.getLocation())); } } if
			 * (rc.getLocation().distanceSquaredTo(archon) > 9) { if
			 * (rc.canMove(rc.getLocation().directionTo(archon))) {
			 * rc.move(rc.getLocation().directionTo(archon)); } }
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
			RobotInfo away = CompareStuff.moveAwayFrom(zombiesWithinRange, rc.getLocation());
			if (away != null) {
				if (rc.isCoreReady() && rc.canMove(away.location.directionTo(rc.getLocation())))
					Statics.moveTo(CompareStuff.moveAwayFrom(zombiesWithinRange, rc.getLocation()).location
							.directionTo(rc.getLocation()), rc);
			} else if (enemiesWithinRange.length > 0) {
				// Check if weapon is ready
				if (rc.isWeaponReady()) {
					shouldAttack = true;
					/*
					 * RobotInfo lowestHealth = enemiesWithinRange[0]; RobotInfo
					 * closest = enemiesWithinRange[0]; for (RobotInfo r :
					 * enemiesWithinRange) { if (lowestHealth.health > r.health)
					 * lowestHealth = r; if
					 * (rc.getLocation().distanceSquaredTo(closest.location) >
					 * rc.getLocation() .distanceSquaredTo(r.location)) closest
					 * = r; } rc.attackLocation(closest.location);
					 */
					rc.attackLocation(CompareStuff.soldierCompare(enemiesWithinRange).location);
				}
			} else if (zombiesWithinRange.length > 0) {
				// Check if weapon is ready
				if (rc.isWeaponReady()) {
					shouldAttack = true;
					/*
					 * RobotInfo lowestHealth = zombiesWithinRange[0]; RobotInfo
					 * closest = zombiesWithinRange[0]; for (RobotInfo r :
					 * zombiesWithinRange) { if (lowestHealth.health > r.health)
					 * lowestHealth = r; if
					 * (rc.getLocation().distanceSquaredTo(closest.location) >
					 * rc.getLocation() .distanceSquaredTo(r.location)) closest
					 * = r; } rc.attackLocation(closest.location);
					 */
					rc.attackLocation(CompareStuff.soldierCompare(zombiesWithinRange).location);
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
}