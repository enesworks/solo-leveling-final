package dev.eness.sololevelingfinal.core.dungeon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;

public class DungeonGenerator {
   private static final int INTERIOR_H = 4;
   private static final int TOTAL_H = 6;
   private static final int EMPTY = 0;
   private static final int ROOM = 1;
   private static final int CORRIDOR = 2;
   private static final int WALL = 3;

   public static String generate(ServerLevel level, BlockPos origin, int complexity, DungeonTheme theme) {
      Random rng = new Random();
      int c = Math.max(1, Math.min(10, complexity));
      int targetRooms = c * 2 + 3;
      int gridSize = c * 22 + 40;
      int baseY = Math.max(5, origin.getY() - 6 - 10);
      int[][] grid = new int[gridSize][gridSize];
      List<DungeonRoom> rooms = placeRooms(rng, grid, gridSize, targetRooms, c);
      if (rooms.isEmpty()) {
         return "§cDungeon generation failed — could not place any rooms.";
      }

      for (int[] edge : buildMST(rooms)) {
         markCorridor(grid, gridSize, rooms.get(edge[0]), rooms.get(edge[1]));
      }

      expandWalls(grid, gridSize);
      int offsetX = origin.getX() - rooms.get(0).centerX();
      int offsetZ = origin.getZ() - rooms.get(0).centerZ();
      buildWorld(level, grid, gridSize, baseY, offsetX, offsetZ, theme, rng);
      decorateRooms(level, rooms, baseY, offsetX, offsetZ, theme, rng);
      buildEntryShaft(level, origin, baseY, rooms.get(0), offsetX, offsetZ, theme);
      int ex = offsetX + rooms.get(0).centerX();
      int ez = offsetZ + rooms.get(0).centerZ();
      return String.format("§a%s §7generated with §e%d rooms§7.  Entry: §f%d %d %d", theme.displayName, rooms.size(), ex, baseY + 1, ez);
   }

   private static List<DungeonRoom> placeRooms(Random rng, int[][] grid, int gridSize, int target, int complexity) {
      List<DungeonRoom> rooms = new ArrayList<>();
      int minSize = 7;
      int maxExtra = Math.max(1, complexity / 2 + 3);
      int attempts = target * 35;

      for (int i = 0; i < attempts && rooms.size() < target; i++) {
         int w = minSize + rng.nextInt(maxExtra);
         int l = minSize + rng.nextInt(maxExtra);
         int maxGx = gridSize - w - 2;
         int maxGz = gridSize - l - 2;
         if (maxGx >= 2 && maxGz >= 2) {
            int gx;
            int gz;
            DungeonRoom.Type type;
            if (rooms.isEmpty()) {
               gx = gridSize / 2 - w / 2;
               gz = gridSize / 2 - l / 2;
               type = DungeonRoom.Type.ENTRY;
            } else {
               gx = 2 + rng.nextInt(maxGx - 2);
               gz = 2 + rng.nextInt(maxGz - 2);
               type = DungeonRoom.Type.NORMAL;
            }

            DungeonRoom candidate = new DungeonRoom(gx, gz, w, l, type);
            boolean fits = true;

            for (DungeonRoom existing : rooms) {
               if (candidate.overlaps(existing, 3)) {
                  fits = false;
                  break;
               }
            }

            if (fits) {
               rooms.add(candidate);

               for (int dx = 0; dx < w; dx++) {
                  for (int dz = 0; dz < l; dz++) {
                     int cx = gx + dx;
                     int cz = gz + dz;
                     if (inBounds(cx, cz, gridSize)) {
                        grid[cx][cz] = 1;
                     }
                  }
               }
            }
         }
      }

      if (rooms.size() < 2) {
         return rooms;
      }

      DungeonRoom entry = rooms.get(0);
      int bossIdx = 1;
      int bestDist = -1;

      for (int i = 1; i < rooms.size(); i++) {
         int d = distSq(rooms.get(i), entry);
         if (d > bestDist) {
            bestDist = d;
            bossIdx = i;
         }
      }

      rooms.set(bossIdx, rooms.get(bossIdx).withType(DungeonRoom.Type.BOSS));

      for (int i = 1; i < rooms.size(); i++) {
         if (rooms.get(i).type == DungeonRoom.Type.NORMAL && rng.nextFloat() < 0.15F) {
            rooms.set(i, rooms.get(i).withType(DungeonRoom.Type.TREASURE));
         }
      }

      return rooms;
   }

