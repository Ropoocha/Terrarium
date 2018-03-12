package net.gegy1000.terrarium.server.world.pipeline.source.earth;

import com.google.gson.JsonObject;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.json.InstanceJsonValueParser;
import net.gegy1000.terrarium.server.world.json.InstanceObjectParser;
import net.gegy1000.terrarium.server.world.pipeline.source.CachedRemoteSource;
import net.gegy1000.terrarium.server.world.pipeline.source.DataTilePos;
import net.gegy1000.terrarium.server.world.pipeline.source.SourceException;
import net.gegy1000.terrarium.server.world.pipeline.source.TerrariumRemoteData;
import net.gegy1000.terrarium.server.world.pipeline.source.TiledDataSource;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTileAccess;
import net.minecraft.util.JsonUtils;
import net.minecraft.world.World;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;

public class SRTMHeightSource extends TiledDataSource<ShortRasterTileAccess> implements CachedRemoteSource {
    public static final int TILE_DATA_SIZE = 1201;
    public static final int TILE_SIZE = 1200;

    private static final Set<DataTilePos> VALID_TILES = new HashSet<>();

    private final File cacheRoot;

    public SRTMHeightSource(CoordinateState coordinateState, String cacheRoot) {
        super(new Coordinate(coordinateState, TILE_SIZE, TILE_SIZE), 9);
        this.cacheRoot = new File(CachedRemoteSource.GLOBAL_CACHE_ROOT, cacheRoot);
    }

    public static void loadValidTiles() {
        try (DataInputStream input = new DataInputStream(new BufferedInputStream(new GZIPInputStream(SRTMHeightSource.getTilesURL().openStream())))) {
            int count = input.readInt();
            for (int i = 0; i < count; i++) {
                int latitude = input.readShort();
                int longitude = input.readShort();
                VALID_TILES.add(new DataTilePos(longitude, -latitude));
            }
        } catch (IOException e) {
            Terrarium.LOGGER.error("Failed to load valid height tiles", e);
        }
    }

    private static URL getTilesURL() throws IOException {
        return new URL(String.format("%s/%s/%s", TerrariumRemoteData.info.getBaseURL(), TerrariumRemoteData.info.getHeightsEndpoint(), TerrariumRemoteData.info.getHeightTiles()));
    }

    @Override
    public ShortRasterTileAccess loadTile(DataTilePos key) throws SourceException {
        key = new DataTilePos(key.getTileX(), key.getTileZ() + 1);
        if (VALID_TILES.isEmpty() || VALID_TILES.contains(key)) {
            try (DataInputStream input = new DataInputStream(this.getStream(key))) {
                short[] heightmap = new short[TILE_DATA_SIZE * TILE_DATA_SIZE];
                for (int i = 0; i < heightmap.length; i++) {
                    heightmap[i] = input.readShort();
                }
                return new ShortRasterTileAccess(heightmap, TILE_DATA_SIZE, TILE_DATA_SIZE);
            } catch (IOException e) {
                Terrarium.LOGGER.error("Failed to parse height tile at {}", key, e);
            }
        }
        return null;
    }

    @Override
    public Class<ShortRasterTileAccess> getTileType() {
        return ShortRasterTileAccess.class;
    }

    @Override
    protected ShortRasterTileAccess getDefaultTile() {
        return new ShortRasterTileAccess(new short[TILE_DATA_SIZE * TILE_DATA_SIZE], TILE_DATA_SIZE, TILE_DATA_SIZE);
    }

    @Override
    public File getCacheRoot() {
        return this.cacheRoot;
    }

    @Override
    public InputStream getRemoteStream(DataTilePos key) throws IOException {
        String cachedName = this.getCachedName(key);
        URL url = new URL(String.format("%s/%s/%s", TerrariumRemoteData.info.getBaseURL(), TerrariumRemoteData.info.getHeightsEndpoint(), cachedName));
        return new GZIPInputStream(url.openStream());
    }

    @Override
    public String getCachedName(DataTilePos key) {
        String latitudePrefix = -key.getTileZ() >= 0 ? "N" : "S";
        String longitudePrefix = key.getTileX() >= 0 ? "E" : "W";

        StringBuilder latitudeString = new StringBuilder(String.valueOf(Math.abs(key.getTileZ())));
        while (latitudeString.length() < 2) {
            latitudeString.insert(0, "0");
        }
        latitudeString.insert(0, latitudePrefix);

        StringBuilder longitudeString = new StringBuilder(String.valueOf(Math.abs(key.getTileX())));
        while (longitudeString.length() < 3) {
            longitudeString.insert(0, "0");
        }
        longitudeString.insert(0, longitudePrefix);

        return String.format(TerrariumRemoteData.info.getHeightsQuery(), latitudeString.toString(), longitudeString.toString());
    }

    public static class Parser implements InstanceObjectParser<TiledDataSource<?>> {
        @Override
        public TiledDataSource<?> parse(TerrariumWorldData worldData, World world, InstanceJsonValueParser valueParser, JsonObject objectRoot) {
            CoordinateState coordinateState = valueParser.parseCoordinateState(objectRoot, "tile_coordinate");
            String cache = JsonUtils.getString(objectRoot, "cache");
            return new SRTMHeightSource(coordinateState, cache);
        }
    }
}
