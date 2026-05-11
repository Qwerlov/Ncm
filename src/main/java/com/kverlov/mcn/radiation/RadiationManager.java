package com.kverlov.mcn.radiation;

import com.kverlov.mcn.NcmMod;
import com.kverlov.mcn.config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.*;

public enum RadiationManager {
    INSTANCE;

    private static final Logger LOGGER = LogManager.getLogger();
    public final Map<BlockPos, RadiationSource> sources = new HashMap<>();
    private final Map<BlockPos, Double> blockDoses = new HashMap<>();
    private final Map<BlockPos, Cell> grid = new HashMap<>();
    private final Queue<BlockPos> dirtyCells = new ArrayDeque<>();
    private ServerLevel level;
    private boolean initialized = false;
    private int cellSize = 4;

    public void initialize(ServerLevel level) {
        this.level = level;
        cellSize = Config.SERVER.radiationCellSize.get();
        sources.clear();
        blockDoses.clear();
        grid.clear();
        dirtyCells.clear();
        initialized = true;
    }

    public void reinitialize(ServerLevel level) {
        Map<BlockPos, RadiationSource> oldSources = new HashMap<>(sources);
        initialize(level);
        sources.putAll(oldSources);
        for (RadiationSource src : sources.values()) {
            markDirty(src.pos);
        }
    }

    private BlockPos getCellPos(BlockPos worldPos) {
        int x = Math.floorDiv(worldPos.getX(), cellSize);
        int y = Math.floorDiv(worldPos.getY(), cellSize);
        int z = Math.floorDiv(worldPos.getZ(), cellSize);
        return new BlockPos(x, y, z);
    }

    private Cell getCell(BlockPos worldPos) {
        BlockPos cellPos = getCellPos(worldPos);
        return grid.computeIfAbsent(cellPos, k -> new Cell());
    }

    public void addSource(BlockPos pos, double activity) {
        sources.put(pos, new RadiationSource(pos, activity));
        markDirty(pos);
    }

    public void removeSource(BlockPos pos) {
        sources.remove(pos);
        markDirty(pos);
    }

