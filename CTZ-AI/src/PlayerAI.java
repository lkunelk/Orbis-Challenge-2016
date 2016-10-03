/* Nam
 * October 1, 2016
 * 
 * 2016 Orbis Challenge
 */

import com.orbischallenge.ctz.objects.ControlPoint;
import com.orbischallenge.ctz.objects.EnemyUnit;
import com.orbischallenge.ctz.objects.FriendlyUnit;
import com.orbischallenge.ctz.objects.Pickup;
import com.orbischallenge.ctz.objects.World;
import com.orbischallenge.ctz.objects.enums.ActivateShieldResult;
import com.orbischallenge.ctz.objects.enums.Direction;
import com.orbischallenge.ctz.objects.enums.PickupResult;
import com.orbischallenge.ctz.objects.enums.PickupType;
import com.orbischallenge.ctz.objects.enums.ShotResult;
import com.orbischallenge.ctz.objects.enums.Team;
import com.orbischallenge.ctz.objects.enums.TileType;
import com.orbischallenge.game.engine.Point;

public class PlayerAI
{
	Hero[] heroes; //stores our units for global access
	Team team; //our team
	
	PickupType[] pickups = {
			PickupType.WEAPON_MINI_BLASTER,
			PickupType.WEAPON_SCATTER_GUN,
			PickupType.WEAPON_LASER_RIFLE,
			PickupType.WEAPON_RAIL_GUN,
			PickupType.SHIELD,
			PickupType.REPAIR_KIT,
	};
	
	//instatiates heroes, calculates weights for each objective and assigns tasks to heroes 
	public void doMove(World world, EnemyUnit[] EU, FriendlyUnit[] FU){
		
		//Instantiate heroes
		if(heroes == null){
			team = FU[0].getTeam();
			heroes = new Hero[4];
			for(int i = 0; i < 4; i++)heroes[i] = new Hero(world, EU, FU[i], i);
		}
		else{ //update heroes positions, and other info
			for(int i = 0; i < 4; i++) heroes[i].update(FU[i], EU);
		}
		
		Pickup[] P = world.getPickups();
		ControlPoint[] C = world.getControlPoints();
		
		int[][] objP = null; // objectives for collecting pickups
		int[][] objC = null; // objectives for capturing control points
		
		//evaluate the cost for each pickup
		if(P.length > 0){
			objP = new int[heroes.length][P.length];
			
			for(int h = 0; h < heroes.length; h++)
			{
				for(int p = 0; p < P.length; p++)
				{
					int x = heroes[h].getPosition().getX() -  P[p].getPosition().getX();
					int y = heroes[h].getPosition().getY() -  P[p].getPosition().getY();
					int dist = x*x + y*y;
					int value = getPickupValue(P[p].getPickupType(), heroes[h]);
					objP[h][p] = f(dist, value);
				}
			}
		}
		
		//evaluate cost of the control points
		if(C.length > 0){
				objC = new int[heroes.length][C.length];
				
				for(int h = 0; h < heroes.length; h++){
						for(int c = 0; c < C.length; c++){
								int dist = world.getPathLength(heroes[h].getPosition(), C[c].getPosition());
								int value = getControlPointValue(C[c], heroes[h]);
								objC[h][c] = f(dist, value);
						}
				}
		}
		
		Point[] obj = new Point[heroes.length];
		
		//assign objectives to heroes
		int[] lowest = new int[heroes.length];
		for(int i = 0; i < heroes.length; i++)lowest[i] = 999999999;
		
		if(P.length > 0){
			int[] takenP = new int[P.length];
			for(int h = 0; h < heroes.length; h++){
				
				Point objective = null;
				int n = 0;
				
				for(int p = 0; p < P.length; p++)
				{
					if(objP[h][p] < lowest[h] && takenP[p]<2)
					{
						n = p;
						lowest[h] = objP[h][p];
						objective = P[p].getPosition();
						
					}
				}
				
				if(objective!=null){
					obj[h] = objective;
					heroes[h].setObjective(objective);
				}
				takenP[n]++;
			}
		}
		
		if(C.length > 0){
			int[] takenC = new int[C.length];
			for(int h = 0; h < heroes.length; h++){
				
				Point objective = null;
				int n = 0;
				
				for(int c = 0; c < C.length; c++)
				{
					if(objC[h][c] < lowest[h] && takenC[c]<4)
					{
						n = c;
						lowest[h] = objC[h][c];
						objective = C[c].getPosition();
						
					}
				}
				
				if(objective!=null){
					obj[h] = objective;
					heroes[h].setObjective(objective);
				}
				takenC[n]++;
			}
		}
		
		EnemyUnit[] e = null;
		
		int x = 0, y = 0;
		while(world.isWithinBounds(new Point(x,y)))
			y++;
		while(world.isWithinBounds(new Point(x,y-1)))
			x++;
		
		System.out.println(x+" - "+y);
		
		//in case of battle
		for(int h = 0; h < heroes.length; h++){
			if(FU[heroes[h].getUnit()].getDamageTakenLastTurn() > 0){
				e = FU[heroes[h].getUnit()].getEnemiesWhoShotMeLastTurn();
				
				for(int i = 0; i < e.length; i++){
					int ex = e[i].getPosition().getX(), ey = e[i].getPosition().getY();
					
					
					for(int c = 0; c < heroes.length; c++){
						int shortest = 5;
						Point goal = null;
						for(int a = 0; a < x; a++){
							for(int b = 0; b < y; b++){
								Point curr = new Point(a,b);
								if((Math.abs(ex - a) == Math.abs(ey-b) || ex - a == 0 || ey - b == 0)
										&& inRange(world, curr, e[i].getPosition(), FU[heroes[c].getUnit()].getCurrentWeapon().getRange(), FU )){
									
									int path = world.getPathLength(heroes[c].getPosition(), new Point(a,b));
									if(path < shortest){
											goal = curr;
											shortest = path;
									}
								}
							}
						}
						if(goal!=null){
							heroes[c].setObjective(goal);
							System.out.println(heroes[c].getPosition()+"hit");
							System.out.println(heroes[c].getPosition()+" "+goal.toString());
						}
					}
				}
			}
		}
		
		//System.out.println(inRange(world, new Point(1,4), new Point(1,1)));
		
		for(int c = 0; c < heroes.length; c++)
			heroes[c].act();
		
	}
	
