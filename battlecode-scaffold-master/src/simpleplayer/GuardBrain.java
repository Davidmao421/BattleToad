package simpleplayer;

import battlecode.common.*;

public class GuardBrain implements Brain {
	private static RobotController rc;
	private static Direction[] directions = { Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
			Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST };
	private static RobotType[] robotTypes = { RobotType.SCOUT, RobotType.SOLDIER, RobotType.SOLDIER, RobotType.SOLDIER,
			RobotType.GUARD, RobotType.GUARD, RobotType.VIPER, RobotType.TURRET };
	private static int myAttackRange = rc.getType().attackRadiusSquared;
	private static Team myTeam = rc.getTeam();
	private static Team enemyTeam = myTeam.opponent();
	@Override
	public void run(RobotController rcI) {
		rc=rcI;
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
	public static void runTurn() throws GameActionException
	{
		MapLocation loc = rc.getLocation();
		
		MapLocation init = rc.getLocation();
		boolean shouldAttack = false;
		// If this robot type can attack, check for enemies within range and
		// attack one
		if (rc.getType().canAttack() && myAttackRange > 0) {
			RobotInfo[] enemiesWithinRange = rc.senseNearbyRobots(myAttackRange, enemyTeam);
			RobotInfo[] zombiesWithinRange = rc.senseNearbyRobots(myAttackRange, Team.ZOMBIE);
			if (enemiesWithinRange.length > 0) { // CURRENTLY PRIORITIZES MACHINES (PROBABLY WANT ZOMBIES BECAUSE THEY DOUBLE DAMAGE)
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
		}
	}
	
}