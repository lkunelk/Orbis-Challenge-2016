/* Andy Rock
 * October 1, 2016
 * 
 * 2016 Orbis Challenge
 */

import com.orbischallenge.ctz.objects.*;
import com.orbischallenge.ctz.objects.enums.*;
import com.orbischallenge.game.engine.*;
import java.util.*;

public class PlayerAI
{
    public void doMove(World world, EnemyUnit[] enemyUnits, FriendlyUnit[] friendlyUnits)
    {
        int[] ddsd = new int[0];
        boolean[] done = greedyMoves(world, enemyUnits, friendlyUnits);

        double[][] heatmap = computeHeatMap(world, enemyUnits, friendlyUnits);

        List<List<Direction>> fs = new ArrayList<List<Direction>>();
        for(int i = 0; i < 4; i++)
        {
            List<Direction> res = new ArrayList<Direction>();

            if(friendlyUnits[i].getHealth() <= 0 || done[i])
                res.add(Direction.NOWHERE);
            else
                for(Direction dir : Direction.values())
                    if(friendlyUnits[i].checkMove(dir) == MoveResult.MOVE_VALID)
                        res.add(dir);

            fs.add(res);
        }

        List<List<Direction>> es = new ArrayList<List<Direction>>();
        for(EnemyUnit eu : enemyUnits)
        {
            List<Direction> res = new ArrayList<Direction>();

            if(eu.getHealth() <= 0)
                res.add(Direction.NOWHERE);
            else
                for(Direction dir : Direction.values())
                    if(world.canMoveFromPointInDirection(eu.getPosition(), dir))
                        res.add(dir);

            es.add(res);
        }

        double best = -1e9;
        Direction[] ans = null;

        for(Direction fa : fs.get(0))for(Direction fb : fs.get(1))for(Direction fc : fs.get(2))for(Direction fd : fs.get(3))
        {
            double worst = 1e9;
            for(Direction ea : es.get(0))for(Direction eb : es.get(1))for(Direction ec : es.get(2))for(Direction ed : es.get(3))
            {
                Point[] f = new Point[4], e = new Point[4];
                f[0] = fa.movePoint(friendlyUnits[0].getPosition());
                f[1] = fb.movePoint(friendlyUnits[1].getPosition());
                f[2] = fc.movePoint(friendlyUnits[2].getPosition());
                f[3] = fd.movePoint(friendlyUnits[3].getPosition());
                e[0] = ea.movePoint(   enemyUnits[0].getPosition());
                e[1] = eb.movePoint(   enemyUnits[1].getPosition());
                e[2] = ec.movePoint(   enemyUnits[2].getPosition());
                e[3] = ed.movePoint(   enemyUnits[3].getPosition());

                double score = 0;
                for(int i = 0; i < 4; i++)
                    score += heatmap[f[i].getX()][f[i].getY()];

                for(int i = 0; i < 1; i++)
                    for(int j = 0; j < 4; j++)
                    {
                        if(world.canShooterShootTarget(f[i], e[i], friendlyUnits[i].getCurrentWeapon().getRange()))
                            score += friendlyUnits[i].getCurrentWeapon().getDamage();

                        if(world.canShooterShootTarget(e[i], f[i],    enemyUnits[i].getCurrentWeapon().getRange()))
                            score -= enemyUnits[i].getCurrentWeapon().getDamage();
                    }

                worst = Math.min(worst, score);
            }

            if(worst > best)
            {
                best = worst;
                ans = new Direction[]{fa, fb, fc, fd};
            }
        }

        for(int i = 0; i < 4; i++)
            if(ans[i] != Direction.NOWHERE)
                friendlyUnits[i].move(ans[i]);
    }


