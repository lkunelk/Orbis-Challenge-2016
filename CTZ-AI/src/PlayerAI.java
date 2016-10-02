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
import com.orbischallenge.ctz.objects.enums.WeaponType;
import com.orbischallenge.game.engine.Point;

public class PlayerAI
{
	final int SUIT_UP = 0;
	final int STAND_ON_FLAGS = 1;
	final int RAMPAGE = 2;
	
	final int SHOT = 3;
	final int MOVED = 4;
	final int STANDBY = 5;
	final int PICKED_UP = 6;
	
	Hero[] heros;
	Team team; 
	
	PickupType[] pickups = {
			PickupType.WEAPON_SCATTER_GUN,
			PickupType.WEAPON_LASER_RIFLE,
			PickupType.WEAPON_RAIL_GUN,
			PickupType.SHIELD,
			PickupType.REPAIR_KIT,
	};
	
	int[] pickupValues = {
			20, //scatter
			10, //laser rifle
			5,   //rail gun
			15,  //shield
			15  //health pack
	};
	
	int[] controlPValues = {
			100,  //mainframe
			120,  //capture flag
	};
	public PlayerAI() {}
	
	//the overall strategizing method, pass roles and objectives onto units
	public void doMove(World world, EnemyUnit[] EU, FriendlyUnit[] FU){
		
		//assign roles
		if(heros == null){
			//analyze map and decide on roles, also give priority for gun types
			team = FU[0].getTeam();
			heros = new Hero[]{
				new Hero(world, EU, FU[0], 0),
				new Hero(world, EU, FU[1], 1),
				new Hero(world, EU, FU[2], 2),
				new Hero(world, EU, FU[3], 3),
			};
		}
		else{ //update heros
			for(int i = 0; i < heros.length; i++)
				heros[i].update(FU[heros[i].getUnit()], EU);
		}
		
		Pickup[] P = world.getPickups();
		ControlPoint[] C = world.getControlPoints();
		
		int[][] objP = null;
		int[][] objC = null;
		
		//evaluate the cost for each pickup
		if(P != null){
			objP = new int[heros.length][P.length];
			
				for(int h = 0; h < heros.length; h++)
				{
						for(int p = 0; p < P.length; p++)
						{
							int dist = world.getPathLength(heros[h].getPosition(), P[p].getPosition());
							int value = getPickupValue(P[p].getPickupType());
							objP[h][p] = f(dist, value);
						}
				}
		}
		
		//evaluate cost of the control points
		if(C != null){
				objC = new int[heros.length][C.length];
				
				for(int h = 0; h < heros.length; h++){
						for(int c = 0; c < C.length; c++){
								int dist = world.getPathLength(heros[h].getPosition(), C[c].getPosition());
								int value = getControlPointValue(C[c]);
								objC[h][c] = f(dist, value);
						}
				}
		}
		
		//assign objectives to heros
		int[] lowest = new int[heros.length];
		for(int i = 0; i < heros.length; i++)lowest[i] = 999999999;
		
		if(P != null){
			boolean[] takenP = new boolean[P.length];
			for(int h = 0; h < heros.length; h++){
				
				Point objective = null;
				int n = 0;
				
				for(int p = 0; p < P.length; p++)
				{
					if(objP[h][p] < lowest[h] && !takenP[p])
					{
						n = p;
						lowest[h] = objP[h][p];
						objective = P[p].getPosition();
						
					}
				}
				
				if(objective!=null)heros[h].setObjective(objective);
				takenP[n] = true;
			}
		}
		
		if(C != null){
			boolean[] takenC = new boolean[C.length];
			for(int h = 0; h < heros.length; h++){
				
				Point objective = null;
				int n = 0;
				
				for(int c = 0; c < C.length; c++)
				{
					if(objC[h][c] < lowest[h] && !takenC[c])
					{
						n = c;
						lowest[h] = objC[h][c];
						objective = C[c].getPosition();
						
					}
				}
				
				if(objective!=null)heros[h].setObjective(objective);
				takenC[n] = true;
			}
		}
		
		for(int c = 0; c < C.length; c++)
			heros[c].act();
		
	}
	
