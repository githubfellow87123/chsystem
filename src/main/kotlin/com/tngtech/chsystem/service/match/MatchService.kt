package com.tngtech.chsystem.service.match

import com.tngtech.chsystem.dto.PlayedMatch
import com.tngtech.chsystem.entities.MatchEntity
import com.tngtech.chsystem.entities.PlayerEntity
import org.springframework.stereotype.Service
import java.util.stream.Collectors

@Service
class MatchService {

    fun isResultMissing(matchEntities: Set<MatchEntity>): Boolean {
        return convertToPlayedMatches(matchEntities) == null
    }

    fun getAllPlayedMatches(matchEntities: Set<MatchEntity>): Set<PlayedMatch> {

        val playedMatches = HashSet<PlayedMatch>()
        for (match in matchEntities) {
            val playedMatch = match.toPlayedMatch()
            if (playedMatch != null) {
                playedMatches.add(playedMatch)
            }
        }

        return playedMatches
    }

    fun convertToPlayedMatches(matchEntities: Set<MatchEntity>): Set<PlayedMatch>? {

        val playedMatches = HashSet<PlayedMatch>()
        for (match in matchEntities) {
            val playedMatch = match.toPlayedMatch()
                ?: return null
            playedMatches.add(playedMatch)
        }

        return playedMatches
    }

    fun mapPlayersToMatches(
        players: Set<PlayerEntity>,
        alreadyPlayedMatches: Set<PlayedMatch>
    ): Map<PlayerEntity, Set<PlayedMatch>> {

        val playerToMatches = HashMap<PlayerEntity, Set<PlayedMatch>>()

        for (player in players) {
            val matchesOfPlayer = alreadyPlayedMatches.stream()
                .filter { match ->
                    match.player1 == player || match.player2 == player
                }
                .collect(Collectors.toSet())
            playerToMatches[player] = matchesOfPlayer
        }

        return playerToMatches
    }

    private fun MatchEntity.toPlayedMatch(): PlayedMatch? {
        val winsPlayer1Val = winsPlayer1
        val winsPlayer2Val = winsPlayer2
        if (winsPlayer1Val == null || winsPlayer2Val == null) {
            return null
        }

        return PlayedMatch(
            id,
            tournament,
            roundIndex,
            player1,
            player2,
            winsPlayer1Val,
            winsPlayer2Val
        )
    }

}