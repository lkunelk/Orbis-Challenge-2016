/* Nam
 * October 1, 2016
 * 
 * This is AI for the game "Cyber Team Zero" for the Orbis Challenge.
 * My AI ranked 9th out of 50 or so teams competing.
 *
 * GAME RULES:
 * ------------
 * The game is played on a rectangular grid. Your AI and the opponents 
 * AI take control of 4 units each. Each unit can move, shoot or do other 
 * tasks each turn. The goal is to get more points than the opponent by the end of
 * the game. You can collect points in various ways such as:
 * - collecting pickups (weapons, heal packs)
 * - capturing a flags (flags generate points)
 * - killing a unit
 * This is just the overall look at the game. For more details you can check out the game manual
 *
 * STRATEGY:
 * ----------
 * My AI has a pretty simple strategy. It evaluates costs of going to pickups, flags and mainframes
 * and then assigns each unit an objective to move towards. In my code I called the units "heroes",
 * that's because at the beginning of the hackathon, I was thinking of having two distinct roles for the units:
 * heroes and followers. The hero would be the one assigned tasks to do, while the follower would simply follow
 * the hero it was assigned. This was to help my units survive, as units that stick together are much more effective
 * at fighting against other untis. However as I wrote the code I realized it would be simpler, to just treat every
 * unit as a hero and assign each one the same target, which would have the same effect of the units sticking together.
 * The cost function was just an arbitrary distance function with some parameters that I tuned during testing.
 *
 * My second strategy (which I added few hours before deadline) was to make my units more effective during fights.
 * If a fight ever broke out between units. My other units would try to line up on the opponents unit and help shoot it.
 * I think this was the biggest factor that made my AI more competetive. There's a rule in game that when multiple of your
 * units shoot at the same target, the sum of the damage is multiplied by the number of units shooting. Thus it is a lot more
 * effective to do that in fights.
 *
 * Finally, the top priority for my AI is to always shoot if the enemy is in range, regardless of current objective.
 * 
 * Obviously my AI is not perfect, when I tested it, the AI doesn't perform the second strategy flawlessly, sometimes, one unit will do nothing
 * when a fight breaks out. Or if objectives are too far, my arbitrary cost functions makes my units go for unexpected pickups. Either way it
 * was a fun experience making this AI, I've learned a bunch. Got to code again in a long time.
 *
 * Things I could have done to improve my AI:
 * - take into account weapons, which ones are effective for what kind of maps
 * - making my units avoid, or keep distance from enemy units with dangerous weapons
 *
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
	
	PickupType[] pickups = { //things you can pickup on the map
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
			
			for(int h = 0; h < heroes.length; h++) //for each hero
			{
				for(int p = 0; p < P.length; p++) // for each pickup
				{
					//calculating distance
					int x = heroes[h].getPosition().getX() -  P[p].getPosition().getX();
					int y = heroes[h].getPosition().getY() -  P[p].getPosition().getY();
					int dist = x*x + y*y;
					int value = getPickupValue(P[p].getPickupType(), heroes[h]);
					
					//calculate cost for the objective
					objP[h][p] = f(dist, value);
				}
			}
		}
		
		//evaluate cost of the control points
		if(C.length > 0){
				objC = new int[heroes.length][C.length];
				
				for(int h = 0; h < heroes.length; h++){
						for(int c = 0; c < C.length; c++){
								// using built in method for finding distance
								int dist = world.getPathLength(heroes[h].getPosition(), C[c].getPosition());
								int value = getControlPointValue(C[c], heroes[h]);
								objC[h][c] = f(dist, value);
						}
				}
		}
		
		Point[] obj = new Point[heroes.length]; // stores location of each objective
		
		//assign objectives related to pickups to heroes
		int[] lowest = new int[heroes.length];
		for(int i = 0; i < heroes.length; i++)lowest[i] = 999999999;
		
		if(P.length > 0) //make sure objective list is non-empty
		{
			int[] takenP = new int[P.length];//keeps track of how many units are taking each objective
			for(int h = 0; h < heroes.length; h++){ //for each hero
				
				Point objective = null;
				int n = 0;
				
				// look for lowest score objective
				for(int p = 0; p < P.length; p++)
				{
					if(objP[h][p] < lowest[h] && takenP[p]<2)
					{
						n = p;
						lowest[h] = objP[h][p];
						objective = P[p].getPosition();
						
					}
				}
				
				//assign the objective
				if(objective!=null){
					obj[h] = objective;
					heroes[h].setObjective(objective);
				}
				takenP[n]++;
			}
		}
		
		//assign objectives related to control points to heroes
		//this may override the previous assigned pickup objective, if it has a lower score
		if(C.length > 0) //make sure the list is non-empty
		{
			int[] takenC = new int[C.length]; //keep track of how many units are assigned each objective
			for(int h = 0; h < heroes.length; h++)
			{
				Point objective = null;
				int n = 0;
				
				//look for lowest cost objective
				for(int c = 0; c < C.length; c++)
				{
					if(objC[h][c] < lowest[h] && takenC[c]<4)
					{
						n = c;
						lowest[h] = objC[h][c];
						objective = C[c].getPosition();
						
					}
				}
				
				//assign the objective
				if(objective!=null){
					obj[h] = objective;
					heroes[h].setObjective(objective);
				}
				takenC[n]++;
			}
		}
		
		EnemyUnit[] e = null;
		
		//calculating the dimensions of the map, x = height, y = width
		int x = 0, y = 0;
		while(world.isWithinBounds(new Point(x,y)))
			y++;
		while(world.isWithinBounds(new Point(x,y-1)))
			x++;
		
		//Strategy of helping friendly units if a battle ensues
		for(int h = 0; h < heroes.length; h++){
			
			if(FU[heroes[h].getUnit()].getDamageTakenLastTurn() > 0){ //if there is a shootout get the enemies who are shooting
				e = FU[heroes[h].getUnit()].getEnemiesWhoShotMeLastTurn();
				
				for(int i = 0; i < e.length; i++)
				{
					// get the their positions
					int ex = e[i].getPosition().getX(), ey = e[i].getPosition().getY();
					
					// try to position my other heros so that they can shoot at the enemy
					for(int c = 0; c < heroes.length; c++){
						int shortest = 5;
						Point goal = null;
						
						//search the whole map for positions from which our units can shoot the enemy
						for(int a = 0; a < x; a++)
						{
							for(int b = 0; b < y; b++)
							{
								Point curr = new Point(a,b);
								if((Math.abs(ex - a) == Math.abs(ey-b) || ex - a == 0 || ey - b == 0) // if the current point is on a straight line passing through the enemy
										&& inRange(world, curr, e[i].getPosition(), FU[heroes[c].getUnit()].getCurrentWeapon().getRange(), FU )){ //and our hero can shoot the enemy from there
									
									int path = world.getPathLength(heroes[c].getPosition(), new Point(a,b)); // distance to thee current point
									if(path < shortest){ // we're looking for point that's closest for our hero to go to
											goal = curr;
											shortest = path;
									}
								}
							}
						}
						
						//set the new objective for each hero
						if(goal!=null){
							heroes[c].setObjective(goal);
						}
					}
				}
			}
		}
		
		//tell heroes to make a move
		for(int c = 0; c < heroes.length; c++)
			heroes[c].act();
		
	}
	
	//method for determining, whether point b is in point a's weapon range
	public boolean inRange(World world, Point a, Point b, int gunRange,FriendlyUnit[] FU){
		int dx = 0, dy = 0; //directions to move towards b
		if(b.getX() != a.getX()) dx = (b.getX() - a.getX())/Math.abs(b.getX() - a.getX());
		if(b.getY() != a.getY()) dy = (b.getY() - a.getY())/Math.abs(b.getY() - a.getY());
		
		int x = a.getX() , y = a.getY(); //position of our hero
		
		//if either x or y distance is bigger than gun range we know b is out of range
		if(Math.abs(x-b.getX()) > gunRange || Math.abs(y-b.getY()) > gunRange)return false;
		
		//if any of our heros is standing at this point we ignore it
		for(int i = 0; i < 4; i++)
			if(FU[i].getPosition().equals(a))
				return false;
		
		//check if there are walls in the way between a and b
		while(x!=b.getX() || y!=b.getY()){
			if(world.getTile(new Point(x,y)) == TileType.WALL)return false;
			x+=dx;
			y+=dy;
		}
		
		//if all conditions above failed, then b is in range of a
		return true;
	}
	
	//function for calculating importance of an objective, lower the score the better
	public int f(int dist, int value){
		double c = 2; //arbitrary constant
		int score = (int)(c * Math.pow(dist,2.1)) - value;
		return score;
	}
	
	//get value of pickup
	public int getPickupValue(PickupType t, Hero h){
		for(int i = 0; i < pickups.length; i++){
			if(pickups[i].equals(t))
				return h.getPickupValues()[i];
		}
		return 0;
	}
	
	//get value of control point
	public int getControlPointValue(ControlPoint c, Hero h){
		int x = 1;
		if(c.getControllingTeam() == team) x=-10000;
		if(c.isMainframe()) return h.getControlPValues()[0]*x;
		else return h.getControlPValues()[1]*x;
	}
	
	//inner class-------------------------------------------------------------------
	
	class Hero
	{
		World world;
		EnemyUnit[] EU;
		FriendlyUnit I;
		
		Point objective;
		int unit;
		
		int action;
		
		int[] pickupValues = { //importance of each pickup
				-99999999,   //blaster not worth it
				180, //scatter
				180, //laser rifle
				250,   //rail gun
				250,  //shield
				250  //health pack
		};
		
		int[] controlPValues = { //importance of each control point
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