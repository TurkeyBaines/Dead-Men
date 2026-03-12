package io.dm;

import io.dm.api.database.Database;
import io.dm.api.database.DatabaseUtils;
import io.dm.api.database.DummyDatabase;
import io.dm.api.filestore.FileStore;
import io.dm.api.filestore.IndexFile;
import io.dm.api.netty.NettyServer;
import io.dm.api.process.ProcessWorker;
//import io.dm.api.rest.KronosRest;
import io.dm.api.protocol.login.LoginInfo;
import io.dm.api.utils.*;
import io.dm.cache.*;
import io.dm.data.DataFile;
import io.dm.data.impl.login_set;
import io.dm.data.yaml.YamlLoader;
import io.dm.model.World;
import io.dm.model.achievements.Achievement;
import io.dm.model.combat.special.Special;
import io.dm.model.entity.npc.NPC;
import io.dm.model.entity.player.Player;
import io.dm.model.entity.player.PlayerLogin;
import io.dm.model.map.object.actions.impl.Trapdoor;
import io.dm.model.map.object.actions.impl.dungeons.StrongholdSecurity;
import io.dm.model.shop.ShopManager;
import io.dm.network.LoginDecoder;
import io.dm.network.incoming.Incoming;
import io.dm.process.CoreWorker;
import io.dm.process.LoginWorker;
import io.dm.process.event.EventWorker;
import io.dm.services.LatestUpdate;
import io.dm.services.Loggers;
import io.dm.utility.CharacterBackups;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import net.rsprot.compression.HuffmanCodec;
import net.rsprot.compression.provider.DefaultHuffmanCodecProvider;
import net.rsprot.compression.provider.HuffmanCodecProvider;
import net.rsprot.crypto.rsa.RsaKeyPair;
import net.rsprot.crypto.xtea.XteaKey;
import net.rsprot.protocol.api.*;
import net.rsprot.protocol.api.bootstrap.BootstrapBuilder;
import net.rsprot.protocol.api.config.NetworkConfiguration;
import net.rsprot.protocol.api.js5.Js5GroupProvider;
import net.rsprot.protocol.api.login.GameLoginResponseHandler;
import net.rsprot.protocol.common.client.OldSchoolClientType;
import net.rsprot.protocol.game.incoming.buttons.*;
import net.rsprot.protocol.game.incoming.locs.OpLoc;
import net.rsprot.protocol.game.incoming.locs.OpLocT;
import net.rsprot.protocol.game.incoming.messaging.MessagePublic;
import net.rsprot.protocol.game.incoming.misc.user.MoveGameClick;
import net.rsprot.protocol.game.incoming.npcs.OpNpc;
import net.rsprot.protocol.loginprot.incoming.util.*;
import net.rsprot.protocol.loginprot.outgoing.LoginResponse;
import net.rsprot.protocol.loginprot.outgoing.util.AuthenticatorResponse;
import net.rsprot.protocol.message.codec.incoming.GameMessageConsumerRepository;
import net.rsprot.protocol.message.codec.incoming.GameMessageConsumerRepositoryBuilder;
import net.rsprot.protocol.message.codec.incoming.provider.DefaultGameMessageConsumerRepositoryProvider;
import net.rsprot.protocol.message.codec.incoming.provider.GameMessageConsumerRepositoryProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Properties;

@Slf4j
public class Server extends ServerWrapper {

    public static final ProcessWorker worker = newWorker("server-worker", 600L, Thread.NORM_PRIORITY + 1);

    public static Database gameDb = new DummyDatabase();

    public static Database siteDb = new DummyDatabase();

    public static final Database dumpsDb = new DummyDatabase();

    public static Database forumDb = new DummyDatabase();

    public static List<Runnable> afterData = new ArrayList<>();

    public static FileStore fileStore;

    public static File dataFolder;

    public static CharacterBackups backups = new CharacterBackups();

