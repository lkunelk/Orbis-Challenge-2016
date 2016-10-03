import com.orbischallenge.ctz.objects.ControlPoint;
import com.orbischallenge.ctz.objects.Pickup;

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
		
		//evaluate objectives for control points
		for(int p = 0; p < cpsL;p++){
			for(int h = 0; h < heros.length; h++){
				int dist = world.getPathLength(heros[h].getPosition(), cps[p].getPosition());
				objectives[h][psL+p] = f(dist, cps[p]);
			}
		}
		
		boolean[] taken = new boolean[cpsL+psL];
		//assign priorities
		for(int h = 0; h < heros.length; h++){
			int lowest = 999999;
			for(int o = 0; o < psL+cpsL; o++){
				if(objectives[h][o] < lowest && !taken[o]){
					lowest = objectives[h][o];
					taken[o] = true;
					
					//set the objective
					if(o >= psL) heros[h].setObjective(cps[o-psL].getPosition());
					else heros[h].setObjective(ps[o].getPosition());
				}
			}
		}
		
		for(int i = 0; i < heros.length; i++)
			heros[i].act();