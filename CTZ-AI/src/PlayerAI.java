/* Nam
 * October 1, 2016
 * 
 * 2016 Orbis Challenge
 */

import com.orbischallenge.ctz.objects.EnemyUnit;
import com.orbischallenge.ctz.objects.FriendlyUnit;
import com.orbischallenge.ctz.objects.Pickup;
import com.orbischallenge.ctz.objects.World;
import com.orbischallenge.ctz.objects.enums.Direction;
import com.orbischallenge.ctz.objects.enums.MoveResult;
import com.orbischallenge.ctz.objects.enums.ShotResult;
import com.orbischallenge.game.engine.Point;

public class PlayerAI
{
	final int SUIT_UP = 0;
	final int STAND_ON_FLAGS = 1;
	final int RAMPAGE = 2;
	
	final int SHOT = 3;
	final int MOVED = 4;
	final int STAND = 5;
	final int PICKED_UP = 6;
	
	public PlayerAI() {}
	
	//the overall strategizing method, pass roles and objectives onto units
	public void doMove(World world, EnemyUnit[] EU, FriendlyUnit[] FU){
		//assign roles
		Hero h1 = new Hero(world, EU, FU[0], SUIT_UP);
		Pickup[] p = world.getPickups();
		h1.setObjective(p[0].getPosition());
		h1.act();
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
			if(target!=-1)I.shootAt(EU[target]);
		}
		
		public void setMode(int mode){
			this.mode = mode;
		}
		
		public void setObjective(Point point){
			objective = point;
		}
		
	}//end Hero class
	
	class Support
	{
		World world;
		EnemyUnit[] EU;
		FriendlyUnit[] FU;
		
		public Support(World world, EnemyUnit[] EU, FriendlyUnit[] FU){
			this.world = world;
			this.EU = EU;
			this.FU = FU;
		}
	}
}