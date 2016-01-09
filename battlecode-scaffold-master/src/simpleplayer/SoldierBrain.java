package simpleplayer;

import java.util.ArrayList;
import java.util.Random;

import battlecode.common.*;

public class SoldierBrain implements Brain {

	private static RobotController rc;
	private static ArrayList<MapLocation> past = new ArrayList<MapLocation>();
	private static Direction[] directions = { Direction.NORTH, Direction.NORTH_EAST, Direction.EAST,
			Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST };
	private static RobotType[] robotTypes = { RobotType.SCOUT, RobotType.SOLDIER, RobotType.SOLDIER, RobotType.SOLDIER,
			RobotType.GUARD, RobotType.GUARD, RobotType.VIPER, RobotType.TURRET };
	private static int myAttackRange = rc.getType().attackRadiusSquared;
	private static Team myTeam = rc.getTeam();
	private static Team enemyTeam = myTeam.opponent();

	@Override
	public void run(RobotController rcI) {
		rc = rcI;
		try {
			// Any code here gets executed exactly once at the beginning of the
			// game.
		} catch (Exception e) {
			// Throwing an uncaught exception makes the robot die, so we need to
			// catch exceptions.
			// Caught exceptions will result in a bytecode penalty.
			System.out.println(e.getMessage());
			e.printStackTrace();
		}

		while (true) {
			// This is a loop to prevent the run() method from returning.
			// Because of the Clock.yield()
			// at the end of it, the loop will iterate once per game round.
			try {
				runTurn();
				Clock.yield();
			} catch (Exception e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}
	}

	public static void runTurn() throws GameActionException {
		MapLocation init = rc.getLocation();
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
					RobotInfo lowestHealth=enemiesWithinRange[0];
					RobotInfo closest=enemiesWithinRange[0];
					for(RobotInfo r: enemiesWithinRange)
					{
						if(lowestHealth.health>r.health)
							lowestHealth=r;
						if(rc.getLocation().distanceSquaredTo(closest.location)>rc.getLocation().distanceSquaredTo(r.location))
							 closest=r;
					}
					rc.attackLocation(closest.location); //Attacks closest enemy, change to incorporate signals
				}
			} else if (zombiesWithinRange.length > 0) {
				shouldAttack = true;
				// Check if weapon is ready
				if (rc.isWeaponReady()) {
					RobotInfo lowestHealth=zombiesWithinRange[0];
					RobotInfo closest=zombiesWithinRange[0];
					for(RobotInfo r: zombiesWithinRange)
					{
						if(lowestHealth.health>r.health)
							lowestHealth=r;
						if(rc.getLocation().distanceSquaredTo(closest.location)>rc.getLocation().distanceSquaredTo(r.location))
							 closest=r;
					}
					rc.attackLocation(closest.location);  //Attacks closest enemy, change to incorporate signals
				}
			}
		}
		if (!shouldAttack) {
			if (rc.isCoreReady()) {
				Boolean move = false;
				Direction lowest = directions[0];
				double L = rc.senseRubble(rc.getLocation().add(directions[0]));
				for (int i = (int)( directions.length*Math.random()); i < i+directions.length; i++) {
					Direction d = directions[i%8]; 
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
