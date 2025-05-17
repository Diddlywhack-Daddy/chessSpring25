package model;

import java.util.List;

public record ListGamesResult(List<GameInfo> games) {
    public record GameInfo(int gameID, String whiteUsername, String blackUsername, String gameName) {}
}