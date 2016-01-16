package team094;

import battlecode.common.Clock;
import battlecode.common.RobotController;

public class ViperBrain {
	
	
	
	public void initialize(RobotController rc) {

	}

	public void runTurn(RobotController rc) {

	}

	public void run(RobotController rc) {
		try {
			initialize(rc);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		while(true){
			Clock.yield();
			try{
				runTurn(rc);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}