	public boolean inRange(World world, Point a, Point b, int gunRange,FriendlyUnit[] FU){
		int dx = 0, dy = 0;
		if(b.getX() != a.getX()) dx = (b.getX() - a.getX())/Math.abs(b.getX() - a.getX());
		if(b.getY() != a.getY()) dy = (b.getY() - a.getY())/Math.abs(b.getY() - a.getY());
		
		int x = a.getX() , y = a.getY();
		
		if(Math.abs(x-b.getX()) > gunRange || Math.abs(y-b.getY()) > gunRange)return false;
		for(int i = 0; i < 4; i++)if(FU[i].getPosition().equals(a))return false;
		
		while(x!=b.getX() || y!=b.getY()){
			if(world.getTile(new Point(x,y)) == TileType.WALL)return false;
			x+=dx;
			y+=dy;
		}
		return true;
	}
	
	//function for calculating importance of an objective, lower the score the better
	public int f(int dist, int value){
		double c = 2;
		int score = (int)(c * Math.pow(dist,2.1)) - value;
		return score;
	}
	
	public int getPickupValue(PickupType t, Hero h){
		for(int i = 0; i < pickups.length; i++){
			if(pickups[i].equals(t))
				return h.getPickupValues()[i];
		}
		return 0;
	}
	
	public int getControlPointValue(ControlPoint c, Hero h){
		int x = 1;
		if(c.getControllingTeam() == team) x=-10000;
		if(c.isMainframe()) return h.getControlPValues()[0]*x;
		else return h.getControlPValues()[1]*x;
	}
	
	//inner classes-------------------------------------------------------------------
	
	class Hero
	{
		World world;
		EnemyUnit[] EU;
		FriendlyUnit I;
		
		Point objective;
		int unit;
		
		int action;
		
		int[] pickupValues = {
				-99999999,   //blaster not worth it
				180, //scatter
				180, //laser rifle
				250,   //rail gun
				250,  //shield
				250  //health pack
		};
		
		int[] controlPValues = {
				300,  //mainframe
				250,  //capture flag
		};
		
		//CONSTRUCTOR
		public Hero(World world, EnemyUnit[] EU, FriendlyUnit FU, int unit){
			this.unit = unit;
			this.world = world;
			this.EU = EU;
			this.I = FU;
		}
		
		//make decisions
		public void act(){
			move();
			shoot(); //shoot has more priority, will override if there's need to
		}
		
		//move the unit towards the objective
		public void move(){
			
			Direction d = world.getNextDirectionInPath(I.getPosition() , objective);
			I.move(d);
			
			//System.out.println(I.getPosition().toString()+" "+d.toString());
			
			//check for special cases
			if(I.getPosition().equals(objective)){
				if(I.checkPickupResult() == PickupResult.PICK_UP_VALID)I.pickupItemAtPosition();
				for(int i = 0; i < pickupValues.length; i++)pickupValues[i]/=-100;
			}
		}
		
		//shoot any target that's on the way
		public void shoot(){
			//check if you can shoot anyone
			int target = -1;
			for(int e = 0; e < 4; e++){
				if(I.checkShotAgainstEnemy(EU[e]) == ShotResult.CAN_HIT_ENEMY){
					target = e;
					break;
				}
			}
			if(target!=-1){ //shoot or get shielded
				
				//activate shield if about to die, or more enemies shooting at you
				if(damageTaken() > I.getCurrentWeapon().getDamage() && I.checkShieldActivation() == ActivateShieldResult.SHIELD_ACTIVATION_VALID
						&& I.getShieldedTurnsRemaining() <= 0){
					I.activateShield();
				}
				else{
					I.shootAt(EU[target]);
				}
			}
		}
		
		//calculates max damage done by enemy in a shootout
		public int damageTaken()
		{
			int total = 0;
			int n = 0; //number of enemies that can hit this turn
			for(int e = 0; e < 4; e++){
				int range = EU[e].getCurrentWeapon().getRange();
				Point ep = EU[e].getPosition();
				if(world.canShooterShootTarget(ep, I.getPosition(), range)){
					total += EU[e].getCurrentWeapon().getDamage();
					n++;
				}
			}
			//System.out.println(total*n+" [damage]---------");
			return total*n;
		}
		
		//------------------------^
		
		public void setObjective(Point point){
			objective = point;
		}
		
		public Point getPosition(){
			return I.getPosition();
		}
		
		public int getUnit(){
			return unit;
		}
		
		public void update(FriendlyUnit I, EnemyUnit[] EU){
			this.EU = EU;
			this.I = I;
		}
		
		public int[] getPickupValues(){
			return pickupValues;
		}
		
		public int[] getControlPValues(){
			return controlPValues;
		}
	}//end Hero class
}