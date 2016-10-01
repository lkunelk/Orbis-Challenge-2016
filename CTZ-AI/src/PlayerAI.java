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

public class PlayerAI
{
	public PlayerAI() {}

	/**
	 * This method will get called every turn.
	 *
	 * @param world The latest state of the world.
	 * @param enemyUnits An array of all 4 units on the enemy team. Their order won't change.
	 * @param friendlyUnits An array of all 4 units on your team. Their order won't change.
	 */

	int activeUnits = 4;
	int[] mode = new int[4];
	boolean[] moved = new boolean[4];
	
	Direction[] dir = {
			Direction.NORTH,
			Direction.NORTH_EAST,
			Direction.EAST,
			Direction.SOUTH_EAST,
			Direction.SOUTH,
			Direction.SOUTH_WEST,
			Direction.WEST,
			Direction.NORTH_WEST};
	
	public void doMove(World world, EnemyUnit[] EU, FriendlyUnit[] FU)
	{
		moved = new boolean[4];
		
		//randomly moving around
		for(int f = 0; f < activeUnits; f++){
			if(mode[f] == 1)random_move(FU[f]);
		}
		
		//assign closest pickup to the player
		Pickup[] pickups = world.getPickups();
		Pickup[] assignedP = new Pickup[4];
		
		for(int f = 0; f < activeUnits; f++){
			int closest = 9999999;
			for(int i = 0; i < pickups.length; i++)
			{
				int pathLen = world.getPathLength(FU[f].getPosition(), pickups[i].getPosition());
				if( pathLen < closest && !contains(assignedP, pickups[i]) ){
					closest = pathLen;
					assignedP[f] = pickups[i];
					//System.out.format("soldier %d is assigned p @ (%d,%d)",f,assignedP[f].getPosition().getX(), assignedP[f].getPosition().getY());
				}
			}
		}
		
		//make sure each one has assigned pickup, otherwise go into mode 1
		for(int f = 0; f < activeUnits; f++){
			if(assignedP[f] == null)
			{
				mode[f] = 1;
			}
		}
		
		//move each player towards the pickup
		for(int f = 0; f < activeUnits; f++){
			if(mode[f] == 0){
				Direction d = world.getNextDirectionInPath(FU[f].getPosition() , assignedP[f].getPosition() );
				FU[f].move(d);
			}
		}
		
		//pick up item if unit gets to it
		for(int f = 0 ; f < activeUnits; f++){
			if(mode[f] == 0){
				if(FU[f].getPosition().equals(assignedP[f].getPosition())){
					FU[f].pickupItemAtPosition();
					mode[f] = 1;
				}
			}
		}
		
		//check if you can shoot anyone
		for(int f = 0; f < activeUnits; f++){
			int target = -1;
			for(int e = 0; e < 4; e++){
				if(FU[f].checkShotAgainstEnemy(EU[e]) == ShotResult.CAN_HIT_ENEMY){
					target = e;
					break;
				}
			}
			if(target>=0){
				FU[f].shootAt(EU[target]);
			}
		}
		
	}//end doMove()
	
	//helper functions
	
	//random move
	public void random_move(FriendlyUnit fu)
	{
		int d = (int)(Math.random()*8);
		for(int i = 0; i < 8; i++){
			if(fu.checkMove(dir[i]) == MoveResult.MOVE_VALID)break;
			else d = (int)(Math.random()*8);
		}
		fu.move(dir[d]);
	}
	
	//searches for instance o in the array a,
	//true if instance is in the array otherwise false
	public boolean contains(Object[] a, Object o){
		for(int i = 0; i < a.length; i++)
			if(a[i] == o) return true;
		return false;
	}
}