    private boolean[] greedyMoves(World world, EnemyUnit[] enemyUnits, FriendlyUnit[] friendlyUnits)
    {
        List<List<Integer>> shootEnemy = new ArrayList<List<Integer>>();
        for(int i = 0; i < 4; i++)
        {
            List<Integer> res = new ArrayList<Integer>();
            res.add(-1);
            if(friendlyUnits[i].getNumShields() > 0 && friendlyUnits[i].getShieldedTurnsRemaining() == 0)
                res.add(-2);

            for(int j = 0; j < 4; j++)
                if(friendlyUnits[i].getHealth() > 0 && enemyUnits[j].getHealth() > 0)
                    if(world.canShooterShootTarget(friendlyUnits[i].getPosition(), enemyUnits[j].getPosition(), friendlyUnits[i].getCurrentWeapon().getRange()))
                        res.add(j);

            shootEnemy.add(res);
        }

        List<List<Integer>> shootFriend = new ArrayList<List<Integer>>();
        for(int i = 0; i < 4; i++)
        {
            List<Integer> res = new ArrayList<Integer>();
            res.add(-1);
            if(enemyUnits[i].getNumShields() > 0 && enemyUnits[i].getShieldedTurnsRemaining() == 0)
                res.add(-2);

            for(int j = 0; j < 4; j++)
                if(friendlyUnits[j].getHealth() > 0 && enemyUnits[i].getHealth() > 0)
                    if(world.canShooterShootTarget(enemyUnits[i].getPosition(), friendlyUnits[j].getPosition(), enemyUnits[i].getCurrentWeapon().getRange()))
                        res.add(j);

            shootFriend.add(res);
        }

        double best = -1e9;
        Integer[] ans = null;

        for(Integer fa : shootEnemy.get(0))for(Integer fb : shootEnemy.get(1))for(Integer fc : shootEnemy.get(2))for(Integer fd : shootEnemy.get(3))
        {
            double worst = 1e9;
            for(Integer ea : shootFriend.get(0))for(Integer eb : shootFriend.get(1))for(Integer ec : shootFriend.get(2))for(Integer ed : shootFriend.get(3))
            {
                Integer[] f = {fa, fb, fc, fd};
                Integer[] e = {ea, eb, ec, ed};

                int[] fDamage = new int[4], eDamage = new int[4];
                int[] fCount  = new int[4], eCount  = new int[4];

                for(int i = 0; i < 4; i++)
                {
                    if(f[i] >= 0 && e[f[i]] != -2)
                    {
                        eDamage[f[i]] += friendlyUnits[i].getCurrentWeapon().getDamage();
                        eCount [f[i]] += 1;
                    }

                    if(e[i] >= 0 && f[e[i]] != -2)
                    {
                        fDamage[e[i]] += enemyUnits[i].getCurrentWeapon().getDamage();
                        fCount [e[i]] += 1;
                    }
                }

                for(int i = 0; i < 4; i++)
                {
                    eDamage[i] *= 10 * eCount[i];
                    if(eDamage[i] >= enemyUnits[i].getHealth())
                        eDamage[i] += 100;

                    fDamage[i] *= 10 * fCount[i];
                    if(fDamage[i] >= friendlyUnits[i].getHealth())
                        fDamage[i] += 100;
                }

                double score = 0;
                for(int i = 0; i < 4; i++)
                    score += eDamage[i] - fDamage[i];

                worst = Math.min(worst, score);
            }

            if(worst > best)
            {
                best = worst;
                ans = new Integer[]{fa, fb, fc, fd};
            }
        }

        System.out.println(java.util.Arrays.toString(ans));

        boolean[] used = new boolean[4];
        for(int i = 0; i < 4; i++)
            if(!ans[i].equals(-1))
            {
                used[i] = true;
                if(ans[i].equals(-2))
                    friendlyUnits[i].activateShield();
                else
                {
                    ShotResult sr = friendlyUnits[i].shootAt(enemyUnits[ans[i]]);
                    System.out.println(sr);
                }
            }
            else if(world.getPickupAtPosition(friendlyUnits[i].getPosition()) != null)
            {
                used[i] = true;
                friendlyUnits[i].pickupItemAtPosition();
            }

        return used;
    }


    private static final int     FRIEND_MAIN_FRAME = 0;
    private static final int      ENEMY_MAIN_FRAME = 10;
    private static final int    NEUTRAL_MAIN_FRAME = 20;
    private static final int  FRIEND_CONTROL_POINT = 0;
    private static final int   ENEMY_CONTROL_POINT = 5;
    private static final int NEUTRAL_CONTROL_POINT = 10;
    private static final int                PICKUP = 3;
    private static final int                 ENEMY = 0;
    private static final int                FRIEND = 0;

    private double[][] computeHeatMap(World world, EnemyUnit[] enemyUnits, FriendlyUnit[] friendlyUnits)
    {
        int width = 0, height = 0;
        while(world.isWithinBounds(new Point(++width, 0)));
        while(world.isWithinBounds(new Point(0, ++height)));

        double[][] ans = new double[width][height];
        for(ControlPoint cp : world.getControlPoints())
        {
            Point pos = cp.getPosition();
            if(cp.isMainframe())
                if(cp.getControllingTeam() == friendlyUnits[0].getTeam())
                    ans[pos.getX()][pos.getY()] += FRIEND_MAIN_FRAME;
                else if(cp.getControllingTeam() == enemyUnits[0].getTeam())
                    ans[pos.getX()][pos.getY()] += ENEMY_MAIN_FRAME;
                else
                    ans[pos.getX()][pos.getY()] += NEUTRAL_MAIN_FRAME;
            else
                if(cp.getControllingTeam() == friendlyUnits[0].getTeam())
                    ans[pos.getX()][pos.getY()] += FRIEND_CONTROL_POINT;
                else if(cp.getControllingTeam() == enemyUnits[0].getTeam())
                    ans[pos.getX()][pos.getY()] += ENEMY_CONTROL_POINT;
                else
                    ans[pos.getX()][pos.getY()] += NEUTRAL_CONTROL_POINT;
        }

        for(Pickup p : world.getPickups())
        {
            Point pos = p.getPosition();
            ans[pos.getX()][pos.getY()] += PICKUP;
        }

        for(EnemyUnit eu : enemyUnits)
        {
            Point pos = eu.getPosition();
            ans[pos.getX()][pos.getY()] += ENEMY;
        }

        for(FriendlyUnit fu : friendlyUnits)
        {
            Point pos = fu.getPosition();
            ans[pos.getX()][pos.getY()] += FRIEND;
        }

        double[][] map = new double[width][height];
        for(int i = 0; i < width; i++)
            for(int j = 0; j < height; j++)
                for(int k = 0; k < width; k++)
                    for(int l = 0; l < height; l++)
                        if(i == k && j == l)
                            map[i][j] += ans[k][l];
                        else if(world.getPathLength(new Point(i, j), new Point(k, l)) != 0)
                            map[i][j] += ans[k][l] * Math.pow(2, -world.getPathLength(new Point(i, j), new Point(k, l)));

        return map;
    }
}