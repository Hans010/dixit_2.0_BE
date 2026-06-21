package com.burnout.dixit.game.service;

import com.burnout.dixit.common.GameId;
import com.burnout.dixit.game.domain.Game;
import com.burnout.dixit.game.persistence.GameMapper;
import com.burnout.dixit.game.persistence.GameSnapshot;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.util.Optional;

/**
 * Persists Game state to Redis, keyed by gameId. Games are stored as a
 * single JSON blob (via GameSnapshot - see that class for why we don't
 * serialize Game directly) under a "dixit:game:<uuid>" key, with a TTL so
 * abandoned games eventually expire instead of accumulating forever.
 */
@ApplicationScoped
public class GameRepository {

    private static final String KEY_PREFIX = "dixit:game:";

    @ConfigProperty(name = "dixit.game.ttl-hours", defaultValue = "24")
    long ttlHours;

    private final ValueCommands<String, GameSnapshot> commands;

    public GameRepository(RedisDataSource redisDataSource) {
        this.commands = redisDataSource.value(GameSnapshot.class);
    }

    public void save(Game game) {
        GameSnapshot snapshot = GameMapper.toSnapshot(game);
        commands.setex(key(game.getId()), Duration.ofHours(ttlHours).toSeconds(), snapshot);
    }

    public Optional<Game> findById(GameId gameId) {
        GameSnapshot snapshot = commands.get(key(gameId));
        return Optional.ofNullable(snapshot).map(GameMapper::fromSnapshot);
    }

    public void delete(GameId gameId) {
        commands.getdel(key(gameId));
    }

    private String key(GameId gameId) {
        return KEY_PREFIX + gameId.uuid();
    }
}
