package team094;

import battlecode.common.*;

public class RobotPlayer {

	private static Brain brain;

	/**
	 * run() is the method that is called when a robot is instantiated in the
	 * Battlecode world. If this method returns, the robot dies! @throws
	 **/

	public static void run(RobotController rc) {
		Statics.referenceLocation = Statics.com(rc.getInitialArchonLocations(rc.getTeam()));
		
		switch (rc.getType()) {
		case ARCHON:
			brain = new ArchonBrain();
			break;

		case SOLDIER:
			brain = new SoldierBrain();
			break;

		case GUARD:
			brain = new SoldierBrain();
			break;
		
		case SCOUT:
			brain = new ScoutBrain();
			break;
		
		case VIPER:
			brain = new ViperBrain();
			break;
			
		case TURRET:
			brain = new TurretBrain();
			break;
			
			
		default:
			break;
		}

		brain.run(rc);
	}
	
}
