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
		
		//check if you can shoot anyone
		for(int f = 0; f < 4; f++){
			int target = -1;
			for(int e = 0; e < 4; e++){
				if(FU[f].checkShotAgainstEnemy(EU[e]) == ShotResult.CAN_HIT_ENEMY){
					target = e;
					break;
				}
			}
			if(target>=0){
				FU[f].shootAt(EU[target]);
				moved[f] = true;
			}
		}
		
		//assign closest pickup to the player
		Pickup[] pickups = world.getPickups();
		Pickup[] assignedP = new Pickup[4];
		
		
		//debug code
		/*System.out.println(FU[0].getPosition().getX()+" - "+FU[0].getPosition().getY());
		for(int i = 0; i < pickups.length; i++){
			int x = pickups[i].getPosition().getX();
			int y = pickups[i].getPosition().getY();
			System.out.println(x+","+y);
		}*/
		
		for(int f = 0; f < 4; f++){
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
		
		//move each player towards the pickup
		for(int f = 0; f < 4; f++){
			if(mode[f] == 0){
				Direction d = world.getNextDirectionInPath(FU[f].getPosition() , assignedP[f].getPosition() );
				FU[f].move(d);
			}
		}
	}//end doMove()
	
	//helper functions
	
	//random move
	public void random_move(World world, EnemyUnit[] EU, FriendlyUnit fu)
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