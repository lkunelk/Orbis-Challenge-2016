import com.orbischallenge.ctz.Constants;
import com.orbischallenge.ctz.objects.EnemyUnit;
import com.orbischallenge.ctz.objects.FriendlyUnit;
import com.orbischallenge.ctz.objects.World;
import com.orbischallenge.ctz.objects.enums.Direction;
import com.orbischallenge.ctz.objects.enums.MoveResult;
import com.orbischallenge.ctz.objects.Pickup;
import com.orbischallenge.ctz.objects.World;
import com.orbischallenge.game.engine.Point;
import com.orbischallenge.ctz.objects.enums.PickupType;
import com.orbischallenge.ctz.objects.enums.WeaponType;
import com.orbischallenge.ctz.objects.enums.ShotResult;


//If have time, find a way to more efficiently get things.
//If possible, have a artilleried unit guard a weapon so that an unartilleried can get it

public class PlayerAI {

    public PlayerAI() {
		//Any initialization code goes here.
		
    }
    static int mainFrameGuy;
    static WeaponType mainFrameWeapon;
    static boolean mainFrameGuyActivated=false;
    public int[] maxArray(int[] myArray){
        int[] mA = new int[2];
        mA[0] = 0;
        mA[1] = 1;
        int j;
        
        for (j = 0; j < myArray.length; ++j) {
            if (myArray[j] > mA[0]) {
                mA[0] = myArray[j];
                mA[1] = j;
            }
        }
            
        return mA;
    }

	 public void shootWeakest(int unitNum, World world, EnemyUnit[] enemyUnits, FriendlyUnit[] friendlyUnits){
        
        int i;
    
        int [] enemyStatus = new int[4];
        for (i = 0; i <= 3; ++i){
            if (friendlyUnits[unitNum].checkShotAgainstEnemy(enemyUnits[i]) == ShotResult.CAN_HIT_ENEMY){
                //record health
                enemyStatus[i] = enemyUnits[i].getHealth();
            }
            else{
                enemyStatus[i] = 0;
            }
            
        }
        if (maxArray(enemyStatus)[0] != 0){ 
            friendlyUnits[unitNum].shootAt(enemyUnits[maxArray(enemyStatus)[1]]);
        }

    }
	void moveUnit(int unitNum,Point loc,FriendlyUnit[] friendlyUnits){
		friendlyUnits[unitNum].move(loc);
	}

    void activateMainFrameGuard (FriendlyUnit[] friendlyUnits){
        if (!mainFrameGuyActivated){
            for (int i=1; i<4; i++){
                if (friendlyUnits[i].getCurrentWeapon()==mainFrameWeapon){
                    mainFrameGuy=i;
                    mainFrameGuyActivated=true;
                }
            }
        }
    }

	Pickup[] getLoc(World world){
		return world.getPickups();
	} 

	boolean powerful(PickupType gun){


		return gun==PickupType.WEAPON_LASER_RIFLE ||gun==PickupType.WEAPON_RAIL_GUN ||gun==PickupType.WEAPON_SCATTER_GUN ; 
	}

	Point getClosest(Pickup[]locs,Point currentLoc,World world){
		int closest=0;
		for(int i=1; i<locs.length; i++){
              if (world.getPathLength (currentLoc,locs[i].getPosition())<world.getPathLength(currentLoc,locs[closest].getPosition())){
              	closest=i;
              }
         }
         return locs[closest].getPosition();
	}


    public void doMove(World world, EnemyUnit[] enemyUnits, FriendlyUnit[] friendlyUnits) {
    	for (int i=0;i<4;i++){
    		if(friendlyUnits[i].getHealth()>0){
                if (i!=mainFrameGuy){
    	    		if (world.getPickups().length!=0){
    				    	friendlyUnits[i].pickupItemAtPosition();
    				   		moveUnit(i,getClosest(getLoc(world),friendlyUnits[i].getPosition (),world),friendlyUnits);
    		   		}
    				else{

    		   		}

    		   		//If problem with runtime too long, put this at first and set a boolean to mark if action done or not
    	    		if (friendlyUnits[i].getCurrentWeapon()!=WeaponType.MINI_BLASTER){
    	    			shootWeakest(i, world, enemyUnits, friendlyUnits);
    	    		}
	    		}
                else{
                    //moveUnit (i,mainFrameLoc ,friendlyUnits);
                }
	    			
		   			
		   		
    		}
    	}

    	/*
    	Pickup [] pickUpLocs=new Pickup[6];
    	pickUpLocs = world.getPickups();

    	friendlyUnits[0].move(world.getPickups()[0].getPosition());
    	friendlyUnits[0].pickupItemAtPosition();
*/

    }
}
/*
    	Direction [] dir=new Direction [2];
    	dir[0]=Direction.EAST;
    	dir[1]=Direction.WEST;
    	System.out.println(dir[theDir]);
		if(friendlyUnits[0].checkMove(dir[theDir])==MoveResult.BLOCKED_BY_WORLD){
			theDir=(theDir+1)%2;
			friendlyUnits[0].move(dir[theDir]);

		}
		else{
		
			friendlyUnits[0].move(dir[theDir]);
		}
		*/