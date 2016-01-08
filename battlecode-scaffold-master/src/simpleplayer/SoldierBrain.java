package simpleplayer;

import java.util.ArrayList;
import java.util.Random;

import battlecode.common.*;

public class SoldierBrain implements Brain {

	static RobotController rc;
	static ArrayList<MapLocation> past = new ArrayList<MapLocation>();

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
		
		
		Direction[] directions = { Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
				Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST };
		RobotType[] robotTypes = { RobotType.SCOUT, RobotType.SOLDIER, RobotType.SOLDIER, RobotType.SOLDIER,
				RobotType.GUARD, RobotType.GUARD, RobotType.VIPER, RobotType.TURRET };
		int myAttackRange = rc.getType().attackRadiusSquared;
		Team myTeam = rc.getTeam();
		Team enemyTeam = myTeam.opponent();
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
			if (rc.isCoreReady()) {
				Boolean move = false;
				Direction lowest = directions[0];
				double L = rc.senseRubble(rc.getLocation().add(directions[0]));
				for (int i = 0; i < 8; i++) {
					Direction d = directions[i]; // prioritizes NORTH atm; either make it more random or take in data
					MapLocation newLoc = rc.getLocation().add(d);
					if (rc.canMove(d)
							&& rc.senseRubble(newLoc) < GameConstants.RUBBLE_OBSTRUCTION_THRESH) {
						if (past.contains(newLoc)==false) {
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
