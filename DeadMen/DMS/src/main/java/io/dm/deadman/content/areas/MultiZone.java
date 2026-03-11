package io.dm.deadman.content.areas;

import io.dm.model.entity.player.Player;
import io.dm.model.map.Bounds;
import io.dm.model.map.MapListener;

import java.util.ArrayList;
import java.util.List;

public class MultiZone {

    private static List<Player> players;
    private static Bounds falador = new Bounds(new int[][] {
            {2944,3455},
            {3008,3455},
            {3008,3327},
            {3015,3327},
            {3015,3312},
            {3021,3312},
            {3023,3314},
            {3042,3314},
            {3044,3312},
            {3044,3300},
            {3041,3297},
            {3038,3297},
            {3038,3298},
            {3033,3298},
            {3033,3297},
            {3021,3297},
            {3021,3299},
            {3014,3299},
            {3014,3303},
            {3008,3303},
            {3008,3304},
            {2944,3304}
    }, 0);

    private static Bounds barb_outpost = new Bounds(new int[][] {
            {3136,3456},
            {3136,3392},
            {3048,3392},
            {3048,3408},
            {3056,3408},
            {3056,3438},
            {3064,3438},
            {3064,3447},
            {3072,3447},
            {3072,3456}
    }, 0);

    private static Bounds wizard_tower = new Bounds(new int[][] {
            {3105,3178},
            {3112,3175},
            {3120,3175},
            {3129,3164},
            {3114,3143},
            {3099,3146},
            {3091,3169}
    }, 0);

    private static Bounds duel_arena = new Bounds(3264,3135,3327,3199, 0);

    private static Bounds necromancer = new Bounds(new int[][] {
            {2657,3232},
            {2657,3232},
            {2664,3232},
            {2664,3218},
            {2683,3218},
            {2683,3256},
            {2657,3256}
    }, 0);

    private static Bounds range_guild = new Bounds(new int[][] {
            {2680,3408},
            {2656,3408},
            {2656,3415},
            {2648,3415},
            {2648,3440},
            {2656,3440},
            {2656,3448},
            {2680,3448},
            {2680,3439},
            {2688,3439},
            {2688,3415},
            {2680,3415}
    }, 0);

    private static Bounds rellekka = new Bounds(new int[][] {
            {2652,3712},
            {2737,3712},
            {2737,3735},
            {2712,3735},
            {2712,3728},
            {2703,3728},
            {2703,3737},
            {2675,3737},
            {2665,3731},
            {2665,3723}
    }, 0);

    private static Bounds ape_atoll = new Bounds(2688,2684,2816,2816, 0);

    private static Bounds pest_control = new Bounds(2622,2556,2689,2626, 0);

    private static Bounds battlefield = new Bounds(new int[][] {
            {2504,3248},
            {2544,3248},
            {2544,3232},
            {2552,3232},
            {2552,3208},
            {2504,3208}
    }, 0);

    private static Bounds jungle = new Bounds(2755,2881,2879,2938, 0);

    private static Bounds bandit_camp = new Bounds(new int[][] {
            {3136,3005},
            {3136,2958},
            {3163,2958},
            {3163,2957},
            {3171,2957},
            {3176,2951},
            {3189,2951},
            {3199,2975},
            {3196,2982},
            {3196,2997},
            {3193,3002},
            {3182,3002},
            {3182,3003},
            {3178,3003},
            {3178,3005}
    }, 0);

    private static Bounds port1 = new Bounds(new int[][] {
            {3708,3453},
            {3702,3453},
            {3701,3452},
            {3692,3452},
            {3692,3451},
            {3689,3451},
            {3689,3450},
            {3688,3450},
            {3688,3449},
            {3684,3449},
            {3684,3448},
            {3683,3448},
            {3683,3447},
            {3682,3447},
            {3682,3443},
            {3683,3443},
            {3683,3440},
            {3684,3440},
            {3684,3439},
            {3686,3439},
            {3686,3430},
            {3708,3430}
    }, 0);

    private static Bounds port2 = new Bounds(new int[][] {
            {3616,3420},
            {3639,3412},
            {3656,3429},
            {3656,3432},
            {3664,3442},
            {3664,3448},
            {3659,3452},
            {3654,3452},
            {3653,3451},
            {3647,3451},
            {3647,3454},
            {3634,3456},
            {3586,3457},
            {3600,3434},
            {3585,3430},
            {3585,3411},
            {3588,3411},
            {3590,3409},
            {3592,3409},
            {3593,3410},
            {3598,3410},
            {3606,3415},
            {3614,3420}
    }, 0);

    private static Bounds port_phas = new Bounds(new int[][] {
            {3650,3457},
            {3650,3470},
            {3653,3473},
            {3653,3504},
            {3656,3507},
            {3668,3507},
            {3677,3516},
            {3687,3516},
            {3687,3496},
            {3689,3494},
            {3690,3494},
            {3690,3493},
            {3693,3493},
            {3693,3483},
            {3698,3482},
            {3699,3482},
            {3699,3481},
            {3703,3481},
            {3703,3470},
            {3714,3470},
            {3714,3453},
            {3702,3453},
            {3701,3452},
            {3696,3452},
            {3693,3454},
            {3676,3454},
            {3674,3452},
            {3667,3452},
            {3665,3453},
            {3662,3453},
            {3660,3455},
            {3653,3455}
    }, 0);


    static {
        players = new ArrayList<>();

        MapListener.registerBounds(falador)
                .onEnter(MultiZone::entered)
                .onExit(MultiZone::exited);

        MapListener.registerBounds(barb_outpost)
                .onEnter(MultiZone::entered)
                .onExit(MultiZone::exited);

        MapListener.registerBounds(wizard_tower)
                .onEnter(MultiZone::entered)
                .onExit(MultiZone::exited);

        MapListener.registerBounds(duel_arena)
                .onEnter(MultiZone::entered)
                .onExit(MultiZone::exited);

        MapListener.registerBounds(necromancer)
                .onEnter(MultiZone::entered)
                .onExit(MultiZone::exited);

        MapListener.registerBounds(range_guild)
                .onEnter(MultiZone::entered)
                .onExit(MultiZone::exited);

        MapListener.registerBounds(rellekka)
                .onEnter(MultiZone::entered)
                .onExit(MultiZone::exited);

        MapListener.registerBounds(ape_atoll)
                .onEnter(MultiZone::entered)
                .onExit(MultiZone::exited);

        MapListener.registerBounds(pest_control)
                .onEnter(MultiZone::entered)
                .onExit(MultiZone::exited);

        MapListener.registerBounds(battlefield)
                .onEnter(MultiZone::entered)
                .onExit(MultiZone::exited);

        MapListener.registerBounds(jungle)
                .onEnter(MultiZone::entered)
                .onExit(MultiZone::exited);

        MapListener.registerBounds(bandit_camp)
                .onEnter(MultiZone::entered)
                .onExit(MultiZone::exited);

        MapListener.registerBounds(port1)
                .onEnter(MultiZone::entered)
                .onExit(MultiZone::exited);

        MapListener.registerBounds(port2)
                .onEnter(MultiZone::entered)
                .onExit(MultiZone::exited);

        MapListener.registerBounds(port_phas)
                .onEnter(MultiZone::entered)
                .onExit(MultiZone::exited);
    }

    private static void entered(Player player) {
        players.add(player);
    }

    private static void exited(Player player, boolean logout) {
        players.remove(player);
        if (!logout) {
            //do something...
        }
    }

    public static boolean contains(Player player) {
        return players.contains(player);
    }

}
