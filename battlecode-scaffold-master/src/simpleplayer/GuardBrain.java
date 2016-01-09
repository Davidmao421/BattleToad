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
		
	}
	
}