   private static List<int[]> buildMST(List<DungeonRoom> rooms) {
      int n = rooms.size();
      boolean[] inMST = new boolean[n];
      int[] minD = new int[n];
      int[] parent = new int[n];
      Arrays.fill(minD, Integer.MAX_VALUE);
      Arrays.fill(parent, -1);
      minD[0] = 0;
      List<int[]> edges = new ArrayList<>();

      for (int iter = 0; iter < n; iter++) {
         int u = -1;

         for (int i = 0; i < n; i++) {
            if (!inMST[i] && (u == -1 || minD[i] < minD[u])) {
               u = i;
            }
         }

         inMST[u] = true;
         if (parent[u] != -1) {
            edges.add(new int[]{parent[u], u});
         }

         for (int v = 0; v < n; v++) {
            if (!inMST[v]) {
               int d = distSq(rooms.get(u), rooms.get(v));
               if (d < minD[v]) {
                  minD[v] = d;
                  parent[v] = u;
               }
            }
         }
      }

      return edges;
   }

   private static void markCorridor(int[][] grid, int gs, DungeonRoom a, DungeonRoom b) {
      int ax = a.centerX();
      int az = a.centerZ();
      int bx = b.centerX();
      int bz = b.centerZ();

      for (int x = Math.min(ax, bx); x <= Math.max(ax, bx); x++) {
         for (int dz = -1; dz <= 1; dz++) {
            setIfEmpty(grid, gs, x, az + dz);
         }
      }

      for (int z = Math.min(az, bz); z <= Math.max(az, bz); z++) {
         for (int dx = -1; dx <= 1; dx++) {
            setIfEmpty(grid, gs, bx + dx, z);
         }
      }
   }

   private static void setIfEmpty(int[][] grid, int gs, int x, int z) {
      if (inBounds(x, z, gs) && grid[x][z] == 0) {
         grid[x][z] = 2;
      }
   }

   private static void expandWalls(int[][] grid, int gs) {
      int[][] copy = new int[gs][gs];

      for (int x = 0; x < gs; x++) {
         System.arraycopy(grid[x], 0, copy[x], 0, gs);
      }

      for (int x = 0; x < gs; x++) {
         label47:
         for (int z = 0; z < gs; z++) {
            if (copy[x][z] == 0) {
               for (int dx = -1; dx <= 1; dx++) {
                  for (int dz = -1; dz <= 1; dz++) {
                     int nx = x + dx;
                     int nz = z + dz;
                     if (inBounds(nx, nz, gs) && copy[nx][nz] != 0) {
                        grid[x][z] = 3;
                        continue label47;
                     }
                  }
               }
            }
         }
      }
   }

   private static void buildWorld(ServerLevel level, int[][] grid, int gs, int baseY, int offsetX, int offsetZ, DungeonTheme theme, Random rng) {
      BlockState air = Blocks.AIR.defaultBlockState();
      BlockState floor = theme.floor.defaultBlockState();

      for (int gx = 0; gx < gs; gx++) {
         for (int gz = 0; gz < gs; gz++) {
            int cell = grid[gx][gz];
            if (cell != 0) {
               int wx = offsetX + gx;
               int wz = offsetZ + gz;
               if (cell == 3) {
                  for (int dy = 0; dy < 6; dy++) {
                     level.setBlock(new BlockPos(wx, baseY + dy, wz), pickWall(theme, rng), 2);
                  }
               } else {
                  level.setBlock(new BlockPos(wx, baseY, wz), floor, 2);

                  for (int dy = 1; dy <= 4; dy++) {
                     level.setBlock(new BlockPos(wx, baseY + dy, wz), air, 2);
                  }

                  level.setBlock(new BlockPos(wx, baseY + 6 - 1, wz), theme.wall.defaultBlockState(), 2);
               }
            }
         }
      }
   }