    private void markDirty(BlockPos worldPos) {
        BlockPos cellPos = getCellPos(worldPos);
        Cell cell = grid.computeIfAbsent(cellPos, k -> new Cell());
        if (!cell.dirty) {
            cell.dirty = true;
            dirtyCells.add(cellPos);
        }
        for (int dx = -1; dx <= 1; dx++)
            for (int dy = -1; dy <= 1; dy++)
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos neighborPos = cellPos.offset(dx, dy, dz);
                    Cell neighbor = grid.computeIfAbsent(neighborPos, k -> new Cell());
                    if (!neighbor.dirty) {
                        neighbor.dirty = true;
                        dirtyCells.add(neighborPos);
                    }
                }
    }

    public void tick() {
        if (!initialized || level == null) return;
        int processed = 0;
        int maxPerTick = 50;
        while (!dirtyCells.isEmpty() && processed < maxPerTick) {
            BlockPos cellPos = dirtyCells.poll();
            Cell cell = grid.get(cellPos);
            if (cell != null && cell.dirty) {
                recomputeCell(cell, cellPos);
                irradiateBlocks(cellPos, cell.doseRate);
                cell.dirty = false;
                processed++;
            }
        }
    }

    private void recomputeCell(Cell cell, BlockPos cellPos) {
        double totalDose = 0;
        Vec3 center = Vec3.atCenterOf(cellPos);
        for (RadiationSource src : sources.values()) {
            Vec3 sourcePos = Vec3.atCenterOf(src.pos);
            double dist = Math.max(1.0, center.distanceTo(sourcePos));
            double attenuation = getAttenuationFactor(sourcePos, center);
            double contribution = src.activity / (dist * dist) * attenuation;
            if (contribution > Config.SERVER.minDoseForEffect.get() * 0.01)
                totalDose += contribution;
        }
        cell.doseRate = totalDose;
    }

    private void irradiateBlocks(BlockPos cellCenter, double doseRate) {
        if (doseRate <= 0) return;
        double mSvPerTick = doseRate / 72000.0;
        int minX = cellCenter.getX() * cellSize;
        int minY = cellCenter.getY() * cellSize;
        int minZ = cellCenter.getZ() * cellSize;
        for (int x = minX; x < minX + cellSize; x++) {
            for (int y = minY; y < minY + cellSize; y++) {
                for (int z = minZ; z < minZ + cellSize; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (sources.containsKey(pos)) continue;
                    double newDose = blockDoses.getOrDefault(pos, 0.0) + mSvPerTick;
                    blockDoses.put(pos, newDose);
                    if (newDose >= Config.SERVER.blockSourceThreshold.get()) {
                        addSource(pos, Config.SERVER.blockSourceActivity.get());
                        blockDoses.remove(pos);
                    }
                }
            }
        }
    }

    public double getDoseRateAt(Vec3 position) {
        return getDoseRateAtInternal(position, true);
    }

    public double getDoseRateAtRaw(Vec3 position) {
        return getDoseRateAtInternal(position, false);
    }

    private double getDoseRateAtInternal(Vec3 position, boolean applyAttenuation) {
        if (!initialized || level == null) return 0.0;
        double total = 0.0;
        double minEffect = Config.SERVER.minDoseForEffect.get();
        for (RadiationSource src : sources.values()) {
            Vec3 sourcePos = Vec3.atCenterOf(src.pos);
            double dist = Math.max(1.0, position.distanceTo(sourcePos));
            double contrib = src.activity / (dist * dist);
            if (applyAttenuation) {
                contrib *= getAttenuationFactor(sourcePos, position);
            }
            if (contrib > minEffect * 0.01)
                total += contrib;
        }
        return total;
    }

    private double getAttenuationFactor(Vec3 from, Vec3 to) {
        if (!Config.SERVER.enableRaytracing.get()) {
            double dist = from.distanceTo(to);
            return Math.pow(Config.SERVER.airAttenuation.get(), dist);
        }

        double attenuation = 1.0;
        double airAtt = Config.SERVER.airAttenuation.get();
        double x = from.x, y = from.y, z = from.z;
        int bx = (int) Math.floor(x), by = (int) Math.floor(y), bz = (int) Math.floor(z);
        double dx = to.x - from.x, dy = to.y - from.y, dz = to.z - from.z;
        double dist = Math.sqrt(dx*dx + dy*dy + dz*dz);
        if (dist < 0.0001) return 1.0;
        double nx = dx / dist, ny = dy / dist, nz = dz / dist;
        int stepX = nx > 0 ? 1 : (nx < 0 ? -1 : 0);
        int stepY = ny > 0 ? 1 : (ny < 0 ? -1 : 0);
        int stepZ = nz > 0 ? 1 : (nz < 0 ? -1 : 0);
        double tMaxX = stepX != 0 ? ((stepX > 0 ? bx + 1 : bx) - x) / nx : Double.POSITIVE_INFINITY;
        double tMaxY = stepY != 0 ? ((stepY > 0 ? by + 1 : by) - y) / ny : Double.POSITIVE_INFINITY;
        double tMaxZ = stepZ != 0 ? ((stepZ > 0 ? bz + 1 : bz) - z) / nz : Double.POSITIVE_INFINITY;
        double tDeltaX = stepX != 0 ? 1.0 / Math.abs(nx) : Double.POSITIVE_INFINITY;
        double tDeltaY = stepY != 0 ? 1.0 / Math.abs(ny) : Double.POSITIVE_INFINITY;
        double tDeltaZ = stepZ != 0 ? 1.0 / Math.abs(nz) : Double.POSITIVE_INFINITY;

        BlockState state = level.getBlockState(new BlockPos(bx, by, bz));
        double blockAtt = RadiationTags.getAttenuation(state);
        LOGGER.info("[DDA] Start block [{} {} {}] = {} (att={})", bx, by, bz, state.getBlock().getDescriptionId(), blockAtt);
        attenuation *= blockAtt;
        double traveled = 0.0;

        while (traveled < dist) {
            double tNext;
            if (tMaxX <= tMaxY && tMaxX <= tMaxZ) {
                tNext = tMaxX;
                tMaxX += tDeltaX;
                bx += stepX;
            } else if (tMaxY <= tMaxZ) {
                tNext = tMaxY;
                tMaxY += tDeltaY;
                by += stepY;
            } else {
                tNext = tMaxZ;
                tMaxZ += tDeltaZ;
                bz += stepZ;
            }
            double segmentLength = Math.min(tNext, dist) - traveled;
            if (segmentLength > 0) {
                attenuation *= Math.pow(airAtt, segmentLength);
                traveled = Math.min(tNext, dist);
            }
            if (traveled >= dist) break;
            state = level.getBlockState(new BlockPos(bx, by, bz));
            blockAtt = RadiationTags.getAttenuation(state);
            LOGGER.info("[DDA] Block [{} {} {}] = {} (att={})", bx, by, bz, state.getBlock().getDescriptionId(), blockAtt);
            attenuation *= blockAtt;
        }
        LOGGER.info("[DDA] Final attenuation = {}", attenuation);
        return Math.max(0.0, attenuation);
    }

    public double getDoseRate(BlockPos pos) {
        BlockPos cellPos = getCellPos(pos);
        Cell cell = grid.get(cellPos);
        return cell == null ? 0.0 : cell.doseRate;
    }

    public double getBlockDose(BlockPos pos) {
        return blockDoses.getOrDefault(pos, 0.0);
    }

    public void clearBlockDose(BlockPos pos) {
        blockDoses.remove(pos);
        if (sources.containsKey(pos)) {
            removeSource(pos);
        }
    }

    public void removeAll() {
        sources.clear();
        blockDoses.clear();
        grid.clear();
        dirtyCells.clear();
        initialized = false;
        level = null;
    }
}
