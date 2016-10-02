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
	
	int[] gunPriority = {
			20, //scatter
			10, //laser rifle
			5,   //rail gun
			15,  //shield
			15  //health pack
	};
	
	int[] objPriority = {
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
				new Hero(world, EU, FU[0], SUIT_UP),
				new Hero(world, EU, FU[1], SUIT_UP),
				new Hero(world, EU, FU[2], SUIT_UP),
				new Hero(world, EU, FU[3], SUIT_UP),
			};
		}
		
		//create priority array for each hero based on distance and importance of the objective
		Pickup[] ps = world.getPickups();
		ControlPoint[] cps = world.getControlPoints();
		int psL = 0;
		int cpsL = 0;
		if(ps != null) psL = ps.length;
		if(cps != null) cpsL = cps.length;
		
		int[][] objectives = new int[heros.length][psL+cpsL];
		
		//evaluate objectives for pickup
		for(int p = 0; p < psL; p++){
			for(int h = 0; h < heros.length; h++){
				int dist = world.getPathLength(heros[h].getPosition(), ps[p].getPosition());
				objectives[h][p] = f(dist, ps[p].getPickupType());
			}
		}
		
		
		for(int p = 0; p < cpsL;p++){
			for(int h = 0; h < heros.length; h++){
				int dist = world.getPathLength(heros[h].getPosition(), cps[p].getPosition());
				objectives[h][psL+p] = f(dist, cps[p]);
			}
		}
		
		
		for(int i = 0; i < cpsL; i++){
			System.out.println("score: "+objectives[0][i+psL]);
			System.out.println("dist: "+world.getPathLength(heros[0].getPosition(), cps[i].getPosition()));
			System.out.println("pos: "+cps[i].getPosition().toString());
			System.out.println("type: "+cps[i].isMainframe());
			System.out.println("-------------------------");
		}
		
		//for(int i = 0; i < 1; i++)
			//heros[i].act();
	}
	
	//function for calculating importance of an objective, lower the score the better
	public int f(int dist, Object type){
		double c = .5;
		int score = (int)(c * dist * dist) - priority(type);
		return score;
	}
	
	public int priority(Object t){
		if(t instanceof PickupType)
		switch((PickupType)t)
		{
			case WEAPON_SCATTER_GUN: return gunPriority[0];
			case WEAPON_LASER_RIFLE: return gunPriority[1];
			case WEAPON_RAIL_GUN: return gunPriority[2];
			case SHIELD: return gunPriority[3];
			case REPAIR_KIT: return gunPriority[4];
			default: return -1;
		}
		else{
			ControlPoint p = (ControlPoint)t;
			if( p.getControllingTeam() == team){
				if(p.isMainframe())return -objPriority[1];
				else return -objPriority[0];
			}
			else{
				if(p.isMainframe())return objPriority[1];
				else return objPriority[0];
			}
		}
	}
	
	class Hero
	{
		World world;
		EnemyUnit[] EU;
		FriendlyUnit I;
		
		Point objective;
		int mode;
		
		int action;
		
		//CONSTRUCTOR
		public Hero(World world, EnemyUnit[] EU, FriendlyUnit FU, int mode){
			this.mode = SUIT_UP;
			this.world = world;
			this.EU = EU;
			this.I = FU;
		}
		
		public void act(){
			move();
			shoot(); //shoot has more priority, will override if there's need to
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