   private static BlockState pickWall(DungeonTheme theme, Random rng) {
      float r = rng.nextFloat();
      if (r < 0.65F) {
         return theme.wall.defaultBlockState();
      } else {
         return r < 0.9F ? theme.accent.defaultBlockState() : theme.rare.defaultBlockState();
      }
   }

   private static void decorateRooms(ServerLevel level, List<DungeonRoom> rooms, int baseY, int offsetX, int offsetZ, DungeonTheme theme, Random rng) {
      for (DungeonRoom room : rooms) {
         int wx0 = offsetX + room.gx;
         int wz0 = offsetZ + room.gz;
         int floorY = baseY + 1;
         int ceilY = baseY + 6 - 1;
         int lightY = ceilY - 1;
         BlockState light = lightState(theme);
         if (room.width >= 7 && room.length >= 7) {
            int[][] corners = new int[][]{
               {wx0 + 1, wz0 + 1}, {wx0 + room.width - 2, wz0 + 1}, {wx0 + 1, wz0 + room.length - 2}, {wx0 + room.width - 2, wz0 + room.length - 2}
            };
            BlockState pillarState = theme.pillar.defaultBlockState();

            for (int[] c : corners) {
               for (int dy = 0; dy < 4; dy++) {
                  level.setBlock(new BlockPos(c[0], floorY + dy, c[1]), pillarState, 2);
               }
            }
         }

         int dx;
         switch (room.type) {
            case ENTRY:
               decorateEntryRoom(level, room, baseY, wx0, wz0, theme);
               continue;
            case BOSS:
               decorateBossRoom(level, room, baseY, wx0, wz0, theme);
               continue;
            default:
               dx = 2;
         }

         while (dx < room.width - 2) {
            for (int dz = 2; dz < room.length - 2; dz += 4) {
               level.setBlock(new BlockPos(wx0 + dx, lightY, wz0 + dz), light, 2);
            }

            dx += 4;
         }

         level.setBlock(new BlockPos(wx0 + room.width / 2, lightY, wz0 + room.length / 2), light, 2);
      }
   }

   private static void decorateEntryRoom(ServerLevel level, DungeonRoom room, int baseY, int wx0, int wz0, DungeonTheme theme) {
      int ceilY = baseY + 6 - 1;
      int lightY = ceilY - 1;
      int w = room.width;
      int l = room.length;
      int cx = w / 2;
      int cz = l / 2;
      BlockState accent = theme.accent.defaultBlockState();
      BlockState rare = theme.rare.defaultBlockState();
      BlockState light = lightState(theme);

      for (int dx = 0; dx < w; dx++) {
         level.setBlock(new BlockPos(wx0 + dx, baseY, wz0 + cz), accent, 2);
      }

      for (int dz = 0; dz < l; dz++) {
         level.setBlock(new BlockPos(wx0 + cx, baseY, wz0 + dz), accent, 2);
      }

      int[][] corners = new int[][]{{wx0 + 1, wz0 + 1}, {wx0 + w - 2, wz0 + 1}, {wx0 + 1, wz0 + l - 2}, {wx0 + w - 2, wz0 + l - 2}};

      for (int[] c : corners) {
         level.setBlock(new BlockPos(c[0], baseY, c[1]), accent, 2);
      }

      level.setBlock(new BlockPos(wx0 + cx, baseY, wz0 + cz), rare, 2);

      for (int dx = 1; dx < w - 1; dx += 3) {
         for (int dz = 1; dz < l - 1; dz += 3) {
            level.setBlock(new BlockPos(wx0 + dx, lightY, wz0 + dz), light, 2);
         }
      }

      level.setBlock(new BlockPos(wx0 + cx, lightY, wz0 + cz), light, 2);
   }

