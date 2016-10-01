/* Nam
 * October 1, 2016
 * 
 * 2016 Orbis Challenge
 */

import com.orbischallenge.ctz.objects.EnemyUnit;
import com.orbischallenge.ctz.objects.FriendlyUnit;
import com.orbischallenge.ctz.objects.World;
import com.orbischallenge.ctz.objects.enums.Direction;
import com.orbischallenge.ctz.objects.enums.MoveResult;

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
	
	Direction[] dir = {
			Direction.NORTH,
			Direction.NORTH_EAST,
			Direction.EAST,
			Direction.SOUTH_EAST,
			Direction.SOUTH,
			Direction.SOUTH_WEST,
			Direction.WEST,
			Direction.NORTH_WEST};
	
	public void doMove(World world, EnemyUnit[] enemyUnits, FriendlyUnit[] friendlyUnits)
	{
		for(int b = 0; b < 4; b++){
			int i = 0;
			for(int a = 0; a < 8; a++){
				i = (int)(Math.random()*8);
				if(friendlyUnits[b].checkMove(dir[i]) == MoveResult.MOVE_VALID)
					break;
			}
			
			friendlyUnits[b].move(dir[i]);
		}
	}
}