    public static boolean dataOnlyMode = false;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Throwable {
        long startTime = System.currentTimeMillis();

        init(Server.class);

        /*
         * Server properties
         */
        println("Loading server settings...");
        Properties properties = new Properties();
        File systemProps = new File("server.properties");
        log.info("Looking for system.properties in {}", systemProps.getAbsolutePath());
        try (InputStream in = new FileInputStream(systemProps)) {
            properties.load(in);
        } catch (IOException e) {
            logError("Failed to load server settings!", e);
            throw e;
        }

        /*
         * World information
         */
        World.parse(properties);

        /*
         * Loading (Data from cache & databases)
         */
        println("Loading server data...");
        try {

            fileStore = new FileStore("C:/Users/Turk/Documents/GitHub/Dead-Men/DeadMen/Cache/");
            dataFolder = FileUtils.get(properties.getProperty("data_path"));
            Varpbit.load();
            IdentityKit.load();
            AnimDef.load();
            GfxDef.load();
            ScriptDef.load();
            InterfaceDef.load();
            ItemDef.load();
            NPCDef.load();
            ObjectDef.load();
            DataFile.load();

            /*
             * The following must come after DataFile.load
             */
            login_set.setActive(null, properties.getProperty("login_set"));

            //KronosRest.start();

        } catch (Throwable t) {
            logError("", t);
            return;
        }

        /*
         * Database connections
         */
        if (!World.isDev()) {
            println("Connecting to SQL databases...");

            siteDb = new Database(properties.getProperty("database_host"), "kronos", properties.getProperty("database_user"), properties.getProperty("database_password"));
            gameDb = new Database(properties.getProperty("database_host"), "game", properties.getProperty("database_user"), properties.getProperty("database_password"));
            forumDb = new Database(properties.getProperty("database_host"), "community", properties.getProperty("database_user"), properties.getProperty("database_password"));

            DatabaseUtils.connect(new Database[]{gameDb, forumDb, siteDb}, errors -> {
                if (!errors.isEmpty()) {
                    for (Throwable t : errors)
                        logError("Database error", t);
                    System.exit(1);
                }
            });

            Loggers.clearOnlinePlayers(World.id);
            LatestUpdate.fetch();
        }


        Achievement.staticInit();

        ShopManager.registerUI();

        World.initDeadman();

        /*
         * Loading (After data has been loaded!
         */
        for (Runnable r : afterData) {
            try {
                r.run();
            } catch (Throwable t) {
                logError("", t);
                return;
            }
        }
        afterData.clear();
        afterData = null;

        /*
         * Loading (Scripts & handlers)
         */
        println("Loading server scripts & handlers...");
        try {
            Special.load();
            Incoming.load();

            // When packaged, priority messes up and these load too late.
            StrongholdSecurity.register();
            Trapdoor.register();

            PackageLoader.load("io.dm"); //ensures all static blocks load

            // When packaged, priority messes up and these load too late.
            StrongholdSecurity.register();


            YamlLoader.initYamlFiles();
            Trapdoor.register();

            backups.start();

        } catch (Throwable t) {
            logError("Error loading handlers", t);
            //return;
        }

        /*
         * Processing
         */
        println("Starting server workers...");
        worker.queue(() -> {
            CoreWorker.process();
            EventWorker.process();
            return false;
        });
        LoginWorker.start();
        //LiveData.start();
        /*
         * Network (RSProt 226)
         *
         * We no longer pass LoginDecoder.class (184) into NettyServer.start
         * Instead, we attach the RSProt 226 LoginChannelInitializer to the channel pipeline
         */
        //NettyServer nettyServer = NettyServer.start(World.type.getWorldName() + " World (" + World.id + ") Server", World.port, LoginDecoder.class, 5, World.isDev());
        final NetworkService<Player> rsprotService = new AbstractNetworkServiceFactory<Player>() {
            @NotNull
            @Override
            public GameConnectionHandler<Player> getGameConnectionHandler() {
                return new GameConnectionHandler<Player>() {
                    @Override
                    public void onLogin(@NotNull GameLoginResponseHandler<Player> handler, @NotNull LoginBlock<AuthenticationType<?>> block) {
                        final String name = block.getUsername();
                        final int[] keys = block.getSeed();

                        String password = "";
                        int tfaCode = 0;
                        boolean tfaTrust = false;
                        int tfaTrustValue = 0;

                        AuthenticationType<?> auth = block.getAuthentication();
                        if (auth instanceof AuthenticationType.PasswordAuthentication<?>) {
                            Password pw = ((AuthenticationType.PasswordAuthentication<?>) auth).getPassword();
                            password = pw.asString();
                            pw.clear();
                            OtpAuthenticationType otp = ((AuthenticationType.PasswordAuthentication<?>) auth).getOtpAuthentication();
                            if (otp instanceof OtpAuthenticationType.OtpAuthentication) {
                                tfaCode = ((OtpAuthenticationType.OtpAuthentication) otp).getOtp();
                            }
                            if (otp instanceof OtpAuthenticationType.TrustedComputer) {
                                tfaTrust = true;
                                tfaTrustValue = ((OtpAuthenticationType.TrustedComputer) otp).getIdentifier();
                            }
                        } else if (auth instanceof AuthenticationType.TokenAuthentication<?>) {
                            Token tk = ((AuthenticationType.TokenAuthentication<?>) auth).getToken();
                            password = tk.asString();
                            tk.clear();
                            OtpAuthenticationType otp = ((AuthenticationType.TokenAuthentication<?>) auth).getOtpAuthentication();
                            if (otp instanceof OtpAuthenticationType.OtpAuthentication) {
                                tfaCode = ((OtpAuthenticationType.OtpAuthentication) otp).getOtp();
                            }
                            if (otp instanceof OtpAuthenticationType.TrustedComputer) {
                                tfaTrust = true;
                                tfaTrustValue = ((OtpAuthenticationType.TrustedComputer) otp).getIdentifier();
                            }
                        }

                        final String uuid = Base64.getEncoder().encodeToString(block.getUuid());

                        int reservedIndex = -1;
                        Player[] list = World.players.entityList;
                        for (int i = 1; i < list.length; i++) {
                            if (list[i] == null) { reservedIndex = i; break; }
                        }
                        if (reservedIndex == -1) {
                            handler.writeFailedResponse(LoginResponse.ServerFull.INSTANCE);
                            return;
                        }

                        LoginResponse.Ok ok = new LoginResponse.Ok(
                                AuthenticatorResponse.NoAuthenticator.INSTANCE,
                                0,
                                false,
                                reservedIndex,
                                true,
                                0L, 0L, 0L
                        );

                        Session<Player> session = handler.writeSuccessfulResponse(ok, block);
                        if (session == null) {
                            return;
                        }

                        Channel ch = handler.getCtx().channel();
                        LoginInfo info = new LoginInfo(
                                ch,
                                name,
                                password,
                                "",
                                "",
                                uuid,
                                tfaCode,
                                tfaTrust,
                                tfaTrustValue,
                                World.id,
                                keys
                        );

                        LoginWorker.add(new PlayerLogin(info));
                    }

                    @Override
                    public void onReconnect(@NotNull GameLoginResponseHandler<Player> handler, @NotNull LoginBlock<XteaKey> block) {
                        handler.writeFailedResponse(LoginResponse.Retry.INSTANCE);
                    }
                };
            }

            @NotNull
            @Override
            public List<OldSchoolClientType> getSupportedClientTypes() {
                return List.of(OldSchoolClientType.DESKTOP);
            }

            @NotNull
            @Override
            public List<Integer> getPorts() {
                return List.of(World.port);
            }

            @NotNull
            @Override
            public RsaKeyPair getRsaKeyPair() {
                BigInteger EXPONENT = new BigInteger("83923922839219287732917015149195732073695536052984669036231778384894490439511932467277679466826619631057457711190000931928848746974072996674449163992099423404866521840525018174269929252811697346444185514151969525075984167323004389765704130745055780821036980587747264383960689940905074229888881011617738648897");
                BigInteger MODULUS = new BigInteger("94210824259843347324509385276594109263523823612210415282840685497179394322370180677069205378760490069724955139827325518162089726630921395369270393801925644637806226306156731189625154078707248525519618118185550146216513714101970726787284175941436804270501308516733103597242337227056455402809871503542425244523");
                return new RsaKeyPair(EXPONENT, MODULUS);
            }

            @NotNull
            @Override
            public HuffmanCodecProvider getHuffmanCodecProvider() {
                try {
                    Path p = Paths.get("C:\\huffman.dat");
                    byte[] bytes = Files.readAllBytes(p);
                    ByteBuf bb = Unpooled.wrappedBuffer(bytes);
                    HuffmanCodec codec = HuffmanCodec.Companion.create(bb);

                    return new DefaultHuffmanCodecProvider(codec);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to load Huffman from C\\huffman.dat");
                }
            }

            @NotNull
            @Override
            public Js5GroupProvider getJs5GroupProvider() {
                final FileStore store;
                try {
                    store = new FileStore("C:\\Users\\Turk\\Documents\\GitHub\\Dead-Men\\DeadMen\\Cache");

                } catch (IOException e) {
                    throw new RuntimeException("Failed to open Cache");
                }

                return new Js5GroupProvider() {
                    @Nullable
                    @Override
                    public ByteBuf provide(int archive, int group) {
                        IndexFile idx = store.get(archive);
                        if (idx == null) return null;
                        byte[] data = idx.getArchiveData(group);
                        return (data == null) ? null : Unpooled.wrappedBuffer(data);
                    }
                };
            }

            @NotNull
            @Override
            public GameMessageConsumerRepositoryProvider<Player> getGameMessageConsumerRepositoryProvider() {
                final GameMessageConsumerRepositoryBuilder<Player> b = new GameMessageConsumerRepositoryBuilder<>();

                // --------------------------
                // Movement: MOVE_GAMECLICK
                // --------------------------
                // Message type from decoder: MoveGameClick
                // Fields per decoder: x, z, KeyCombination. (We use x & z)
                b.addListener(MoveGameClick.class,
                        (player, msg) -> {
                            player.getRouteFinder().routeAbsolute(msg.getX(), msg.getZ(), false);
                        }
                );

                // --------------------------
                // NPC interaction: OPNPC1
                // --------------------------
                // Message type from decoder: OnNPC
                b.addListener(OpNpc.class,
                        (player, msg) -> {
                            NPC npc = World.getNpc(msg.getIndex());
                            if (npc != null) {
                                player.getRouteFinder().routeEntity(npc);
                                //TODO (Later): invoke specific op=msg.getOp() action handler
                            }
                        }
                );

                // --------------------------
                // Object interaction: OPLOC1..5
                // --------------------------
                // Message type from decoder: OpLoc
                // Fields per decoder: x, z, controlKey, op
                b.addListener(OpLoc.class,
                        (player, msg) -> {
                            player.getRouteFinder().routeAbsolute(msg.getX(), msg.getZ(), false);
                            //TODO (Later): call object action dispatcher for (msg.getOp(), msg.getId())
                        }
                );

                // --------------------------
                // Object interaction: OPLOCT (item on object)
                // --------------------------
                // Message type from decoder: OpLocT
                b.addListener(OpLocT.class,
                        (player, msg) -> {
                            player.getRouteFinder().routeAbsolute(msg.getX(), msg.getZ(), false);
                        }
                );

                // --------------------------
                // Buttons (UI): IF_BUTTON, IF_SUBOP, IF_BUTTOND, IF_BUTTONT, IF3
                // --------------------------
                // Message type from decoder: If1Button, IfSubOp, IfButtonD, IfButtonT, If3Button
                b.addListener(If1Button.class,
                        (player, msg) -> player.sendMessage("[UI] If1Button: " + msg.getCombinedId())
                );
                b.addListener(IfSubOp.class,
                        (player, msg) -> player.sendMessage("[UI] IfSubOp: " + msg.getCombinedId() + ", op=" + msg.getOp() + ", subop=" + msg.getSubop())
                );
                b.addListener(IfButtonD.class,
                        (player, msg) -> player.sendMessage("[UI] IfButtonD: selected=" + msg.getSelectedCombinedId() + " -> target=" + msg.getTargetCombinedId())
                );
                b.addListener(IfButtonT.class,
                        (player, msg) -> player.sendMessage("[UI] IfButtonT: selected=" + msg.getSelectedCombinedId() + " -> target=" + msg.getTargetCombinedId())
                );
                b.addListener(If3Button.class,
                        (player, msg) -> player.sendMessage("[UI] If3Button: " + msg.getCombinedId() + ", sub=" + msg.getSub() + ", obj=" + msg.getObj())
                );

                // --------------------------
                // Public chat: MESSAGE_PUBLIC
                // --------------------------
                b.addListener(MessagePublic.class,
                        (player, msg) -> player.sendMessage(msg.getMessage())
                );

                GameMessageConsumerRepository<Player> repo = b.build();
                return new DefaultGameMessageConsumerRepositoryProvider<>(repo);
            }

        }.build();

        rsprotService.start();

        //log.info("Data path = {}", Server.dataFolder.getAbsolutePath());
        ServerWrapper.println("Started server in " + (System.currentTimeMillis() - startTime) + "ms.");

        /*
         * Shutdown hook
         */
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println();
            System.out.println("Server shutting down with " + worker.getQueuedTaskCount() + " tasks queued.");
            System.out.println("Gracefully shutting down world server...");
            /*
             * Shutdown network
             */
            //nettyServer.shutdown();

            /*
             * Remove players
             */
            int fails = 0;
            while (true) {
                try {
                    for(Player p : World.players) {
                        if(p.getChannel().id() == null)
                            p.logoutStage = -1;
                    }
                    if (Server.worker.getExecutor().submit(World::removePlayers).get())
                        break;

                    ThreadUtils.sleep(10L); //^ that will already be a big enough delay
                } catch (Throwable t) {
                    logError("ERROR: Removing online players", t);
                    if (++fails >= 5 && World.removePlayers())
                        break;
                    ThreadUtils.sleep(1000L);
                }
            }
        }));
    }

    /**
     * Timing
     */
    public static long currentTick() {
        return worker.getExecutions();
    }

    public static long getEnd(long ticks) {
        return currentTick() + ticks;
    }

    public static boolean isPast(long end) {
        return currentTick() >= end;
    }

    public static int tickMs() {
        return (int) Server.worker.getPeriod();
    }

    public static int toTicks(int seconds) {
        return (seconds * 1000) / tickMs();
    }

}