   private static void decorateBossRoom(ServerLevel level, DungeonRoom room, int baseY, int wx0, int wz0, DungeonTheme theme) {
      int floorY = baseY + 1;
      int ceilY = baseY + 6 - 1;
      int lightY = ceilY - 1;
      int w = room.width;
      int l = room.length;
      int cx = w / 2;
      int cz = l / 2;
      BlockState accent = theme.accent.defaultBlockState();
      BlockState pillar = theme.pillar.defaultBlockState();
      BlockState rare = theme.rare.defaultBlockState();
      BlockState light = lightState(theme);

      for (int dx = 0; dx < w; dx++) {
         for (int dz = 0; dz < l; dz++) {
            if ((dx + dz) % 2 == 0) {
               level.setBlock(new BlockPos(wx0 + dx, baseY, wz0 + dz), accent, 2);
            }
         }
      }

      int half = w >= 11 && l >= 11 ? 2 : 1;

      for (int dx = -half; dx <= half; dx++) {
         for (int dz = -half; dz <= half; dz++) {
            level.setBlock(new BlockPos(wx0 + cx + dx, floorY, wz0 + cz + dz), pillar, 2);
         }
      }

      level.setBlock(new BlockPos(wx0 + cx, floorY, wz0 + cz), rare, 2);

      for (int dx = 1; dx < w - 1; dx += 2) {
         level.setBlock(new BlockPos(wx0 + dx, lightY, wz0 + 1), light, 2);
         level.setBlock(new BlockPos(wx0 + dx, lightY, wz0 + l - 2), light, 2);
      }

      for (int dz = 1; dz < l - 1; dz += 2) {
         level.setBlock(new BlockPos(wx0 + 1, lightY, wz0 + dz), light, 2);
         level.setBlock(new BlockPos(wx0 + w - 2, lightY, wz0 + dz), light, 2);
      }

      level.setBlock(new BlockPos(wx0 + cx, lightY, wz0 + cz), light, 2);
   }

   private static void buildEntryShaft(ServerLevel level, BlockPos origin, int baseY, DungeonRoom entry, int offsetX, int offsetZ, DungeonTheme theme) {
      int sx = offsetX + entry.centerX();
      int sz = offsetZ + entry.centerZ();
      int dungeonCeilingY = baseY + 6 - 1;
      int shaftTopY = origin.getY();
      level.setBlock(new BlockPos(sx, dungeonCeilingY, sz), Blocks.AIR.defaultBlockState(), 2);
      BlockState ladderState = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.WEST);
      BlockState wallState = theme.wall.defaultBlockState();

      for (int y = dungeonCeilingY; y <= shaftTopY; y++) {
         level.setBlock(new BlockPos(sx, y, sz), Blocks.AIR.defaultBlockState(), 2);
         level.setBlock(new BlockPos(sx + 1, y, sz), wallState, 2);
         level.setBlock(new BlockPos(sx, y, sz), ladderState, 3);
      }

      level.setBlock(new BlockPos(sx, baseY + 1, sz), lightState(theme), 2);
   }

   private static boolean inBounds(int x, int z, int gs) {
      return x >= 0 && x < gs && z >= 0 && z < gs;
   }

   private static int distSq(DungeonRoom a, DungeonRoom b) {
      int dx = a.centerX() - b.centerX();
      int dz = a.centerZ() - b.centerZ();
      return dx * dx + dz * dz;
   }

   private static BlockState lightState(DungeonTheme theme) {
      BlockState state = theme.light.defaultBlockState();
      return state.hasProperty(LanternBlock.HANGING) ? state.setValue(LanternBlock.HANGING, true) : state;
   }
}