	//function for calculating importance of an objective, lower the score the better
	public int f(int dist, int value){
		double c = 1;
		int score = (int)(c * dist * dist) - value;
		return score;
	}
	
	public int getPickupValue(PickupType t){
		for(int i = 0; i < pickups.length; i++){
			if(pickups[i].equals(t))
				return pickupValues[i];
		}
		return 0;
	}
	
	public int getControlPointValue(ControlPoint c){
		int x = 1;
		if(c.getControllingTeam() == team) x=-1;
		if(c.isMainframe()) return controlPValues[0]*x;
		else return controlPValues[1]*x;
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
		
		//CONSTRUCTOR
		public Hero(World world, EnemyUnit[] EU, FriendlyUnit FU, int unit){
			this.unit = unit;
			this.world = world;
			this.EU = EU;
			this.I = FU;
		}
		
		public void act(){
			move();
			shoot(); //shoot has more priority, will override if there's need to
		}
		
		public void move(){
			//System.out.println(I.getPosition().toString());
			//System.out.println(objective.toString());
			//System.out.println("");
			
			Direction d = world.getNextDirectionInPath(I.getPosition() , objective);
			I.move(d);
			
			//check for special cases
			if(I.getPosition().equals(objective)){
				if(I.checkPickupResult() == PickupResult.PICK_UP_VALID)I.pickupItemAtPosition();
				action = PICKED_UP;
			}
			
			action = MOVED;
		}
		
		public void shoot(){
			//check if you can shoot anyone
			int target = -1;
			for(int e = 0; e < 4; e++){
				if(I.checkShotAgainstEnemy(EU[e]) == ShotResult.CAN_HIT_ENEMY){
					target = e;
					action = SHOT;
					break;
				}
			}
			if(target!=-1){ //shoot or get shielded
				
				//activate shield if about to die, or more enemies shooting at you
				if(damageTaken() > 4 && I.checkShieldActivation() == ActivateShieldResult.SHIELD_ACTIVATION_VALID
						&& I.getShieldedTurnsRemaining() <= 0){
					I.activateShield();
				}
				else{
					I.shootAt(EU[target]);
				}
			}
		}
		
		//estimates damage done by enemy in a shootout
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
	}//end Hero class
	
	class Support
	{
		World world;
		EnemyUnit[] EU;
		FriendlyUnit I;
		
		Point objective;
		int mode;
		
		int action;
		
		//CONSTRUCTOR
		public Support(World world, EnemyUnit[] EU, FriendlyUnit FU, int mode){
			this.mode = SUIT_UP;
			this.world = world;
			this.EU = EU;
			this.I = FU;
		}
		
		public void act(){
			//move();
			//shoot(); //shoot has more priority, will override if there's need to
			damageTaken();
		}
		
		public void move(){
			Direction d = world.getNextDirectionInPath(I.getPosition() , objective);
			I.move(d);
			
			//check for special cases
			if(I.getPosition().equals(objective)){
				if(mode == SUIT_UP)I.pickupItemAtPosition();
				action = PICKED_UP;
			}
			
			action = MOVED;
		}
		
		public void shoot(){
			//check if you can shoot anyone
			int target = -1;
			for(int e = 0; e < 4; e++){
				if(I.checkShotAgainstEnemy(EU[e]) == ShotResult.CAN_HIT_ENEMY){
					target = e;
					action = SHOT;
					break;
				}
			}
			if(target!=-1){ //shoot or get shielded
				
				//activate shield if about to die, or more enemies shooting at you
				if(damageTaken() > 4 && I.checkShieldActivation() == ActivateShieldResult.SHIELD_ACTIVATION_VALID
						&& I.getShieldedTurnsRemaining() <= 0){
					I.activateShield();
				}
				else{
					I.shootAt(EU[target]);
				}
			}
		}
		
		//estimates damage done by enemy in a shootout
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
		
		public void setMode(int mode){
			this.mode = mode;
		}
		
		public void setObjective(Point point){
			objective = point;
		}
		

		public Point getPosition(){
			return I.getPosition();
		}
	}
}