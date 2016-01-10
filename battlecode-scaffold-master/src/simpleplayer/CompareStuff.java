package simpleplayer;

import java.util.*;

import battlecode.common.*;

	public class CompareStuff{	
		public static RobotInfo soldierCompare(RobotInfo[] enemies, RobotInfo[] zombies)
		{
			ArrayList<RobotInfo> allDZ = new ArrayList<RobotInfo>();
			RobotInfo DZ = zombies[0];
			for(RobotInfo i: zombies){
				if(i.attackPower<DZ.attackPower){
					allDZ.clear();;
					DZ=i;
				}
				else if(i.attackPower==DZ.attackPower){
					allDZ.add(i);
				}
			}
			RobotInfo HZ = allDZ.get(0);
			for(RobotInfo i: allDZ)
			{
				if(i.health<HZ.health)
					HZ=i;
			}
			if (allDZ.size()==1)
			{
				return allDZ.get(0);
			}
			else {
				ArrayList<RobotInfo> allDE = new ArrayList<RobotInfo>();
				RobotInfo DE = enemies[0];
				for(RobotInfo i: enemies){
					if(i.attackPower<DZ.attackPower){
						allDE.clear();;
						DE=i;
					}
					else if(i.attackPower==DE.attackPower){
						allDE.add(i);
					}
				}
				/*RobotInfo HE = allDE.get(0);   //if we want to do stuff with the lowest health enemy
				for(RobotInfo i: allDE)
				{
					if(i.health<HE.health)
						HE=i;
				}*/
				if (allDE.size()==1&&DE.attackPower>DZ.attackPower) //only attacks enemy if its stronger than zombie
				{
					return allDE.get(0);
				}
				else {
					return DZ;
				}
			}
		}
		
}