package io.dm.deadman.areas;

import io.dm.deadman.guard.DMMGuardCombat;
import io.dm.deadman.guard.DMMGuard;
import io.dm.model.entity.npc.NPC;
import io.dm.model.entity.player.Player;
import io.dm.model.entity.player.PlayerAction;
import io.dm.model.inter.Interface;
import io.dm.model.inter.InterfaceType;
import io.dm.model.inter.utils.Config;
import io.dm.model.map.Bounds;
import io.dm.model.map.MapListener;
import io.dm.model.map.Position;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SafeZone {

    private static List<Player> players;
    private static HashMap<Player, DMMGuard> guards;

    private static Bounds varrock = new Bounds(
            new int[][] {
                    {3143,3517},
                    {3158,3517},
                    {3160,3514},
                    {3168,3514},
                    {3171,3517},
                    {3189,3517},
                    {3198,3508},
                    {3229,3508},
                    {3235,3501},
                    {3252,3501},
                    {3252,3496},
                    {3255,3493},
                    {3262,3493},
                    {3263,3492},
                    {3263,3472},
                    {3271,3464},
                    {3271,3437},
                    {3273,3437},
                    {3273,3411},
                    {3276,3408},
                    {3287,3408},
                    {3288,3407},
                    {3288,3391},
                    {3289,3390},
                    {3289,3385},
                    {3290,3384},
                    {3290,3379},
                    {3287,3376},
                    {3275,3377},
                    {3274,3376},
                    {3265,3376},
                    {3265,3380},
                    {3253,3380},
                    {3253,3382},
                    {3245,3382},
                    {3245,3380},
                    {3242,3380},
                    {3242,3382},
                    {3182,3382},
                    {3182,3398},
                    {3181,3399},
                    {3174,3399},
                    {3174,3448},
                    {3200,3448},
                    {3191,3457},
                    {3186,3458},
                    {3186,3462},
                    {3187,3464},
                    {3187,3467},
                    {3143,3467},
                    {3138,3472},
                    {3138,3482},
                    {3141,3485},
                    {3141,3491},
                    {3138,3494},
                    {3138,3513}},
            0);

    private static Bounds lumbridge = new Bounds(
            new int[][] {
                {3256,3190},
                {3260,3206},
                {3253,3219},
                {3243,3225},
                {3243,3227},
                {3234,3260},
                {3233,3261},
                {3233,3264},
                {3213,3264},
                {3213,3257},
                {3201,3257},
                {3201,3220},
                {3200,3220},
                {3200,3217},
                {3201,3217},
                {3201,3204},
                {3204,3201},
                {3213,3201},
                {3215,3203},
                {3221,3203},
                {3226,3208},
                {3228,3206},
                {3228,3205},
                {3227,3205},
                {3227,3202},
                {3228,3202},
                {3228,3201},
                {3230,3201},
                {3230,3195},
                {3238,3195},
                {3238,3191},
                {3247,3191},
                {3247,3190}
            }, 0);

    private static Bounds yanille = new Bounds(new int[][] {
            {2539,3107},
            {2541,3107},
            {2543,3109},
            {2608,3109},
            {2620,3097},
            {2620,3076},
            {2619,3075},
            {2591,3075},
            {2589,3073},
            {2584,3073},
            {2582,3075},
            {2543,3075},
            {2541,3077},
            {2539,3077}
    }, 0);

    private static Bounds ardougne = new Bounds(new int[][] {
            {2559,3263},
            {2559,3337},
            {2564,3342},
            {2615,3342},
            {2617,3340},
            {2668,3340},
            {2674,3334},
            {2682,3334},
            {2688,3328},
            {2688,3265},
            {2638,3264},
            {2637,3263},
            {2626,3263},
            {2625,3264},
            {2612,3264},
            {2611,3263},
            {2611,3258},
            {2610,3257},
            {2604,3257},
            {2602,3258},
            {2602,3264},
            {2587,3263},
            {2580,3258},
            {2573,3258},
            {2570,3261},
            {2567,3261},
            {2563,3256},
            {2560,3256},
            {2560,3263}
    }, 0);

    private static Bounds tree_gnome = new Bounds(new int[][] {
            {2385,3523},
            {2393,3523},
            {2394,3524},
            {2399,3524},
            {2400,3525},
            {2403,3525},
            {2405,3523},
            {2406,3523},
            {2407,3522},
            {2414,3522},
            {2415,3521},
            {2425,3521},
            {2428,3523},
            {2429,3523},
            {2432,3520},
            {2494,3521},
            {2496,3456},
            {2496,3440},
            {2494,3438},
            {2494,3433},
            {2498,3429},
            {2498,3418},
            {2496,3416},
            {2496,3413},
            {2495,3412},
            {2495,3409},
            {2494,3408},
            {2494,3404},
            {2502,3396},
            {2505,3396},
            {2506,3395},
            {2506,3392},
            {2505,3391},
            {2502,3391},
            {2501,3390},
            {2494,3390},
            {2493,3391},
            {2489,3391},
            {2488,3390},
            {2488,3389},
            {2486,3389},
            {2485,3390},
            {2482,3390},
            {2481,3389},
            {2477,3389},
            {2476,3390},
            {2472,3390},
            {2471,3391},
            {2468,3391},
            {2466,3389},
            {2466,3386},
            {2464,3384},
            {2459,3384},
            {2457,3386},
            {2457,3389},
            {2455,3391},
            {2450,3391},
            {2449,3390},
            {2447,3390},
            {2446,3391},
            {2443,3391},
            {2441,3389},
            {2440,3389},
            {2439,3388},
            {2435,3388},
            {2432,3391},
            {2429,3391},
            {2427,3393},
            {2421,3393},
            {2416,3398},
            {2416,3399},
            {2414,3401},
            {2414,3408},
            {2410,3412},
            {2387,3412},
            {2387,3407},
            {2384,3407},
            {2383,3408},
            {2381,3408},
            {2379,3410},
            {2378,3410},
            {2376,3412},
            {2376,3413},
            {2375,3414},
            {2375,3417},
            {2369,3423},
            {2369,3432},
            {2371,3434},
            {2371,3443},
            {2375,3447},
            {2375,3458},
            {2381,3464},
            {2381,3476},
            {2380,3476},
            {2377,3479},
            {2377,3485},
            {2375,3487},
            {2375,3498},
            {2377,3500},
            {2377,3506},
            {2380,3509},
            {2380,3520},
            {2381,3521},
            {2382,3521},
            {2383,3522},
            {2384,3522}
    }, 0);

    private static Bounds rellekka = new Bounds(new int[][] {
            {2652,3712},
            {2689,3712},
            {2691,3710},
            {2691,3707},
            {2693,3705},
            {2693,3704},
            {2690,3698},
            {2690,3693},
            {2693,3688},
            {2693,3685},
            {2689,3679},
            {2689,3672},
            {2691,3668},
            {2691,3667},
            {2689,3663},
            {2689,3658},
            {2692,3652},
            {2692,3648},
            {2686,3645},
            {2675,3645},
            {2673,3644},
            {2670,3644},
            {2669,3645},
            {2659,3645},
            {2657,3644},
            {2653,3644},
            {2652,3645},
            {2650,3644},
            {2646,3644},
            {2645,3645},
            {2636,3645},
            {2635,3646},
            {2627,3646},
            {2615,3653},
            {2609,3653},
            {2606,3655},
            {2596,3655},
            {2611,3682},
            {2624,3682},
            {2625,3684},
            {2634,3684},
            {2638,3690},
            {2644,3690},
            {2652,3709}
    }, 0);

    private static Bounds pest_control = new Bounds(2629,2633,2680,2682, 0);

    static {

        players = new ArrayList<>();
        guards = new HashMap<Player, DMMGuard>();

        MapListener.registerBounds(varrock)
                .onEnter(SafeZone::entered)
                .onExit(SafeZone::exited);

        MapListener.registerBounds(lumbridge)
                .onEnter(SafeZone::entered)
                .onExit(SafeZone::exited);

        MapListener.registerBounds(yanille)
                .onEnter(SafeZone::entered)
                .onExit(SafeZone::exited);

        MapListener.registerBounds(ardougne)
                .onEnter(SafeZone::entered)
                .onExit(SafeZone::exited);

        MapListener.registerBounds(tree_gnome)
                .onEnter(SafeZone::entered)
                .onExit(SafeZone::exited);

        MapListener.registerBounds(rellekka)
                .onEnter(SafeZone::entered)
                .onExit(SafeZone::exited);

        MapListener.registerBounds(pest_control)
                .onEnter(SafeZone::entered)
                .onExit(SafeZone::exited);
    }

    private static void entered(Player player) {
        System.out.println("SAFEZONE: " + player.getName() + " > ENTERED");

        players.add(player);
        player.attackPlayerListener = SafeZone::allowAttack;
        player.attackNpcListener = SafeZone::allowNPCAttack;
        player.closeInterface(InterfaceType.WILDERNESS_OVERLAY);
        Config.IN_PVP_AREA.set(player, 0);
        player.setAction(1, PlayerAction.ATTACK);
        player.getEquipment().update(0);
        player.getEquipment().sendUpdates();

//        if (player.getSkullTimer().skulled()) {
//            NPC n = new DMMGuard();
//            n.setCombat(new DMMGuardCombat());
//            n.spawn(player.getPosition());
//        }
    }

    private static void exited(Player player, boolean logout) {
        System.out.println("SAFEZONE: " + player.getName() + " > EXITED");
        if (logout) return;
        players.remove(player);
        player.attackPlayerListener = SafeZone::allowAttack;
        player.attackNpcListener = SafeZone :: allowNPCAttack;
        player.closeInterface(InterfaceType.WILDERNESS_OVERLAY);
        Config.IN_PVP_AREA.set(player, 1);
        player.setAction(1, PlayerAction.ATTACK);
        player.getEquipment().update(0);
        player.getEquipment().sendUpdates();

        if (guards.containsKey(player)) {
            guards.get(player).remove();
        }
    }

    private static boolean allowAttack(Player player, Player pTarget, boolean message) {
        return true;
    }
    private static boolean allowNPCAttack(Player player, NPC npc, boolean message) {
        return true;
    }


}
