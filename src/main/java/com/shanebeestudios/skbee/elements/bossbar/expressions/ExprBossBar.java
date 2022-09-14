package com.shanebeestudios.skbee.elements.bossbar.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.shanebeestudios.skbee.api.util.MathUtil;
import com.shanebeestudios.skbee.api.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.entity.Boss;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Name("BossBar")
@Description({"Get a BossBar from an entity (such as a wither), all BossBars of players, create your own custom BossBar,",
        "or get a list of all custom BossBars.",
        "Progress is a number between 0-100",
        "NOTE: BossBars from entities cannot be saved in global variables, as the entity may not be loaded on the",
        "server when that variable is trying to load. Custom BossBars and BossBars from players can be saved in variables."})
@Examples({"set {_bar} to boss bar named \"le-bar\"",
        "set {_bar} to boss bar named \"le-bar\" with title \"Le Title\" with color bar blue with progress 50",
        "delete boss bar named \"le-bar\"",
        "set {_bar} to boss bar of target entity",
        "set {_bars::*} to boss bars of player",
        "set {_bars::*} to all bossbars"})
@Since("1.16.0")
public class ExprBossBar extends SimpleExpression<BossBar> {


    static {
        Skript.registerExpression(ExprBossBar.class, BossBar.class, ExpressionType.COMBINED,
                "boss[ ]bar of %entity%",
                "boss[ ]bars of %players%",
                "[new] boss[ ]bar named %string% [with title %-string%] [with color %-bossbarcolor%] " +
                        "[with style %-bossbarstyle%] [with progress %-number%]",
                "all boss[ ]bars");
    }

    private int pattern;
    private Expression<Entity> entity;
    private Expression<Player> players;
    private Expression<String> key;
    private Expression<String> title;
    private Expression<BarColor> barColor;
    private Expression<BarStyle> barStyle;
    private Expression<Number> progress;

    @SuppressWarnings({"NullableProblems", "unchecked"})
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        pattern = matchedPattern;
        this.entity = pattern == 0 ? (Expression<Entity>) exprs[0] : null;
        this.players = pattern == 1 ? (Expression<Player>) exprs[0] : null;
        this.key = pattern == 2 ? (Expression<String>) exprs[0] : null;
        this.title = pattern == 2 ? (Expression<String>) exprs[1] : null;
        this.barColor = pattern == 2 ? (Expression<BarColor>) exprs[2] : null;
        this.barStyle = pattern == 2 ? (Expression<BarStyle>) exprs[3] : null;
        this.progress = pattern == 2 ? (Expression<Number>) exprs[4] : null;
        return true;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    protected @Nullable BossBar[] get(Event event) {
        if (pattern == 0 && this.entity != null) {
            Entity entity = this.entity.getSingle(event);
            if (entity instanceof Boss boss) {
                return new BossBar[]{boss.getBossBar()};
            }
        } else if (pattern == 1 && this.players != null) {
            List<BossBar> bars = new ArrayList<>();
            Player[] players = this.players.getArray(event);
            Bukkit.getBossBars().forEachRemaining(bossBar -> {
                for (Player player : players) {
                    if (bossBar.getPlayers().contains(player)) {
                        bars.add(bossBar);
                    }
                }
            });
            return bars.toArray(new BossBar[0]);
        } else if (pattern == 2 && this.key != null) {
            String name = this.key.getSingle(event);
            if (name == null) return null;
            NamespacedKey key = Util.getNamespacedKey(name, true);
            if (key != null) {
                KeyedBossBar bossBar = Bukkit.getBossBar(key);
                if (bossBar == null) {
                    String title = null;
                    if (this.title != null && this.title.getSingle(event) != null) {
                        title = this.title.getSingle(event);
                    }

                    BarColor barColor = null;
                    if (this.barColor != null) {
                        barColor = this.barColor.getSingle(event);
                    }
                    if (barColor == null) {
                        barColor = BarColor.PURPLE;
                    }

                    BarStyle barStyle = null;
                    if (this.barStyle != null) {
                        barStyle = this.barStyle.getSingle(event);
                    }
                    if (barStyle == null) {
                        barStyle = BarStyle.SEGMENTED_20;
                    }

                    float progress = 1;
                    if (this.progress != null) {
                        Number proNumber = this.progress.getSingle(event);
                        if (proNumber != null) {
                            progress = MathUtil.clamp(proNumber.floatValue() / 100, 0, 1);
                        }
                    }
                    bossBar = Bukkit.createBossBar(key, title, barColor, barStyle);
                    bossBar.setProgress(progress);
                }
                return new BossBar[]{bossBar};
            }

        } else if (pattern == 3) {
            List<BossBar> bars = new ArrayList<>();
            Bukkit.getBossBars().forEachRemaining(bars::add);
            return bars.toArray(new BossBar[0]);
        }
        return null;
    }

    @Override
    public boolean isSingle() {
        return pattern == 0 || pattern == 2;
    }

    @Override
    public @NotNull Class<? extends BossBar> getReturnType() {
        return BossBar.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean d) {
        if (pattern == 0) {
            return "boss bar of entity " + this.entity.toString(e, d);
        } else if (pattern == 1) {
            return "boss bar of players " + this.players.toString(e, d);
        }else if (pattern == 2) {
            String name = "boss bar named " + this.key.toString(e, d);
            String title = this.title != null ? " named " + this.title.toString(e, d) : "";
            String color = this.barColor != null ? " with color " + this.barColor.toString(e, d) : "";
            String style = this.barStyle != null ? " with style " + this.barStyle.toString(e, d) : "";
            String progress = this.progress != null ? " with progress " + this.progress.toString(e, d) : "";
            return name + title + color + style + progress;
        } else {
            return "all boss bars";
        }
    }

}
