package br.com.plutomc.lobby.core.lobby.scoreboard;

import lombok.Getter;

@Getter
public abstract class AnimatedCommon {

    public abstract String current();
    public abstract String next();
    public abstract String previous();

}
