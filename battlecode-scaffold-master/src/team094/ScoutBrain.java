package team094;

import java.util.ArrayList;
import java.util.Random;

import battlecode.common.*;

public class ScoutBrain implements Brain {

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