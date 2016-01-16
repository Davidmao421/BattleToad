package team094;

import java.util.Random;

import battlecode.common.*;

public class TurretBrain implements Brain{
	static RobotController rc;
	@Override
	public void run(RobotController rcI) {
		rc=rcI;
		
        try {
            //add beginning of game code
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        while (true) {
            // This is a loop to prevent the run() method from returning. Because of the Clock.yield()
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
		Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
                Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
        RobotType[] robotTypes = {RobotType.SCOUT, RobotType.SOLDIER, RobotType.SOLDIER, RobotType.SOLDIER,
                RobotType.GUARD, RobotType.GUARD, RobotType.VIPER, RobotType.TURRET};
        int myAttackRange = 0;
        Team myTeam = rc.getTeam();
        Team enemyTeam = myTeam.opponent();
		if (rc.isWeaponReady()) {
            RobotInfo[] enemiesWithinRange = rc.senseNearbyRobots(myAttackRange, enemyTeam);
            RobotInfo[] zombiesWithinRange = rc.senseNearbyRobots(myAttackRange, Team.ZOMBIE);
            Signal[] signals = rc.emptySignalQueue();
            for(Signal s: signals) {
            	//TODO: process signals to get possible attack locations and store in array
            	RobotInfo[] robots = null; //replace with actual signal stuff
            }
            
            if (enemiesWithinRange.length > 0) {
                for (RobotInfo enemy : enemiesWithinRange) {
                	
                    // Check whether the enemy is in a valid attack range (turrets have a minimum range)
                    if (rc.canAttackLocation(enemy.location)) {
                        rc.attackLocation(enemy.location);
                        break;
                    }
                }
            } else if (zombiesWithinRange.length > 0) {
                for (RobotInfo zombie : zombiesWithinRange) {
                    if (rc.canAttackLocation(zombie.location)) {
                        rc.attackLocation(zombie.location);
                        break;
                    }
                }
            }
        }
	}
	public static boolean travelTo(MapLocation loc) throws GameActionException{
		if(rc.getType()!=RobotType.TTM) {
			rc.pack();
			return false;
		}
		else if(rc.getLocation().distanceSquaredTo(loc)<=2) {//TODO: need to make smart 
			rc.unpack();
			return true;
		}
		else {
			Statics.navigateTo(loc, rc);
			return false;
		}
	}

}
